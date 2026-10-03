package com.application.requiemproject.domain.repository

import com.application.requiemproject.domain.model.Account

interface AccountRepository {
    suspend fun current(): Account?
    suspend fun signIn(login: String, password: String): Account?
    suspend fun register(login: String, password: String): Account?
    suspend fun updateEmail(email: String)
    fun signOut()
}
