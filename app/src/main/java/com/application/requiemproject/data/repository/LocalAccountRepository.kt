package com.application.requiemproject.data.repository

import com.application.requiemproject.data.local.SessionManager
import com.application.requiemproject.data.local.dao.UserDao
import com.application.requiemproject.data.local.entities.User
import com.application.requiemproject.domain.model.Account
import com.application.requiemproject.domain.repository.AccountRepository

class LocalAccountRepository(private val dao: UserDao, private val session: SessionManager) : AccountRepository {
    private fun User.toAccount() = Account(id, login, email)
    override suspend fun current() = dao.getUserById(session.getUserId())?.toAccount()
    override suspend fun signIn(login: String, password: String): Account? {
        val user = dao.getUserByLogin(login)?.takeIf { it.password == password } ?: return null
        session.saveSession(user.id)
        return user.toAccount()
    }
    override suspend fun register(login: String, password: String): Account? {
        if (dao.getUserByLogin(login) != null) return null
        val id = dao.insertUser(User(login = login, password = password, privilege = listOf("NEWCOMER!")))
        session.saveSession(id)
        return Account(id, login, null)
    }
    override suspend fun updateEmail(email: String) = dao.updateEmail(session.getUserId(), email)
    override fun signOut() = session.clearSession()
}
