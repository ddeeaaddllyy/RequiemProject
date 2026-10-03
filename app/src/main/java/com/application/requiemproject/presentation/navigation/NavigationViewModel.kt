package com.application.requiemproject.presentation.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

enum class Destination(val label: String, val index: String) {
    HOME("Перевод", "01"), HELP("Справка", "02"), PROFILE("Профиль", "03")
}

class NavigationViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    val destination = savedState.getStateFlow("destination", Destination.HOME)
    fun navigate(destination: Destination) { savedState["destination"] = destination }
}
