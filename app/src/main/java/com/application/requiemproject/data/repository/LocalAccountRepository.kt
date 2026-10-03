package com.application.requiemproject.data.repository

import com.application.requiemproject.data.local.SessionManager
import com.application.requiemproject.data.local.dao.UserDao
import com.application.requiemproject.data.local.entities.User
import com.application.requiemproject.domain.model.Account
import com.application.requiemproject.domain.repository.AccountRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalAccountRepository(private val dao: UserDao, private val session: SessionManager) : AccountRepository {
    private fun User.toAccount() = Account(id, login, email)
    override suspend fun current(): Account? = withContext(Dispatchers.IO) {
        val user = dao.getUserById(session.getUserId())
            ?: session.getLogin()?.let { dao.getUserByLogin(it) }
            ?: return@withContext null
        session.saveSession(user.id, user.login)
        user.toAccount()
    }
    override suspend fun signIn(login: String, password: String): Account? {
        val user = dao.getUserByLogin(login)?.takeIf { it.password == password } ?: return null
        withContext(Dispatchers.IO) { session.saveSession(user.id, user.login) }
        return user.toAccount()
    }
    override suspend fun register(login: String, password: String): Account? {
        if (dao.getUserByLogin(login) != null) return null
        val id = dao.insertUser(User(login = login, password = password, privilege = listOf("NEWCOMER!")))
        withContext(Dispatchers.IO) { session.saveSession(id, login) }
        return Account(id, login, null)
    }
    override suspend fun updateEmail(email: String) = dao.updateEmail(session.getUserId(), email)
    override fun signOut() = session.clearSession()
}
