package com.application.requiemproject.domain.repository

import com.application.requiemproject.domain.model.*

interface ProviderConfigurationRepository {
    suspend fun read(provider: TranslationProvider): ProviderConfiguration
    suspend fun save(provider: TranslationProvider, configuration: ProviderConfiguration)
}
