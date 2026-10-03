package com.application.requiemproject.presentation.overlay

import androidx.lifecycle.ViewModel
import com.application.requiemproject.domain.model.TextBlock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class OverlayUiState(val translations: List<TextBlock> = emptyList(), val accessibility: List<TextBlock> = emptyList())

class OverlayViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(OverlayUiState())
    val state = mutableState.asStateFlow()
    fun translations(blocks: List<TextBlock>) { mutableState.update { it.copy(translations = blocks) } }
    fun accessibility(blocks: List<TextBlock>) { mutableState.update { it.copy(accessibility = blocks) } }
}
