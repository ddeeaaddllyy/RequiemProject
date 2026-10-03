package com.application.requiemproject.presentation

import androidx.lifecycle.SavedStateHandle
import com.application.requiemproject.domain.model.*
import com.application.requiemproject.domain.repository.AccountRepository
import com.application.requiemproject.domain.repository.SettingsRepository
import com.application.requiemproject.domain.repository.ProviderConfigurationRepository
import com.application.requiemproject.domain.usecase.*
import com.application.requiemproject.presentation.account.AccountViewModel
import com.application.requiemproject.presentation.home.HomeViewModel
import com.application.requiemproject.presentation.navigation.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelsTest {
    @Before fun prepare() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun cleanup() { Dispatchers.resetMain() }

    @Test fun selectionClosesPickerAndPersistsCorrectSide() {
        val repository = object : SettingsRepository {
            override val settings = MutableStateFlow(TranslationSettings(AppLanguage.ENGLISH, AppLanguage.RUSSIAN, ScanSource.OCR))
            override fun save(settings: TranslationSettings) { this.settings.value = settings }
        }
        val credentials = object : ProviderConfigurationRepository {
            override suspend fun read(provider: TranslationProvider) = ProviderConfiguration(model = provider.defaultModel)
            override suspend fun save(provider: TranslationProvider, configuration: ProviderConfiguration) = Unit
        }
        val viewModel = HomeViewModel(TranslationSettingsUseCase(repository), SavedStateHandle(), ProviderSettingsUseCase(credentials))
        viewModel.openLanguages(false)
        viewModel.selectLanguage(AppLanguage.JAPANESE)
        assertNull(viewModel.languagePicker.value)
        assertEquals("en|ja", viewModel.settings.value.languagePair)
    }

    @Test fun navigationRestoresSelectedDestination() {
        val saved = SavedStateHandle(mapOf("destination" to Destination.PROFILE))
        val viewModel = NavigationViewModel(saved)
        assertEquals(Destination.PROFILE, viewModel.destination.value)
        viewModel.navigate(Destination.HELP)
        assertEquals(Destination.HELP, saved.get<Destination>("destination"))
    }

    @Test fun providerEditorSavesSelectionButNeverPutsKeyInSavedState() = runTest {
        val repository = object : SettingsRepository {
            override val settings = MutableStateFlow(TranslationSettings(AppLanguage.ENGLISH, AppLanguage.RUSSIAN, ScanSource.OCR))
            override fun save(settings: TranslationSettings) { this.settings.value = settings }
        }
        val credentials = object : ProviderConfigurationRepository {
            var configuration = ProviderConfiguration(model = "gpt-4.1-mini")
            override suspend fun read(provider: TranslationProvider) = configuration
            override suspend fun save(provider: TranslationProvider, configuration: ProviderConfiguration) { this.configuration = configuration }
        }
        val saved = SavedStateHandle()
        val viewModel = HomeViewModel(TranslationSettingsUseCase(repository), saved, ProviderSettingsUseCase(credentials))
        viewModel.selectProvider(TranslationProvider.OPENAI)
        advanceUntilIdle()
        viewModel.providerKey("only-test-key")
        viewModel.saveProvider()
        advanceUntilIdle()
        assertEquals(TranslationProvider.OPENAI, repository.settings.value.provider)
        assertEquals("only-test-key", credentials.configuration.apiKey)
        assertFalse(viewModel.providerEditor.value.visible)
        assertEquals("", viewModel.providerEditor.value.key)
        assertTrue(saved.keys().none { saved.get<Any>(it)?.toString()?.contains("only-test-key") == true })
    }

    @Test fun failedLoginShowsErrorAndGuestEntryClearsPassword() = runTest {
        val repository = object : AccountRepository {
            override suspend fun current(): Account? = null
            override suspend fun signIn(login: String, password: String): Account? = null
            override suspend fun register(login: String, password: String): Account? = null
            override suspend fun updateEmail(email: String) = Unit
            override fun signOut() = Unit
        }
        val viewModel = AccountViewModel(AccountUseCase(repository))
        advanceUntilIdle()
        viewModel.login("rebel")
        viewModel.password("incorrect")
        viewModel.authenticate()
        advanceUntilIdle()
        assertFalse(viewModel.state.value.loading)
        assertNotNull(viewModel.state.value.error)
        assertNull(viewModel.state.value.account)
        viewModel.guest()
        assertTrue(viewModel.state.value.guest)
        assertEquals("", viewModel.state.value.password)
        assertNull(viewModel.state.value.error)
    }
}
