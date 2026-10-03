package com.application.requiemproject.domain.model

enum class SelectionStatus { RECOGNIZED, TRANSLATING, TRANSLATED, ERROR }

data class SelectableTextBlock(
    val source: TextBlock,
    val status: SelectionStatus = SelectionStatus.RECOGNIZED,
    val translation: String? = null,
    val requestId: Long = 0,
    val error: String? = null
)
