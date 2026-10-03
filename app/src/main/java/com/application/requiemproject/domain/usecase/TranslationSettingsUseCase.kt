package com.application.requiemproject.domain.usecase

import com.application.requiemproject.domain.model.AppLanguage
import com.application.requiemproject.domain.model.ScanSource
import com.application.requiemproject.domain.repository.SettingsRepository

class TranslationSettingsUseCase(private val repository: SettingsRepository) {
    val settings get() = repository.settings
    fun selectLanguage(language: AppLanguage, source: Boolean) {
        val current = settings.value
        repository.save(if (source) current.copy(sourceLanguage = language) else current.copy(targetLanguage = language))
    }
    fun swapLanguages() {
        val current = settings.value
        repository.save(current.copy(sourceLanguage = current.targetLanguage, targetLanguage = current.sourceLanguage))
    }
    fun selectScanSource(source: ScanSource) = repository.save(settings.value.copy(scanSource = source))
}
