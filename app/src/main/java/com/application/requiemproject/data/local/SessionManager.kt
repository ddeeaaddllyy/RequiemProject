package com.application.requiemproject.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "app_session",
        Context.MODE_PRIVATE
    )

    companion object {
        const val KEY_USER_ID = "current_user_id"
        private const val KEY_LOGIN = "current_user_login"
    }

    fun saveSession(userId: Long, login: String? = null) {
        val editor = prefs.edit().putLong(KEY_USER_ID, userId)
        if (login != null) editor.putString(KEY_LOGIN, login)
        check(editor.commit()) { "Unable to persist account session" }
    }

    fun getUserId(): Long {
        // Older installations may have stored the ID as an Int or a String.
        return when (val value = prefs.all[KEY_USER_ID]) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: -1L
            else -> -1L
        }
    }

    fun getLogin(): String? = prefs.getString(KEY_LOGIN, null)

    fun clearSession() {
        check(prefs.edit().remove(KEY_USER_ID).remove(KEY_LOGIN).commit()) { "Unable to clear account session" }
    }

    fun isLoggedIn(): Boolean {
        return getUserId() != -1L
    }
}
