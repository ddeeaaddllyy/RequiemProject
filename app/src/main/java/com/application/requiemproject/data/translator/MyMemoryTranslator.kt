package com.application.requiemproject.data.translator

import android.util.Log
import androidx.core.text.HtmlCompat
import com.application.requiemproject.data.api.MyMemoryTranslationApi
import com.application.requiemproject.domain.model.TranslationResult
import com.application.requiemproject.data.local.dao.UserDao
import com.application.requiemproject.data.local.SessionManager
import com.application.requiemproject.domain.model.TranslatorModel
import kotlinx.coroutines.CancellationException

/**
 * Implementation of [TranslatorModel] that uses the MyMemory translation API.
 *
 * This translator performs network requests to fetch translated text.
 * It supports dynamic language selection via a language pair string.
 *
 * Example of language pair: "en|ru" (English -> Russian)
 *
 * @property api Retrofit API used to communicate with MyMemory service.
 * @property userDao from [com.application.requiemproject.data.local.db.AppDatabase] used to MyMemory API for more tokens for translate
 * @property sessionManager from [SessionManager] used to get user id.
 */
class MyMemoryTranslator(
    private val api: MyMemoryTranslationApi,
    private val userDao: UserDao,
    private val sessionManager: SessionManager
): TranslatorModel {
    /**
     * Translates given [text] using MyMemory API.
     *
     * @param text Text to translate.
     * @param languages Language pair in format "source|target"
     * (e.g., "en|ru").
     *
     * @return [TranslationResult.Success] with translated text if successful,
     * or [TranslationResult.Error] if something went wrong.
     *
     * Possible failure reasons:
     * - Network error
     * - API error response
     * - Empty or invalid response body
     */
    override suspend fun translate(text: String, languages: String): TranslationResult {
        return try {
            val userId = sessionManager.getUserId()
            val email = userDao.getEmailById(userId)
            val paragraphs = TranslationInput.paragraphs(text)
            if (paragraphs.isEmpty()) return TranslationResult.Error("Empty input")
            val translatedParagraphs = mutableListOf<String>()
            for (paragraph in paragraphs) {
                val translatedSegments = mutableListOf<String>()
                for (segment in TranslationInput.segments(paragraph)) {
                    val response = api.getTranslateText(segment, languages, email)

                    if (!response.isSuccessful) {
                        return TranslationResult.Error(response.errorBody()?.string() ?: "Unknown api error")
                    }
                    val body = response.body()
                    if (body == null || body.quotaFinished || body.responseStatus?.let { it != 200 } == true) {
                        return TranslationResult.Error(body?.responseDetails?.takeIf { it.isNotBlank() }
                            ?: "Translation service unavailable")
                    }
                    val translatedText = body.responseData?.translatedText
                    if (translatedText.isNullOrBlank()) return TranslationResult.Error("Empty response")
                    val decoded = HtmlCompat.fromHtml(translatedText, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
                    if (decoded.isBlank()) return TranslationResult.Error("Empty response")
                    translatedSegments += decoded
                }
                translatedParagraphs += translatedSegments.joinToString(" ")
            }
            TranslationResult.Success(translatedParagraphs.joinToString("\n\n"))

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("MyMemoryTranslator", "Exception during translation", e)
            TranslationResult.Error(
                message = e.message ?: "Unknown error"
            )
        }
    }
}
