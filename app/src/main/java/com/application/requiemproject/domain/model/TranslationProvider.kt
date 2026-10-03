package com.application.requiemproject.domain.model

enum class TranslationProvider(val label: String, val defaultModel: String = "") {
    MYMEMORY("MyMemory"),
    OPENAI("ChatGPT / OpenAI", "gpt-4.1-mini"),
    GEMINI("Gemini", "gemini-3.5-flash-lite"),
    CLAUDE("Claude", "claude-haiku-4-5"),
    CUSTOM("Другой сервис") ;

    val requiresKey get() = this != MYMEMORY
}

data class ProviderConfiguration(val apiKey: String = "", val model: String = "", val baseUrl: String = "") {
    override fun toString() = "ProviderConfiguration(apiKey=<redacted>, model=$model, baseUrl=$baseUrl)"
}
