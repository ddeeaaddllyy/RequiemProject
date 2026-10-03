package com.application.requiemproject.data.translator

/** OCR line wraps are layout, not sentence boundaries. Keep paragraph context. */
object TranslationInput {
    fun paragraphs(text: String): List<String> = text.replace("\r\n", "\n")
        .split(Regex("\n\\s*\n"))
        .map { it.replace(Regex("\\s+"), " ").trim() }.filter { it.isNotEmpty() }

    fun segments(paragraph: String, maxBytes: Int = 500): List<String> {
        require(maxBytes >= 4)
        val result = mutableListOf<String>()
        var remaining = paragraph.trim()
        while (remaining.isNotEmpty()) {
            var end = 0
            var bytes = 0
            while (end < remaining.length) {
                val codePoint = remaining.codePointAt(end)
                val count = Character.charCount(codePoint)
                val size = remaining.substring(end, end + count).toByteArray(Charsets.UTF_8).size
                if (bytes + size > maxBytes) break
                bytes += size
                end += count
            }
            if (end < remaining.length) {
                val prefix = remaining.substring(0, end)
                val sentenceEnd = Regex("[.!?。！？]\\s+").findAll(prefix).lastOrNull()?.range?.last
                val wordEnd = prefix.lastIndexOf(' ')
                val boundary = sentenceEnd ?: wordEnd
                if (boundary > 0) end = boundary + 1
            }
            result += remaining.substring(0, end).trim()
            remaining = remaining.substring(end).trimStart()
        }
        return result
    }
}
