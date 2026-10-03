package com.application.requiemproject.data.translator

import com.application.requiemproject.domain.model.*
import com.application.requiemproject.domain.repository.*
import kotlinx.coroutines.CancellationException

class ProviderTranslator(private val myMemory: TranslatorModel, private val ai: AiTranslator,
    private val credentials: ProviderConfigurationRepository, private val settingsRepository: SettingsRepository) : TranslatorModel {
    override suspend fun translate(text: String, languages: String): TranslationResult {
        val pair = languages.split('|')
        val current = settingsRepository.settings.value
        val source = AppLanguage.entries.firstOrNull { it.translationCode == pair.getOrNull(0) } ?: return TranslationResult.Error("Unknown source language")
        val target = AppLanguage.entries.firstOrNull { it.translationCode == pair.getOrNull(1) } ?: return TranslationResult.Error("Unknown target language")
        return translate(text, current.copy(sourceLanguage = source, targetLanguage = target))
    }
    override suspend fun translate(text: String, settings: TranslationSettings): TranslationResult {
        if (settings.sourceLanguage == settings.targetLanguage) return TranslationResult.Success(text)
        if (settings.provider == TranslationProvider.MYMEMORY) return myMemory.translate(text, settings.languagePair)
        return try { ai.translate(text, settings, credentials.read(settings.provider)) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { TranslationResult.Error("Не удалось прочитать настройки ${settings.provider.label}") }
    }
}
