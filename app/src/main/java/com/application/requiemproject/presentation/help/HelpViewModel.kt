package com.application.requiemproject.presentation.help

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.requiemproject.domain.usecase.SearchHelpUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HelpViewModel(search: SearchHelpUseCase, private val savedState: SavedStateHandle) : ViewModel() {
    val query = savedState.getStateFlow("query", "")
    val category = savedState.getStateFlow("category", "Все")
    val expanded = savedState.getStateFlow<Int?>("expanded", null)
    val articles = combine(query, category) { query, category -> search(query, category) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), search("", "Все"))
    fun query(value: String) { savedState["query"] = value }
    fun category(value: String) { savedState["category"] = value }
    fun toggle(id: Int) { savedState["expanded"] = if (expanded.value == id) null else id }
}
