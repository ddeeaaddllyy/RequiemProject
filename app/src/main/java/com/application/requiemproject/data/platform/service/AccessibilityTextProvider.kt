package com.application.requiemproject.data.platform.service

import com.application.requiemproject.domain.model.TextBlock
import kotlin.concurrent.Volatile

object AccessibilityTextProvider {
    @Volatile
    var latestBlocks: List<TextBlock> = emptyList()
}