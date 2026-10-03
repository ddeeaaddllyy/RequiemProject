package com.application.requiemproject.domain.repository

import com.application.requiemproject.domain.model.TranslationSettings
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val settings: StateFlow<TranslationSettings>
    fun save(settings: TranslationSettings)
}
