package com.application.requiemproject.data

import com.application.requiemproject.data.api.AiTranslationApi
import com.application.requiemproject.data.translator.*
import com.application.requiemproject.domain.model.*
import com.application.requiemproject.domain.repository.*
import com.google.gson.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response
import okhttp3.ResponseBody

class AiTranslatorTest {
    private class Api : AiTranslationApi {
        var url = ""
        var headers = emptyMap<String, String>()
        var body = JsonObject()
        var answer = """{"status":"completed","output":[{"type":"reasoning"},{"type":"message","content":[{"type":"output_text","text":"Привет, "},{"type":"output_text","text":"я люблю тебя"}]}]}"""
        var code = 200
        var gate: CompletableDeferred<Unit>? = null
        override suspend fun generate(url: String, headers: Map<String, String>, body: JsonObject): Response<JsonObject> {
            this.url = url; this.headers = headers; this.body = body
            gate?.await()
            return if (code == 200) Response.success(JsonParser().parse(answer).asJsonObject)
                else Response.error(code, ResponseBody.create(null, "Error"))
        }
    }
    private val settings = TranslationSettings(AppLanguage.ENGLISH, AppLanguage.RUSSIAN, ScanSource.OCR, TranslationProvider.OPENAI)
    private val configuration = ProviderConfiguration("test-key", "test-model", "https://example.com/v1")

    @Test fun openAiSendsOnlySelectedTextWithInstructionsAndCombinesTextOutputs() = runTest {
        val api = Api()
        assertEquals(TranslationResult.Success("Привет, я люблю тебя"), AiTranslator(api).translate("Hello,\nI love you", settings, configuration))
        assertEquals("https://api.openai.com/v1/responses", api.url)
        assertEquals("Bearer test-key", api.headers["Authorization"])
        assertEquals("Hello, I love you", api.body["input"].asString)
        assertFalse(api.body["store"].asBoolean)
        assertTrue(api.body["instructions"].asString.contains("Russian"))
    }

    @Test fun geminiUsesHeaderKeyAndExcludesThoughtsFromTranslation() = runTest {
        val api = Api().apply { answer = """{"candidates":[{"finishReason":"STOP","content":{"parts":[{"thought":true,"text":"private thought"},{"text":"Привет"}]}}]}""" }
        assertEquals(TranslationResult.Success("Привет"), AiTranslator(api).translate("Hello", settings.copy(provider = TranslationProvider.GEMINI), configuration))
        assertTrue(api.url.endsWith("models/test-model:generateContent"))
        assertFalse(api.url.contains("test-key"))
        assertEquals("test-key", api.headers["x-goog-api-key"])
    }

    @Test fun claudeAndCompatibleServersUseTheirOwnProtocol() = runTest {
        val api = Api().apply { answer = """{"stop_reason":"end_turn","content":[{"type":"text","text":"Привет"}]}""" }
        assertEquals(TranslationResult.Success("Привет"), AiTranslator(api).translate("Hello", settings.copy(provider = TranslationProvider.CLAUDE), configuration))
        assertEquals("https://api.anthropic.com/v1/messages", api.url)
        assertEquals("test-key", api.headers["x-api-key"])
        api.answer = """{"choices":[{"finish_reason":"stop","message":{"content":"Привет"}}]}"""
        assertEquals(TranslationResult.Success("Привет"), AiTranslator(api).translate("Hello", settings.copy(provider = TranslationProvider.CUSTOM), configuration))
        assertEquals("https://example.com/v1/chat/completions", api.url)
    }

    @Test fun errorsMissingKeysAndIncompleteResultsAreNotSuccessfulTranslations() = runTest {
        val api = Api()
        val translator = AiTranslator(api)
        assertTrue(translator.translate("Hello", settings, configuration.copy(apiKey = "")) is TranslationResult.Error)
        assertEquals("", api.url)
        api.code = 429
        assertTrue(translator.translate("Hello", settings, configuration) is TranslationResult.Error)
        api.code = 200; api.answer = """{"status":"incomplete","output":[]}"""
        assertTrue(translator.translate("Hello", settings, configuration) is TranslationResult.Error)
    }

    @Test fun cancellationIsPropagatedAndNeverConvertedToProviderError() = runTest {
        val api = Api().apply { gate = CompletableDeferred() }
        val request = async { AiTranslator(api).translate("Hello", settings, configuration) }
        yield(); request.cancel()
        assertTrue(runCatching { request.await() }.exceptionOrNull() is CancellationException)
    }

    @Test fun routingUsesSettingsSnapshotAndNeverFallsBackToAnotherProvider() = runTest {
        var myMemoryCalls = 0
        val myMemory = object : TranslatorModel {
            override suspend fun translate(text: String, languages: String): TranslationResult {
                myMemoryCalls++; return TranslationResult.Success("MyMemory")
            }
        }
        val credentials = object : ProviderConfigurationRepository {
            override suspend fun read(provider: TranslationProvider) = configuration.copy(apiKey = "")
            override suspend fun save(provider: TranslationProvider, configuration: ProviderConfiguration) = Unit
        }
        val repository = object : SettingsRepository {
            override val settings = kotlinx.coroutines.flow.MutableStateFlow(this@AiTranslatorTest.settings.copy(provider = TranslationProvider.MYMEMORY))
            override fun save(settings: TranslationSettings) { this.settings.value = settings }
        }
        val router = ProviderTranslator(myMemory, AiTranslator(Api()), credentials, repository)
        assertTrue(router.translate("Hello", settings) is TranslationResult.Error)
        assertEquals(0, myMemoryCalls)
        assertEquals(TranslationResult.Success("MyMemory"), router.translate("Hello", repository.settings.value))
        assertEquals(1, myMemoryCalls)
    }
}
