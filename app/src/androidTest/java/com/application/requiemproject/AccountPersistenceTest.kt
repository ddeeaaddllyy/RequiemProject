package com.application.requiemproject

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.application.requiemproject.data.local.SessionManager
import com.application.requiemproject.data.local.db.AppDatabase
import com.application.requiemproject.data.repository.LocalAccountRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class AccountPersistenceTest {
    @Test fun registrationSurvivesDatabaseReopenAndSignOutDoesNotDeleteAccount() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferences = context.getSharedPreferences("app_session", Context.MODE_PRIVATE)
        val original = preferences.all.toMap()
        val name = "account-persistence-${System.nanoTime()}"
        var database = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
        try {
            val account = LocalAccountRepository(database.userDao(), SessionManager(context)).register("PersistentRebel", "secret1")!!
            assertTrue(account.id > 0)
            database.close()
            database = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
            val reopened = LocalAccountRepository(database.userDao(), SessionManager(context))
            assertEquals(account, reopened.current())
            // A restored legacy preference with Int ID should still open this profile.
            preferences.edit().putInt(SessionManager.KEY_USER_ID, account.id.toInt()).commit()
            assertEquals(account, reopened.current())
            // If a restored row ID changes, the saved login recovers the same account.
            preferences.edit().putLong(SessionManager.KEY_USER_ID, 999999).commit()
            assertEquals(account, reopened.current())
            reopened.signOut()
            assertNull(reopened.current())
            assertEquals(account, reopened.signIn("persistentrebel", "secret1"))
            assertNull(reopened.register("PersistentRebel", "another1"))
        } finally {
            database.close()
            context.deleteDatabase(name)
            val editor = preferences.edit().clear()
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
