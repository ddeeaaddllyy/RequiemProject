package com.application.requiemproject

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.view.MotionEvent
import android.view.InputDevice
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.platform.app.InstrumentationRegistry
import com.application.requiemproject.data.repository.OCRRepository
import com.application.requiemproject.domain.model.*
import com.application.requiemproject.domain.usecase.SelectTextForTranslationUseCase
import com.application.requiemproject.presentation.MainActivity
import com.application.requiemproject.presentation.overlay.OverlayManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class SelectiveOverlayTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun actualFrameTapSelectsOnlyItsBlockAndOutsideTapReachesApplication() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val calls = mutableListOf<String>()
        val translator = object : TranslatorModel {
            override suspend fun translate(text: String, languages: String): TranslationResult {
                calls.add(text)
                return TranslationResult.Success("Переведён весь\nвыбранный блок")
            }
        }
        val selection = SelectTextForTranslationUseCase(translator)
        val first = TextBlock("Hello rebel\nTake your time", TextBounds(40, 220, 640, 360))
        val second = TextBlock("Leave me alone", TextBounds(40, 440, 640, 540))
        val taps = AtomicInteger()
        compose.runOnUiThread {
            compose.activity.setContentView(android.widget.Button(compose.activity).apply {
                text = "Underlying application"
                setBackgroundColor(Color.WHITE)
                setOnClickListener { taps.incrementAndGet() }
            })
        }
        permission("allow")
        val overlay = OverlayManager(context)
        try {
            instrumentation.runOnMainSync {
                overlay.setOnBlockSelected { block ->
                    runBlocking { selection.select(block, TranslationSettings(AppLanguage.ENGLISH, AppLanguage.RUSSIAN, ScanSource.OCR)) }
                    overlay.updateBlocks(selection.blocks.value)
                }
                selection.recognize(listOf(first, second))
                overlay.showOverlay()
                overlay.updateBlocks(selection.blocks.value)
            }
            instrumentation.waitForIdleSync()
            assertTrue(calls.isEmpty())
            tap(200f, 290f)
            instrumentation.waitForIdleSync()
            assertEquals(listOf(first.text), calls)
            compose.onNodeWithContentDescription("Перевод: Переведён весь\nвыбранный блок").assertExists()
            assertEquals(SelectionStatus.RECOGNIZED, selection.blocks.value[1].status)
            assertEquals(0, taps.get())
            // Translation must retain its exact source rectangle, including touch bounds.
            SystemClock.sleep(250) // WindowManager surfaces present after Compose semantics update.
            saveScreenshot("09-selective-overlay")
            val screenshot = instrumentation.uiAutomation.takeScreenshot()!!
            try {
                assertEquals(Color.rgb(250, 250, 250), screenshot.getPixel(50, 230))
                assertEquals(Color.WHITE, screenshot.getPixel(650, 300))
                assertEquals(Color.WHITE, screenshot.getPixel(200, 390))
            } finally { screenshot.recycle() }
            tap(200f, 390f)
            instrumentation.waitForIdleSync()
            assertEquals(1, taps.get())
            instrumentation.runOnMainSync { overlay.updateBlocks(emptyList()) }
            instrumentation.waitForIdleSync()
            SystemClock.sleep(160)
            tap(200f, 290f)
            instrumentation.waitForIdleSync()
            assertEquals(2, taps.get())
        } finally {
            instrumentation.runOnMainSync { overlay.removeOverlay() }
            permission("default")
        }
    }

    @Test fun ocrReturnsWholeMultilineBlockWithOriginalScreenCoordinates() {
        val bitmap = Bitmap.createBitmap(800, 240, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; textSize = 42f }
        canvas.drawText("Hello rebel", 40f, 70f, paint)
        canvas.drawText("Take your time", 40f, 120f, paint)
        try {
            val blocks = OCRRepository().recognizeText(bitmap, 1f, 100)
            val block = blocks.first { "Hello rebel" in it.text }
            assertTrue(block.text.contains("Take your time"))
            assertTrue(block.text.contains('\n'))
            val bounds = block.boundingBox!!
            assertTrue(bounds.left in 30..60)
            assertTrue(bounds.top in 120..160)
            assertTrue(bounds.bottom in 210..230)
        } finally {
            bitmap.recycle()
        }
    }

    @Test fun ocrReadsThinWhiteLettersOnDarkBackgroundWithoutContrastConversion() {
        val bitmap = Bitmap.createBitmap(800, 240, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.rgb(13, 13, 16))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(244, 240, 232); textSize = 34f }
        canvas.drawText("Take your time", 40f, 70f, paint)
        canvas.drawText("Hello rebel", 40f, 115f, paint)
        try {
            val blocks = OCRRepository().recognizeText(bitmap, 1f, 0)
            assertTrue(blocks.any { "Take your time" in it.text && "Hello rebel" in it.text })
            val bounds = blocks.first { "Take your time" in it.text }.boundingBox!!
            assertTrue(bounds.left in 30..55)
            assertTrue(bounds.top in 35..60)
            assertTrue(bounds.bottom in 100..125)
        } finally { bitmap.recycle() }
    }

    private fun permission(mode: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(
            "appops set ${instrumentation.targetContext.packageName} SYSTEM_ALERT_WINDOW $mode"
        )).use { it.readBytes() }
    }

    private fun tap(x: Float, y: Float) {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val time = SystemClock.uptimeMillis()
        val down = MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN, x, y, 0)
        down.source = InputDevice.SOURCE_TOUCHSCREEN
        try {
            automation.injectInputEvent(down, true)
            SystemClock.sleep(80)
            val up = MotionEvent.obtain(time, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, x, y, 0)
            up.source = InputDevice.SOURCE_TOUCHSCREEN
            try { automation.injectInputEvent(up, true) } finally { up.recycle() }
        } finally { down.recycle() }
    }

    private fun saveScreenshot(name: String) {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val screenshot = automation.takeScreenshot() ?: return
        val file = java.io.File(compose.activity.getExternalFilesDir(null), "$name.png")
        try { file.outputStream().use { screenshot.compress(Bitmap.CompressFormat.PNG, 100, it) } }
        finally { screenshot.recycle() }
        ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(
            "cp ${file.absolutePath} /sdcard/Download/requiem-ui-review/$name.png"
        )).use { it.readBytes() }
    }
}
