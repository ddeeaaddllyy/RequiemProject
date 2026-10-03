package com.application.requiemproject.data.platform.capture

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import com.application.requiemproject.data.platform.TagSet.SCREEN_CAPTURE_MANAGER_TAG

/** One clean snapshot per explicit request. Reading an overlay never triggers another scan. */
open class ScreenCaptureManager(
    private val context: Context,
    private val projectionManager: MediaProjectionManager,
    private val backgroundHandler: Handler
) {
    @Volatile private var mediaProjection: MediaProjection? = null
    @Volatile private var imageReader: ImageReader? = null
    private var virtualDisplay: VirtualDisplay? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    @Volatile private var sampling = false
    @Volatile private var awaitingImage = false
    @Volatile var isPaused = false
    private var screenWidth = 0
    private var screenHeight = 0

    var onProcessedCaptured: ((Bitmap, Float, Int) -> Unit)? = null
    var onCaptureVisibility: ((Boolean) -> Unit)? = null
    var onCaptureStopped: (() -> Unit)? = null

    fun startCapture(resultCode: Int, resultData: Intent) {
        if (mediaProjection != null) return
        mediaProjection = projectionManager.getMediaProjection(resultCode, resultData)
        mediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                stopCapture()
                mainHandler.post { onCaptureStopped?.invoke() }
            }
        }, backgroundHandler)
        val bounds = context.getSystemService(WindowManager::class.java).currentWindowMetrics.bounds
        screenWidth = bounds.width()
        screenHeight = bounds.height()
        val reader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2)
        imageReader = reader
        reader.setOnImageAvailableListener({ source ->
            // Leave fresh frames queued while waiting for our windows to disappear.
            if (sampling && !awaitingImage) return@setOnImageAvailableListener
            val image = source.acquireLatestImage() ?: return@setOnImageAvailableListener
            if (awaitingImage && !isPaused) {
                awaitingImage = false
                deliver(image)
            } else image.close()
        }, backgroundHandler)
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "RequiemSnapshot", screenWidth, screenHeight, context.resources.displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader.surface, null, backgroundHandler
        )
        requestScan()
    }

    fun requestScan() {
        if (mediaProjection == null || isPaused || sampling) return
        sampling = true
        onCaptureVisibility?.invoke(false)
        backgroundHandler.post {
            val reader = imageReader
            reader?.acquireLatestImage()?.close()
            backgroundHandler.postDelayed({
                if (reader == null || reader !== imageReader || isPaused) {
                    finishSampling()
                    return@postDelayed
                }
                val image = reader.acquireLatestImage()
                if (image != null) deliver(image)
                else {
                    awaitingImage = true
                    // A display may be static; restore controls if no new frame arrives.
                    backgroundHandler.postDelayed({
                        if (awaitingImage) finishSampling()
                    }, 1500L)
                }
            }, 200L)
        }
    }

    private fun deliver(image: Image) {
        try {
            val bitmap = try { imageToBitmap(image) } finally { image.close() }
            // Preserve native colors, sharpness, and screen coordinates for ML Kit.
            val callback = onProcessedCaptured
            if (callback == null) bitmap.recycle() else callback(bitmap, 1f, 0)
        } catch (error: RuntimeException) {
            Log.e(SCREEN_CAPTURE_MANAGER_TAG, "Unable to capture snapshot", error)
        } finally { finishSampling() }
    }

    private fun finishSampling() {
        awaitingImage = false
        sampling = false
        mainHandler.post { onCaptureVisibility?.invoke(true) }
    }

    private fun imageToBitmap(image: Image): Bitmap {
        val plane = image.planes[0]
        val width = image.width
        val height = image.height
        val paddedWidth = width + (plane.rowStride - plane.pixelStride * width) / plane.pixelStride
        val padded = Bitmap.createBitmap(paddedWidth, height, Bitmap.Config.ARGB_8888)
        padded.copyPixelsFromBuffer(plane.buffer)
        if (paddedWidth == width) return padded
        return Bitmap.createBitmap(padded, 0, 0, width, height).also { padded.recycle() }
    }

    open fun stopCapture() {
        val projection = mediaProjection
        mediaProjection = null
        val display = virtualDisplay
        val reader = imageReader
        virtualDisplay = null
        imageReader = null
        awaitingImage = false
        sampling = false
        display?.release()
        reader?.close()
        projection?.stop()
    }
}
