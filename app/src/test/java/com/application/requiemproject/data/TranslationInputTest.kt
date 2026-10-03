package com.application.requiemproject.data

import com.application.requiemproject.data.translator.TranslationInput
import org.junit.Assert.*
import org.junit.Test

class TranslationInputTest {
    @Test fun wrappedSentencesKeepContextAndParagraphsStaySeparate() {
        assertEquals(listOf("Take your time to read this.", "Next paragraph."),
            TranslationInput.paragraphs("Take your time\nto read this.\n\nNext   paragraph."))
    }

    @Test fun russianLineWrapDoesNotBreakThePhraseSentToTranslator() {
        assertEquals(listOf("Привет, я люблю тебя"), TranslationInput.paragraphs("Привет, я люблю тебя"))
        assertEquals(listOf("привет я люблю тебя"), TranslationInput.paragraphs("привет\nя люблю тебя"))
    }

    @Test fun longUnicodeParagraphsStayWithinByteLimitWithoutLosingWords() {
        val paragraph = List(150) { "Привет 🌍." }.joinToString(" ")
        val segments = TranslationInput.segments(paragraph)
        assertTrue(segments.size > 1)
        assertTrue(segments.all { it.toByteArray(Charsets.UTF_8).size <= 500 })
        assertEquals(paragraph, segments.joinToString(" "))
    }

    @Test fun unbrokenUnicodeTextDoesNotSplitSurrogatePairs() {
        val text = "🌍".repeat(300)
        val segments = TranslationInput.segments(text)
        assertTrue(segments.all { it.toByteArray(Charsets.UTF_8).size <= 500 })
        assertEquals(text, segments.joinToString(""))
    }
}
