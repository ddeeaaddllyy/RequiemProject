package com.application.requiemproject.domain.model

data class TranslationSettings(
    val sourceLanguage: AppLanguage,
    val targetLanguage: AppLanguage,
    val scanSource: ScanSource,
    val provider: TranslationProvider = TranslationProvider.MYMEMORY
) {
    val languagePair: String
        get() = "${sourceLanguage.translationCode}|${targetLanguage.translationCode}"
}
