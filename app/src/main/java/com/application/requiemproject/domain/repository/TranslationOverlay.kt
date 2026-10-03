package com.application.requiemproject.domain.repository

import com.application.requiemproject.domain.model.TextBlock

/** Output port for translated and recognized text. */
interface TranslationOverlay {
    fun showOverlay()
    fun updateTextOnScreen(rects: List<TextBlock>)
    fun updateAccessibilityOverlay(rects: List<TextBlock>)
    fun removeOverlay()
}
