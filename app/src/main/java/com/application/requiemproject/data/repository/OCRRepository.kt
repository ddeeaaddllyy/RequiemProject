package com.application.requiemproject.data.repository

import android.graphics.Bitmap
import com.application.requiemproject.domain.model.TextBlock
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.android.gms.tasks.Tasks
import com.application.requiemproject.domain.model.TextBounds
import android.util.Log
import com.application.requiemproject.data.platform.TagSet.OCR_REPOSITORY_TAG

open class OCRRepository {
    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    open fun recognizeText(bitmap: Bitmap, scale: Float, yOffset: Int): List<TextBlock> {
        return try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val result = Tasks.await(recognizer.process(inputImage))
            val characterList = mutableListOf<TextBlock>()

            for (block in result.textBlocks) {
                val box = block.boundingBox ?: continue

                val correctedBox = TextBounds(
                    (box.left / scale).toInt(),
                    ((box.top / scale) + yOffset).toInt(),
                    (box.right / scale).toInt(),
                    ((box.bottom / scale) + yOffset).toInt()
                )

                characterList.add(
                    TextBlock(
                        text = block.text,
                        boundingBox = correctedBox
                    )
                )
            }
            characterList
        } catch (e: Exception) {
            Log.e(OCR_REPOSITORY_TAG, "Recognize Text Exception: $e")
            e.printStackTrace()
            emptyList()
        }
    }

}
