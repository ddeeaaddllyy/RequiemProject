package com.application.requiemproject.presentation.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.requiemproject.domain.model.AppLanguage
import com.application.requiemproject.domain.model.ScanSource
import com.application.requiemproject.domain.usecase.TranslationSettingsUseCase
import com.application.requiemproject.domain.usecase.ProviderSettingsUseCase
import com.application.requiemproject.domain.model.TranslationProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class ProviderEditorState(val visible: Boolean = false, val provider: TranslationProvider = TranslationProvider.MYMEMORY,
    val loading: Boolean = false, val hasKey: Boolean = false, val key: String = "", val model: String = "", val baseUrl: String = "", val error: String? = null)

class HomeViewModel(private val settingsUseCase: TranslationSettingsUseCase, private val savedState: SavedStateHandle,
    private val providers: ProviderSettingsUseCase) : ViewModel() {
    val settings = settingsUseCase.settings
    val languagePicker = savedState.getStateFlow<String?>("language_picker", null)
    val captureMessage = savedState.getStateFlow<String?>("capture_message", null)
    private val editor = MutableStateFlow(ProviderEditorState())
    val providerEditor = editor.asStateFlow()
    private var editorJob: Job? = null
    private var prepareJob: Job? = null
    fun openProviders() = editProvider(settings.value.provider)
    fun selectProvider(provider: TranslationProvider) {
        settingsUseCase.selectProvider(provider)
        editProvider(provider)
    }
    private fun editProvider(provider: TranslationProvider) {
        editorJob?.cancel()
        editor.value = ProviderEditorState(visible = true, provider = provider, loading = provider.requiresKey, model = provider.defaultModel)
        if (!provider.requiresKey) return
        editorJob = viewModelScope.launch {
            try {
                val configuration = providers.read(provider)
                editor.value = editor.value.copy(loading = false, hasKey = configuration.apiKey.isNotBlank(),
                    model = configuration.model, baseUrl = configuration.baseUrl)
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { editor.update { it.copy(loading = false, error = "Не удалось открыть настройки сервиса") } }
        }
    }
    fun closeProviders() { editorJob?.cancel(); editor.value = ProviderEditorState() }
    fun providerKey(value: String) { editor.update { it.copy(key = value, error = null) } }
    fun providerModel(value: String) { editor.update { it.copy(model = value, error = null) } }
    fun providerUrl(value: String) { editor.update { it.copy(baseUrl = value, error = null) } }
    fun saveProvider() {
        val input = editor.value
        if (input.loading) return
        if (!input.provider.requiresKey) { closeProviders(); return }
        editor.update { it.copy(loading = true, error = null) }
        editorJob = viewModelScope.launch {
            try { providers.save(input.provider, input.model, input.key, input.baseUrl); closeProviders() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { editor.update { it.copy(loading = false,
                error = if (error is IllegalArgumentException) error.message else "Не удалось сохранить настройки сервиса") } }
        }
    }
    fun removeProviderKey() {
        val input = editor.value
        if (input.loading) return
        editor.update { it.copy(loading = true) }
        editorJob = viewModelScope.launch {
            try { providers.removeKey(input.provider); editor.update { it.copy(loading = false, key = "", hasKey = false) } }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { editor.update { it.copy(loading = false, error = "Не удалось удалить ключ") } }
        }
    }
    fun prepareCapture(onReady: () -> Unit) {
        if (prepareJob?.isActive == true) return
        prepareJob = viewModelScope.launch {
            val snapshot = settings.value
            try {
                if (snapshot.provider.requiresKey && snapshot.sourceLanguage != snapshot.targetLanguage && providers.read(snapshot.provider).apiKey.isBlank()) {
                    captureMessage("Добавьте API-ключ ${snapshot.provider.label}, чтобы начать перевод")
                    openProviders()
                } else onReady()
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { captureMessage("Не удалось прочитать настройки переводчика") }
        }
    }
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
