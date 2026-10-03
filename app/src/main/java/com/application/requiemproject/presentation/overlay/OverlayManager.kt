package com.application.requiemproject.presentation.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.application.requiemproject.domain.model.TextBlock
import com.application.requiemproject.domain.repository.TranslationOverlay

/** A WindowManager host for a Compose-only translation overlay. Called on the main thread. */
class OverlayManager(private val context: Context) : TranslationOverlay {
    private val windowManager = context.getSystemService(WindowManager::class.java)
    private var view: ComposeView? = null
    private var owner: OverlayLifecycle? = null
    private var viewModel: OverlayViewModel? = null

    override fun showOverlay() {
        if (view != null || !Settings.canDrawOverlays(context)) return
        val lifecycle = OverlayLifecycle()
        val model = ViewModelProvider(lifecycle)[OverlayViewModel::class.java]
        val compose = ComposeView(context).apply {
            setViewTreeLifecycleOwner(lifecycle)
            setViewTreeSavedStateRegistryOwner(lifecycle)
            setContent {
                val state by model.state.collectAsStateWithLifecycle()
                TranslationOverlay(state.translations, state.accessibility)
            }
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START }
        try {
            windowManager.addView(compose, params)
            lifecycle.start()
            owner = lifecycle
            view = compose
            viewModel = model
        } catch (error: RuntimeException) {
            compose.disposeComposition()
            lifecycle.destroy()
            android.util.Log.e("RequiemOverlay", "Unable to attach overlay", error)
        }
    }
    override fun updateTextOnScreen(rects: List<TextBlock>) { viewModel?.translations(rects) }
    override fun updateAccessibilityOverlay(rects: List<TextBlock>) { viewModel?.accessibility(rects) }
    override fun removeOverlay() {
        view?.let {
            it.disposeComposition()
            if (it.isAttachedToWindow) windowManager.removeView(it)
        }
        owner?.destroy()
        owner = null
        view = null
        viewModel = null
    }
}

private class OverlayLifecycle : LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {
    private val registry = LifecycleRegistry(this)
    private val controller = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = registry
    override val savedStateRegistry: SavedStateRegistry get() = controller.savedStateRegistry
    override val viewModelStore = ViewModelStore()
    init {
        controller.performAttach()
        controller.performRestore(null)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }
    fun start() { registry.handleLifecycleEvent(Lifecycle.Event.ON_START); registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME) }
    fun destroy() { registry.currentState = Lifecycle.State.DESTROYED; viewModelStore.clear() }
}

@Composable
private fun TranslationOverlay(translations: List<TextBlock>, accessibility: List<TextBlock>) {
    val measurer = rememberTextMeasurer()
    Canvas(Modifier.fillMaxSize()) {
        accessibility.forEach { block ->
            block.boundingBox?.let { bounds ->
                if (bounds.width() > 0 && bounds.height() > 0) drawRect(Color(0xFFF32040), Offset(bounds.left.toFloat(), bounds.top.toFloat()),
                    Size(bounds.width().toFloat(), bounds.height().toFloat()), style = Stroke(2f))
            }
        }
        translations.forEach { block ->
            val bounds = block.boundingBox ?: return@forEach
            if (bounds.width() <= 0 || bounds.height() <= 0) return@forEach
            val topLeft = Offset(bounds.left.toFloat(), bounds.top.toFloat())
            drawRect(Color(0xF20D0D10), topLeft, Size(bounds.width().toFloat(), bounds.height().toFloat()))
            val initialSize = (bounds.height() * .65f / density / fontScale).coerceAtLeast(1f)
            val initial = measurer.measure(block.text, TextStyle(color = Color(0xFFF4F0E8), fontSize = initialSize.sp), softWrap = false)
            val ratio = minOf(1f, bounds.width().toFloat() / initial.size.width.coerceAtLeast(1), bounds.height().toFloat() / initial.size.height.coerceAtLeast(1))
            val text = measurer.measure(block.text, TextStyle(color = Color(0xFFF4F0E8), fontSize = (initialSize * ratio).sp), softWrap = false,
                constraints = Constraints(maxWidth = bounds.width(), maxHeight = bounds.height()))
            drawText(text, topLeft = Offset(bounds.centerX() - text.size.width / 2f, bounds.centerY() - text.size.height / 2f))
        }
    }
}
