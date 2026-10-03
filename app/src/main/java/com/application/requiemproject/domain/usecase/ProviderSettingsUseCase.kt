package com.application.requiemproject.domain.usecase

import com.application.requiemproject.domain.model.*
import com.application.requiemproject.domain.repository.ProviderConfigurationRepository
import java.net.URI

class ProviderSettingsUseCase(private val repository: ProviderConfigurationRepository) {
    suspend fun read(provider: TranslationProvider) = repository.read(provider)
    suspend fun save(provider: TranslationProvider, model: String, key: String, baseUrl: String) {
        require(model.matches(Regex("[A-Za-z0-9][A-Za-z0-9._:/-]*"))) { "Укажите название модели" }
        if (provider == TranslationProvider.CUSTOM) {
            val uri = runCatching { URI(baseUrl) }.getOrNull()
            require(uri?.scheme == "https" && !uri.host.isNullOrBlank() && uri.userInfo == null && uri.query == null && uri.fragment == null) {
                "Укажите HTTPS-адрес API без ключа и параметров, например https://example.com/v1"
            }
        }
        val current = repository.read(provider)
        val apiKey = key.trim().ifBlank { current.apiKey }
        require(apiKey.isNotBlank()) { "Добавьте API-ключ" }
        require(!apiKey.any { it.isWhitespace() }) { "API-ключ не должен содержать пробелы" }
        repository.save(provider, ProviderConfiguration(apiKey, model.trim(), baseUrl.trim().trimEnd('/')))
    }
    suspend fun removeKey(provider: TranslationProvider) {
        repository.save(provider, repository.read(provider).copy(apiKey = ""))
    }
}
