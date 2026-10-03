package com.application.requiemproject.domain

import com.application.requiemproject.data.repository.LocalHelpRepository
import com.application.requiemproject.domain.model.*
import com.application.requiemproject.domain.repository.*
import com.application.requiemproject.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class UseCasesTest {
    @Test fun swappingLanguagesPreservesRecognitionMode() {
        val repository = object : SettingsRepository {
            override val settings = MutableStateFlow(TranslationSettings(AppLanguage.JAPANESE, AppLanguage.RUSSIAN, ScanSource.ACCESSIBILITY))
            override fun save(settings: TranslationSettings) { this.settings.value = settings }
        }
        val useCase = TranslationSettingsUseCase(repository)
        useCase.swapLanguages()
        assertEquals("ru|ja", repository.settings.value.languagePair)
        assertEquals(ScanSource.ACCESSIBILITY, repository.settings.value.scanSource)
        useCase.selectLanguage(AppLanguage.GERMAN, source = false)
        assertEquals("ru|de", repository.settings.value.languagePair)
    }

    @Test fun helpSearchCombinesCategoryAndCaseInsensitiveTrimmedQuery() {
        val search = SearchHelpUseCase(LocalHelpRepository())
        assertEquals(listOf(4), search("  СНИМКИ  ", "Приватность").map { it.id })
        assertTrue(search("снимки", "Начало").isEmpty())
        assertEquals(6, search("", "Все").size)
        assertTrue(search("нет такого запроса", "Все").isEmpty())
    }

    @Test fun invalidRegistrationNeverReachesRepository() = runTest {
        val repository = FakeAccountRepository()
        val useCase = AccountUseCase(repository)
        try {
            useCase.authenticate("ab", "123", true)
            fail("Invalid registration must fail")
        } catch (_: IllegalArgumentException) { }
        assertEquals(0, repository.registrations)
        assertEquals("rebel", useCase.authenticate(" rebel ", "secret1", true).name)
        assertEquals(1, repository.registrations)
    }

    @Test fun passwordWhitespaceIsPreserved() = runTest {
        val repository = FakeAccountRepository()
        AccountUseCase(repository).authenticate(" rebel ", " secret1 ", true)
        assertEquals(" secret1 ", repository.password)
    }

    @Test fun translationWithSameLanguageDoesNotCallNetwork() = runTest {
        val translator = object : TranslatorModel {
            override suspend fun translate(text: String, languages: String): TranslationResult = error("Network must not be called")
        }
        val blocks = listOf(TextBlock("Hello", TextBounds(1, 2, 30, 40)))
        assertEquals(blocks, TranslateBlocksUseCase(translator).translateBlocks(blocks,
            TranslationSettings(AppLanguage.ENGLISH, AppLanguage.ENGLISH, ScanSource.OCR)))
    }

    @Test fun mergingRemovesOverlappingOcrAndBlankText() {
        val accessibility = TextBlock("Hello", TextBounds(0, 0, 50, 50))
        val overlapping = TextBlock("Hello OCR", TextBounds(20, 20, 60, 60))
        val separate = TextBlock("World", TextBounds(70, 70, 100, 100))
        assertEquals(listOf(accessibility, separate), MergeText.mergeAndFilter(listOf(accessibility), listOf(overlapping, separate, TextBlock("  ", null))))
    }
}

private class FakeAccountRepository : AccountRepository {
    var registrations = 0
    var password = ""
    override suspend fun current(): Account? = null
    override suspend fun signIn(login: String, password: String): Account? = null
    override suspend fun register(login: String, password: String): Account {
        registrations++
        this.password = password
        return Account(1, login, null)
    }
    override suspend fun updateEmail(email: String) = Unit
    override fun signOut() = Unit
}
