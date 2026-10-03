package com.application.requiemproject.domain.usecase


import com.application.requiemproject.domain.model.TextBlock

object MergeText {
    private val textRegex = Regex("[\\p{L}\\p{N}]")

    fun filterValidBlocks(blocks: List<TextBlock>): List<TextBlock> {
        return blocks.filter { it.text.isNotBlank() && textRegex.containsMatchIn(it.text) }
    }

    fun mergeAndFilter(
        accBlocks: List<TextBlock>,
        ocrBlocks: List<TextBlock>
    ): List<TextBlock> {
        val validAcc = filterValidBlocks(accBlocks)
        val validOcr = filterValidBlocks(ocrBlocks)
        val result = validAcc.toMutableList()

        for (ocrBlock in validOcr) {
            val ocrRect = ocrBlock.boundingBox ?: continue

            val isOverlapping = validAcc.any { accBlock ->
                val accRect = accBlock.boundingBox ?: return@any false
                ocrRect.intersects(accRect)
            }

            if (!isOverlapping) {
                result.add(ocrBlock)
            }
        }

        return result
    }
}
