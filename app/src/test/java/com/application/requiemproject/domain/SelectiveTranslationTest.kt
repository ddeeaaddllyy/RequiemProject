package com.application.requiemproject.domain

import com.application.requiemproject.domain.model.*
import com.application.requiemproject.domain.usecase.SelectTextForTranslationUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SelectiveTranslationTest {
    private val first = TextBlock("Hello rebel\nTake your time", TextBounds(10, 100, 250, 200))
    private val second = TextBlock("Other text", TextBounds(10, 300, 250, 350))
    private val settings = TranslationSettings(AppLanguage.ENGLISH, AppLanguage.RUSSIAN, ScanSource.OCR)
    private class FakeTranslator : TranslatorModel {
        val calls = mutableListOf<Pair<String, String>>()
        var answer: TranslationResult = TranslationResult.Success("Привет")
        var gate: CompletableDeferred<Unit>? = null
        override suspend fun translate(text: String, languages: String): TranslationResult {
            calls.add(text to languages)
            gate?.await()
            return answer
        }
    }

    @Test fun recognitionNeverSendsTextAndSelectionTranslatesOnlyItsEntireBlock() = runTest {
        val translator = FakeTranslator()
        val session = SelectTextForTranslationUseCase(translator)
        session.recognize(listOf(first, second))
        assertTrue(translator.calls.isEmpty())
        session.select(first, settings)
        assertEquals(listOf(first.text to "en|ru"), translator.calls)
        assertEquals(SelectionStatus.TRANSLATED, session.blocks.value[0].status)
        assertEquals("Привет", session.blocks.value[0].translation)
        assertEquals(SelectionStatus.RECOGNIZED, session.blocks.value[1].status)
        session.recognize(listOf(first, second))
        session.select(first, settings)
        assertEquals(1, translator.calls.size)
        assertEquals("Привет", session.blocks.value[0].translation)
    }

    @Test fun duplicateTapsDuringRequestDoNotSendAnotherRequest() = runTest {
        val translator = FakeTranslator().apply { gate = CompletableDeferred() }
        val session = SelectTextForTranslationUseCase(translator)
        session.recognize(listOf(first))
        val request = launch { session.select(first, settings) }
        runCurrent()
        assertEquals(SelectionStatus.TRANSLATING, session.blocks.value.single().status)
        session.select(first, settings)
        assertEquals(1, translator.calls.size)
        translator.gate!!.complete(Unit)
        request.join()
    }

    @Test fun disappearedAndReappearedBlockDoesNotReceiveOldResult() = runTest {
        val translator = FakeTranslator().apply { gate = CompletableDeferred() }
        val session = SelectTextForTranslationUseCase(translator)
        session.recognize(listOf(first))
        val request = launch { session.select(first, settings) }
        runCurrent()
        session.recognize(emptyList())
        session.recognize(listOf(first))
        translator.gate!!.complete(Unit)
        request.join()
        assertEquals(SelectionStatus.RECOGNIZED, session.blocks.value.single().status)
        assertNull(session.blocks.value.single().translation)
    }

    @Test fun changedTextAtSamePositionRequiresNewSelection() = runTest {
        val translator = FakeTranslator().apply { gate = CompletableDeferred() }
        val session = SelectTextForTranslationUseCase(translator)
        session.recognize(listOf(first))
        val request = launch { session.select(first, settings) }
        runCurrent()
        val changed = first.copy(text = "Next dialogue")
        session.recognize(listOf(changed))
        translator.gate!!.complete(Unit)
        request.join()
        assertEquals(changed, session.blocks.value.single().source)
        assertEquals(SelectionStatus.RECOGNIZED, session.blocks.value.single().status)
        session.select(first, settings)
        assertEquals(1, translator.calls.size)
    }

    @Test fun failedTranslationCanBeRetriedBySelection() = runTest {
        val translator = FakeTranslator().apply { answer = TranslationResult.Error("Offline") }
        val session = SelectTextForTranslationUseCase(translator)
        session.recognize(listOf(first))
        session.select(first, settings)
        assertEquals(SelectionStatus.ERROR, session.blocks.value.single().status)
        translator.answer = TranslationResult.Success("Снова в сети")
        session.select(first, settings)
        assertEquals(2, translator.calls.size)
        assertEquals("Снова в сети", session.blocks.value.single().translation)
    }

    @Test fun cancellationAllowsRetryAndClearDiscardsPendingResults() = runTest {
        val translator = FakeTranslator().apply { gate = CompletableDeferred() }
        val session = SelectTextForTranslationUseCase(translator)
        session.recognize(listOf(first))
        val request = launch { session.select(first, settings) }
        runCurrent()
        request.cancel()
        request.join()
        assertEquals(SelectionStatus.RECOGNIZED, session.blocks.value.single().status)
        val retry = launch { session.select(first, settings) }
        runCurrent()
        session.clear()
        translator.gate!!.complete(Unit)
        retry.join()
        assertTrue(session.blocks.value.isEmpty())
    }

    @Test fun sameLanguageSelectionNeedsNoNetworkAndInvalidBoundsHaveNoFrames() = runTest {
        val translator = FakeTranslator()
        val session = SelectTextForTranslationUseCase(translator)
        session.recognize(listOf(first, first, TextBlock("Hidden", null), second.copy(boundingBox = TextBounds(10, 10, 10, 10))))
        assertEquals(1, session.blocks.value.size)
        session.select(first, settings.copy(targetLanguage = AppLanguage.ENGLISH))
        assertEquals(first.text, session.blocks.value.single().translation)
        assertTrue(translator.calls.isEmpty())
    }
}
