package com.application.requiemproject.domain.usecase

import com.application.requiemproject.domain.repository.AccountRepository

class AccountUseCase(private val repository: AccountRepository) {
    suspend fun current() = repository.current()

    suspend fun authenticate(login: String, password: String, register: Boolean) = run {
        require(login.isNotBlank() && password.isNotBlank()) { "Заполните логин и пароль" }
        if (register) {
            require(login.trim().length >= 5) { "Логин: минимум 5 символов" }
            require(password.length >= 6 && password.any(Char::isDigit)) {
                "Пароль: минимум 6 символов и одна цифра"
            }
            requireNotNull(repository.register(login.trim(), password)) { "Этот логин уже занят" }
        } else {
            requireNotNull(repository.signIn(login.trim(), password)) { "Неверный логин или пароль" }
        }
    }

    suspend fun updateEmail(email: String) {
        require(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email.trim())) {
            "Введите корректный email"
        }
        repository.updateEmail(email.trim())
    }

    fun signOut() = repository.signOut()
}
