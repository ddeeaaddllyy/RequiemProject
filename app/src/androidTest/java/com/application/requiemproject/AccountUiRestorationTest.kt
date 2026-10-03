package com.application.requiemproject

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.application.requiemproject.data.local.SessionManager
import com.application.requiemproject.data.local.dao.UserDao
import com.application.requiemproject.domain.repository.AccountRepository
import com.application.requiemproject.presentation.MainActivity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.GlobalContext

class AccountUiRestorationTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun newActivityRestoresRegisteredProfileAndCanSignInWithoutRegisteringAgain() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferences = context.getSharedPreferences("app_session", Context.MODE_PRIVATE)
        val original = preferences.all.toMap()
        val accounts = GlobalContext.get().get<AccountRepository>()
        val dao = GlobalContext.get().get<UserDao>()
        val name = "ColdRebel${System.nanoTime()}"
        val account = runBlocking { accounts.register(name, "secret1")!! }
        try {
            repeat(2) {
                ActivityScenario.launch(MainActivity::class.java).use {
                    compose.waitUntil(10_000) { compose.onAllNodesWithText("Профиль").fetchSemanticsNodes().isNotEmpty() }
                    compose.onNodeWithText("Профиль").performClick()
                    compose.onNodeWithText(name).assertIsDisplayed()
                    compose.onNodeWithText("Продолжить без аккаунта →").assertDoesNotExist()
                }
            }
            accounts.signOut()
            ActivityScenario.launch(MainActivity::class.java).use {
                compose.waitUntil(10_000) { compose.onAllNodes(hasText("ВОЙТИ В ИГРУ") and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
                compose.onNodeWithText("Логин").performScrollTo().performTextInput(name.lowercase())
                compose.onNodeWithText("Пароль").performTextInput("secret1")
                Espresso.closeSoftKeyboard()
                compose.onNodeWithText("ВОЙТИ В ИГРУ").performScrollTo().performClick()
                compose.waitUntil(10_000) { compose.onAllNodesWithText("Профиль").fetchSemanticsNodes().isNotEmpty() }
                compose.onNodeWithText("Профиль").performClick()
                compose.onNodeWithText(name).assertIsDisplayed()
                assertEquals(account, runBlocking { accounts.current() })
            }
        } finally {
            runBlocking { dao.getUserById(account.id)?.let { dao.deleteUser(it) } }
            SessionManager(context).clearSession()
            val editor = preferences.edit()
            original.forEach { (key, value) -> when (value) {
                is Long -> editor.putLong(key, value)
                is Int -> editor.putInt(key, value)
                is String -> editor.putString(key, value)
                is Boolean -> editor.putBoolean(key, value)
            } }
            editor.commit()
        }
    }
}
