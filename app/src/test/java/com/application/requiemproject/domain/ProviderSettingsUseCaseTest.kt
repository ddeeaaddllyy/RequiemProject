package com.application.requiemproject.domain

import com.application.requiemproject.domain.model.*
import com.application.requiemproject.domain.repository.ProviderConfigurationRepository
import com.application.requiemproject.domain.usecase.ProviderSettingsUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class ProviderSettingsUseCaseTest {
    private class Store : ProviderConfigurationRepository {
        val values = mutableMapOf<TranslationProvider, ProviderConfiguration>()
        override suspend fun read(provider: TranslationProvider) = values[provider] ?: ProviderConfiguration(model = provider.defaultModel)
        override suspend fun save(provider: TranslationProvider, configuration: ProviderConfiguration) { values[provider] = configuration }
    }

    @Test fun changingModelKeepsSavedKeyAndRemovingKeyAffectsOnlyOneProvider() = runTest {
        val store = Store()
        val useCase = ProviderSettingsUseCase(store)
        useCase.save(TranslationProvider.OPENAI, "gpt-4.1-mini", "openai-test-key", "")
        useCase.save(TranslationProvider.GEMINI, "gemini-3.5-flash-lite", "gemini-test-key", "")
        useCase.save(TranslationProvider.OPENAI, "another-model", "", "")
        assertEquals("openai-test-key", store.read(TranslationProvider.OPENAI).apiKey)
        useCase.removeKey(TranslationProvider.OPENAI)
        assertEquals("", store.read(TranslationProvider.OPENAI).apiKey)
        assertEquals("gemini-test-key", store.read(TranslationProvider.GEMINI).apiKey)
    }

    @Test fun emptyKeysAndInsecureOrCredentialBearingUrlsCannotBeSaved() = runTest {
        val store = Store()
        val useCase = ProviderSettingsUseCase(store)
        assertTrue(runCatching { useCase.save(TranslationProvider.OPENAI, "model", "", "") }.exceptionOrNull() is IllegalArgumentException)
        for (url in listOf("http://example.com/v1", "https://secret@example.com/v1", "https://example.com/v1?key=secret")) {
            assertTrue(runCatching { useCase.save(TranslationProvider.CUSTOM, "model", "key", url) }.exceptionOrNull() is IllegalArgumentException)
        }
        assertTrue(store.values.isEmpty())
        useCase.save(TranslationProvider.CUSTOM, "model", "key", "https://example.com/v1/")
        assertEquals("https://example.com/v1", store.read(TranslationProvider.CUSTOM).baseUrl)
    }
}
