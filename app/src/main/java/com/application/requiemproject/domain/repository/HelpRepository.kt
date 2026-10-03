package com.application.requiemproject.domain.repository

import com.application.requiemproject.domain.model.HelpArticle

interface HelpRepository {
    fun articles(): List<HelpArticle>
}
