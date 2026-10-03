package com.application.requiemproject.data.repository

import android.content.Context
import androidx.core.content.edit
import com.application.requiemproject.domain.model.AppLanguage
import com.application.requiemproject.domain.model.ScanSource
import com.application.requiemproject.domain.model.TranslationSettings
import com.application.requiemproject.domain.model.TranslationProvider
import com.application.requiemproject.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TranslationSettingsRepository(context: Context) : SettingsRepository {
    private val preferences = context.getSharedPreferences("translation_settings", Context.MODE_PRIVATE)
    private val state = MutableStateFlow(readSettings())
    override val settings = state.asStateFlow()

    private fun readSettings() = TranslationSettings(
        AppLanguage.entries.firstOrNull { it.name == preferences.getString("source_language", null) } ?: AppLanguage.defaultSource,
        AppLanguage.entries.firstOrNull { it.name == preferences.getString("target_language", null) } ?: AppLanguage.defaultTarget,
        ScanSource.entries.firstOrNull { it.name == preferences.getString("scan_source", null) } ?: ScanSource.OCR,
        TranslationProvider.entries.firstOrNull { it.name == preferences.getString("translation_provider", null) } ?: TranslationProvider.MYMEMORY
    )
    fun getSettings() = settings.value
    override fun save(settings: TranslationSettings) {
        preferences.edit(commit = true) {
            putString("source_language", settings.sourceLanguage.name)
            putString("target_language", settings.targetLanguage.name)
            putString("scan_source", settings.scanSource.name)
            putString("translation_provider", settings.provider.name)
        }
        state.value = settings
    }
}
