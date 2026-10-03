package com.application.requiemproject.domain.usecase

import com.application.requiemproject.domain.repository.HelpRepository

class SearchHelpUseCase(private val repository: HelpRepository) {
    operator fun invoke(query: String, category: String) = repository.articles().filter {
        (category == "Все" || it.category == category) &&
            (it.title.contains(query.trim(), true) || it.answer.contains(query.trim(), true))
    }
}
