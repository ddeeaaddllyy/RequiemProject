package com.application.requiemproject.presentation.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.savedstate.*
import com.application.requiemproject.domain.model.*
import com.application.requiemproject.domain.repository.TranslationOverlay
import com.application.requiemproject.presentation.theme.*
import com.application.requiemproject.presentation.components.SlashShape

/** Separate touchable windows cover only recognized blocks; the rest of the screen stays usable. */
class OverlayManager(private val context: Context) : TranslationOverlay {
    private val windowManager = context.getSystemService(WindowManager::class.java)
    private val views = mutableMapOf<TextBlock, ComposeView>()
    private var owner: OverlayLifecycle? = null
    private var viewModel: OverlayViewModel? = null
    private var listener: ((TextBlock) -> Unit)? = null
    private var refreshListener: (() -> Unit)? = null
    private var controls: ComposeView? = null
    private var visible = true

    override fun setOnBlockSelected(listener: ((TextBlock) -> Unit)?) { this.listener = listener }
    override fun setOnRefresh(listener: (() -> Unit)?) { refreshListener = listener }

    override fun showOverlay() {
        if (owner != null || !Settings.canDrawOverlays(context)) return
        val lifecycle = OverlayLifecycle()
        viewModel = ViewModelProvider(lifecycle)[OverlayViewModel::class.java]
        owner = lifecycle
        lifecycle.start()
        val toolbar = ComposeView(context).apply {
            setViewTreeLifecycleOwner(lifecycle)
            setViewTreeSavedStateRegistryOwner(lifecycle)
            setContent {
                RequiemTheme {
                    Row(Modifier.fillMaxSize().clip(SlashShape).background(Ink)
                        .border(2.dp, Red, SlashShape)
                        .clickable(role = Role.Button) { refreshListener?.invoke() }
                        .padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Refresh, null, tint = Red)
                        Text("Обновить", color = Paper, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        val density = context.resources.displayMetrics.density
        val params = WindowManager.LayoutParams(
            (156 * density).toInt(), (48 * density).toInt(), WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.BOTTOM or Gravity.END; x = (12 * density).toInt(); y = (72 * density).toInt() }
        try { windowManager.addView(toolbar, params); controls = toolbar }
        catch (error: RuntimeException) {
            toolbar.disposeComposition()
            android.util.Log.e("RequiemOverlay", "Unable to attach scan controls", error)
        }
    }

    override fun updateBlocks(blocks: List<SelectableTextBlock>) {
        val lifecycle = owner ?: return
        val model = viewModel ?: return
        val valid = blocks.filter { it.source.boundingBox?.let { box -> box.width() > 0 && box.height() > 0 } == true }
        model.blocks(valid)
        val sources = valid.map { it.source }.toSet()
        views.keys.filter { it !in sources }.forEach { source -> detach(views.remove(source)!!) }
        valid.forEach { entry ->
            val source = entry.source
            val bounds = source.boundingBox ?: return@forEach
            val screen = windowManager.currentWindowMetrics.bounds
            val x = bounds.left.coerceIn(0, screen.width())
            val y = bounds.top.coerceIn(0, screen.height())
            val width = bounds.right.coerceIn(0, screen.width()) - x
            val height = bounds.bottom.coerceIn(0, screen.height()) - y
            if (width <= 0 || height <= 0) return@forEach
            views[source]?.let { existing ->
                val params = existing.layoutParams as WindowManager.LayoutParams
                if (params.width != width || params.height != height || params.x != x || params.y != y) {
                    params.width = width; params.height = height; params.x = x; params.y = y
                    windowManager.updateViewLayout(existing, params)
                }
                return@forEach
            }
            val compose = ComposeView(context).apply {
                setViewTreeLifecycleOwner(lifecycle)
                setViewTreeSavedStateRegistryOwner(lifecycle)
                visibility = if (visible) View.VISIBLE else View.INVISIBLE
                setContent {
                    val state by model.state.collectAsStateWithLifecycle()
                    state.blocks.firstOrNull { it.source == source }?.let { block ->
                        SelectionFrame(block) { listener?.invoke(source) }
                    }
                }
            }
            val params = WindowManager.LayoutParams(
                width, height, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                this.x = x
                this.y = y
                setFitInsetsTypes(0)
            }
            try {
                windowManager.addView(compose, params)
                views[source] = compose
            } catch (error: RuntimeException) {
                compose.disposeComposition()
                android.util.Log.e("RequiemOverlay", "Unable to attach text frame", error)
            }
        }
    }

    override fun setVisible(visible: Boolean) {
        this.visible = visible
        views.values.forEach { it.visibility = if (visible) View.VISIBLE else View.INVISIBLE }
        controls?.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    override fun removeOverlay() {
        views.values.forEach(::detach)
        views.clear()
        controls?.let(::detach)
        controls = null
        owner?.destroy()
        owner = null
        viewModel = null
        visible = true
    }

    private fun detach(view: ComposeView) {
        view.disposeComposition()
        if (view.isAttachedToWindow) windowManager.removeView(view)
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
private fun SelectionFrame(block: SelectableTextBlock, onSelect: () -> Unit) {
    val label = when (block.status) {
        SelectionStatus.RECOGNIZED -> "Перевести: ${block.source.text}"
        SelectionStatus.TRANSLATING -> "Переводим: ${block.source.text}"
        SelectionStatus.TRANSLATED -> "Перевод: ${block.translation}"
        SelectionStatus.ERROR -> "${block.error ?: "Ошибка перевода"}. Нажмите, чтобы повторить: ${block.source.text}"
    }
    val textColor = Color(0xFF252525)
    val surface = Color(0xFFFAFAFA)
    when (block.status) {
        SelectionStatus.TRANSLATED -> {
            val measurer = rememberTextMeasurer()
            BoxWithConstraints(Modifier.fillMaxSize().background(surface)
                .semantics { contentDescription = label }) {
                val density = androidx.compose.ui.platform.LocalDensity.current
                val width = with(density) { (maxWidth - 4.dp).roundToPx().coerceAtLeast(1) }
                val height = with(density) { (maxHeight - 4.dp).roundToPx().coerceAtLeast(1) }
                val output = block.translation.orEmpty()
                // Fit ordinary reading text into the original OCR bounds. Scroll long
                // results rather than expanding the window over neighbouring content.
                var fontSize = 16
                while (fontSize > 12 && measurer.measure(output,
                    TextStyle(fontSize = fontSize.sp, lineHeight = (fontSize * 1.25f).sp),
                    constraints = Constraints(maxWidth = width)).size.height > height) fontSize--
                Text(output, Modifier.fillMaxSize().padding(2.dp)
                    .verticalScroll(rememberScrollState()), color = textColor,
                    fontSize = fontSize.sp, lineHeight = (fontSize * 1.25f).sp,
                    fontWeight = FontWeight.Normal)
            }
        }
        SelectionStatus.RECOGNIZED -> Box(Modifier.fillMaxSize()
            .border(0.75.dp, Color(0xFF929292))
            .semantics { contentDescription = label }
            .clickable(role = Role.Button, onClick = onSelect))
        else -> Box(Modifier.fillMaxSize().background(surface)
            .semantics { contentDescription = label }
            .clickable(role = Role.Button, enabled = block.status == SelectionStatus.ERROR, onClick = onSelect),
            contentAlignment = Alignment.Center) {
            Text(if (block.status == SelectionStatus.TRANSLATING) "…" else "${block.error ?: "Ошибка перевода"}. Нажмите, чтобы повторить",
                Modifier.padding(2.dp).verticalScroll(rememberScrollState()), color = textColor, fontSize = 12.sp)
        }
    }
}
