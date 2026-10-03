package com.application.requiemproject.domain.usecase

import com.application.requiemproject.domain.model.TranslationResult
import com.application.requiemproject.domain.model.TextBlock
import com.application.requiemproject.domain.model.TranslatorModel
import com.application.requiemproject.domain.model.TranslationSettings
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Repository responsible for translating text blocks using current translator.
 */
class TranslateBlocksUseCase(
    private val currentTranslator: TranslatorModel
) {

    /**
     * Translates a list of [TextBlock] objects concurrently.
     *
     * @param blocks List of text blocks to translate.
     * @return List of translated text blocks.
     */
    suspend fun translateBlocks(
        blocks: List<TextBlock>,
        settings: TranslationSettings
    ): List<TextBlock> = coroutineScope {
        if (settings.sourceLanguage == settings.targetLanguage) {
            return@coroutineScope blocks
        }

        blocks.map { blocks ->
            async {
                val result = currentTranslator.translate(blocks.text, settings)
                val outputText = when (result) {
                    is TranslationResult.Success -> result.text
                    is TranslationResult.Error -> blocks.text
                }

                blocks.copy(text = outputText)
            }
        }.awaitAll()
    }

}
