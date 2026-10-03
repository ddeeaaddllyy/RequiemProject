package com.application.requiemproject.domain.repository

import com.application.requiemproject.domain.model.TextBlock
import com.application.requiemproject.domain.model.SelectableTextBlock

/** Output port for translated and recognized text. */
interface TranslationOverlay {
    fun showOverlay()
    fun updateBlocks(blocks: List<SelectableTextBlock>)
    fun setOnBlockSelected(listener: ((TextBlock) -> Unit)?)
    fun setOnRefresh(listener: (() -> Unit)?)
    fun setVisible(visible: Boolean)
    fun removeOverlay()
}
