package com.application.requiemproject.domain.usecase

import com.application.requiemproject.domain.model.*
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Recognition stays local. Only an explicit selection can call the translator. */
class SelectTextForTranslationUseCase(private val translator: TranslatorModel) {
    private val requests = AtomicLong()
    private val mutableBlocks = MutableStateFlow<List<SelectableTextBlock>>(emptyList())
    val blocks = mutableBlocks.asStateFlow()

    fun recognize(blocks: List<TextBlock>) {
        val valid = MergeText.filterValidBlocks(blocks).filter {
            it.boundingBox?.let { bounds -> bounds.width() > 0 && bounds.height() > 0 } == true
        }.distinct()
        mutableBlocks.update { previous ->
            valid.map { block -> previous.firstOrNull { it.source == block } ?: SelectableTextBlock(block) }
        }
    }

    fun clear() { mutableBlocks.value = emptyList() }

    suspend fun select(block: TextBlock, settings: TranslationSettings) {
        val requestId = requests.incrementAndGet()
        while (true) {
            val snapshot = mutableBlocks.value
            val entry = snapshot.firstOrNull { it.source == block } ?: return
            if (entry.status == SelectionStatus.TRANSLATING || entry.status == SelectionStatus.TRANSLATED) return
            val loading = snapshot.map {
                if (it.source == block) it.copy(status = SelectionStatus.TRANSLATING, requestId = requestId, error = null) else it
            }
            if (mutableBlocks.compareAndSet(snapshot, loading)) break
        }
        try {
            val result = if (settings.sourceLanguage == settings.targetLanguage) {
                TranslationResult.Success(block.text)
            } else translator.translate(block.text, settings)
            mutableBlocks.update { entries ->
                entries.map { entry ->
                    if (entry.source != block || entry.requestId != requestId) entry
                    else when (result) {
                        is TranslationResult.Success -> entry.copy(status = SelectionStatus.TRANSLATED, translation = result.text)
                        is TranslationResult.Error -> entry.copy(status = SelectionStatus.ERROR, error = result.message)
                    }
                }
            }
        } catch (cancelled: CancellationException) {
            finishFailedRequest(block, requestId, SelectionStatus.RECOGNIZED)
            throw cancelled
        } catch (_: Exception) {
            finishFailedRequest(block, requestId, SelectionStatus.ERROR)
        }
    }

    private fun finishFailedRequest(block: TextBlock, requestId: Long, status: SelectionStatus) {
        mutableBlocks.update { entries ->
            entries.map { if (it.source == block && it.requestId == requestId) it.copy(status = status) else it }
        }
    }
}
