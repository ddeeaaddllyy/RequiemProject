package com.application.requiemproject.presentation.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.application.requiemproject.domain.model.AppLanguage
import com.application.requiemproject.domain.model.ScanSource
import com.application.requiemproject.domain.usecase.TranslationSettingsUseCase

class HomeViewModel(private val settingsUseCase: TranslationSettingsUseCase, private val savedState: SavedStateHandle) : ViewModel() {
    val settings = settingsUseCase.settings
    val languagePicker = savedState.getStateFlow<String?>("language_picker", null)
    val captureMessage = savedState.getStateFlow<String?>("capture_message", null)
    fun openLanguages(source: Boolean) { savedState["language_picker"] = if (source) "source" else "target" }
    fun closeLanguages() { savedState["language_picker"] = null }
    fun selectLanguage(language: AppLanguage) {
        settingsUseCase.selectLanguage(language, languagePicker.value == "source")
        closeLanguages()
    }
    fun swapLanguages() = settingsUseCase.swapLanguages()
    fun selectScanSource(source: ScanSource) = settingsUseCase.selectScanSource(source)
    fun captureMessage(message: String?) { savedState["capture_message"] = message }
}
