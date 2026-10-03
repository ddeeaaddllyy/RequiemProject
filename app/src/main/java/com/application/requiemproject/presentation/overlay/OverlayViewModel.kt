package com.application.requiemproject.presentation.overlay

import androidx.lifecycle.ViewModel
import com.application.requiemproject.domain.model.SelectableTextBlock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class OverlayUiState(val blocks: List<SelectableTextBlock> = emptyList())

class OverlayViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(OverlayUiState())
    val state = mutableState.asStateFlow()
    fun blocks(blocks: List<SelectableTextBlock>) { mutableState.update { it.copy(blocks = blocks) } }
}
