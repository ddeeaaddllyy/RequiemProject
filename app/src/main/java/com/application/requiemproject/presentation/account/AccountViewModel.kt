package com.application.requiemproject.presentation.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.requiemproject.domain.model.Account
import com.application.requiemproject.domain.usecase.AccountUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountUiState(
    val account: Account? = null,
    val loading: Boolean = true,
    val guest: Boolean = false,
    val login: String = "",
    val password: String = "",
    val register: Boolean = false,
    val email: String = "",
    val editing: Boolean = false,
    val error: String? = null
)

class AccountViewModel(private val accounts: AccountUseCase) : ViewModel() {
    private val mutableState = MutableStateFlow(AccountUiState())
    val state = mutableState.asStateFlow()
    init {
        viewModelScope.launch {
            try {
                val account = accounts.current()
                mutableState.update { it.copy(account = account, email = account?.email.orEmpty(), loading = false) }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { mutableState.update { it.copy(loading = false, error = "Не удалось открыть аккаунт. Попробуйте ещё раз.") } }
        }
    }
    fun login(value: String) { mutableState.update { it.copy(login = value, error = null) } }
    fun password(value: String) { mutableState.update { it.copy(password = value, error = null) } }
    fun email(value: String) { mutableState.update { it.copy(email = value, error = null) } }
    fun toggleRegistration() { mutableState.update { it.copy(register = !it.register, error = null, password = "") } }
    fun guest() { mutableState.update { it.copy(guest = true, password = "", error = null) } }
    fun showLogin() { mutableState.update { it.copy(guest = false, error = null) } }
    fun editProfile(edit: Boolean) { mutableState.update { it.copy(editing = edit, email = it.account?.email.orEmpty(), error = null) } }
    fun authenticate() {
        if (state.value.loading) return
        val input = state.value
        mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val account = accounts.authenticate(input.login, input.password, input.register)
                mutableState.update { it.copy(account = account, loading = false, guest = false, password = "", email = account.email.orEmpty()) }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { mutableState.update { it.copy(loading = false, error = if (error is IllegalArgumentException) error.message else "Не удалось войти. Попробуйте ещё раз.") } }
        }
    }
    fun saveProfile() {
        if (state.value.loading) return
        val email = state.value.email
        mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                accounts.updateEmail(email)
                mutableState.update { it.copy(account = it.account?.copy(email = email.trim()), loading = false, editing = false) }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { mutableState.update { it.copy(loading = false, error = if (error is IllegalArgumentException) error.message else "Не удалось сохранить профиль") } }
        }
    }
    fun signOut() {
        accounts.signOut()
        mutableState.value = AccountUiState(loading = false)
    }
}
