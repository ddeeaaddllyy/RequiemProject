package com.application.requiemproject

import androidx.test.platform.app.InstrumentationRegistry
import com.application.requiemproject.data.api.MyMemoryTranslationApi
import com.application.requiemproject.data.api.response.*
import com.application.requiemproject.data.local.SessionManager
import com.application.requiemproject.data.local.dao.UserDao
import com.application.requiemproject.data.translator.MyMemoryTranslator
import com.application.requiemproject.domain.model.TranslationResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.koin.core.context.GlobalContext
import retrofit2.Response

class MyMemoryTranslatorTest {
    private class FakeApi : MyMemoryTranslationApi {
        val requests = mutableListOf<String>()
        var answer = TranslationResponse(ResponseData("Читай &amp; отдыхай"), 200)
        override suspend fun getTranslateText(text: String, langPair: String, email: String?): Response<TranslationResponse> {
            requests += text
            return Response.success(answer)
        }
    }

    private fun translator(api: FakeApi) = MyMemoryTranslator(api, GlobalContext.get().get<UserDao>(),
        SessionManager(InstrumentationRegistry.getInstrumentation().targetContext))

    @Test fun normalizesLineWrapsDecodesEntitiesAndTranslatesLongBlocksInFull() = runBlocking {
        val api = FakeApi()
        val translator = translator(api)
        assertEquals(TranslationResult.Success("Читай & отдыхай"), translator.translate("Take your\ntime", "en|ru"))
        assertEquals(listOf("Take your time"), api.requests)
        api.requests.clear()
        val longText = List(100) { "Read this sentence." }.joinToString(" ")
        assertTrue(translator.translate(longText, "en|ru") is TranslationResult.Success)
        assertTrue(api.requests.size > 1)
        assertTrue(api.requests.all { it.toByteArray(Charsets.UTF_8).size <= 500 })
        assertEquals(longText, api.requests.joinToString(" "))
    }

    @Test fun serviceErrorsInsideHttpSuccessAreNeverDisplayedAsTranslations() = runBlocking {
        val api = FakeApi().apply { answer = TranslationResponse(ResponseData("QUERY LENGTH LIMIT EXCEEDED"), 403, "Too long") }
        val translator = translator(api)
        assertTrue(translator.translate("Hello", "en|ru") is TranslationResult.Error)
        api.answer = TranslationResponse(ResponseData("Quota exceeded"), 200, quotaFinished = true)
        assertTrue(translator.translate("Hello", "en|ru") is TranslationResult.Error)
    }
}
