package com.application.requiemproject

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.graphics.asAndroidBitmap
import android.graphics.Bitmap
import java.io.File
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import android.os.ParcelFileDescriptor
import com.application.requiemproject.presentation.MainActivity
import org.junit.Rule
import org.junit.Test

class ComposeNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun guestCanChooseLanguageSearchHelpAndOpenProfile() {
        compose.waitUntil(10_000) {
            compose.onAllNodes(hasText("Продолжить без аккаунта →") and isEnabled()).fetchSemanticsNodes().isNotEmpty()
        }
        screenshot("01-auth")
        compose.onNodeWithText("Продолжить без аккаунта →").performScrollTo().performClick()
        screenshot("02-home")
        compose.onNodeWithText("Английский").performScrollTo().performClick()
        screenshot("03-languages")
        compose.onNodeWithText("Японский").performClick()
        compose.onNodeWithText("Японский").assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Японский").assertIsDisplayed()
        compose.onNodeWithText("Справка").performClick()
        compose.onNodeWithText("Поиск по досье").performTextInput("снимки")
        Espresso.closeSoftKeyboard()
        compose.onNodeWithText("Где хранятся снимки экрана?").performScrollTo().performClick()
        compose.onNodeWithText("Кадры обрабатываются", substring = true).assertExists()
        screenshot("04-help")
        compose.onNodeWithText("Профиль").performClick()
        compose.onNodeWithText("Свободный агент").assertIsDisplayed()
        screenshot("05-profile")
    }

    @Test fun registrationProfileEditingAndReturningLoginWork() {
        val login = "Rebel${System.currentTimeMillis() % 100000}"
        compose.waitUntil(10_000) {
            compose.onAllNodes(hasText("Нет профиля? Создать") and isEnabled()).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Нет профиля? Создать").performScrollTo().performClick()
        screenshot("06-register")
        compose.onNodeWithText("Логин").performTextInput(login)
        compose.onNodeWithText("Пароль").performTextInput("secret1")
        Espresso.closeSoftKeyboard()
        compose.onNodeWithText("СОЗДАТЬ ПРОФИЛЬ").performScrollTo().performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Профиль").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Профиль").performClick()
        compose.onNodeWithText(login).assertIsDisplayed()
        compose.onNodeWithText("Настройки профиля").performScrollTo().performClick()
        compose.onAllNodesWithText("Email").onLast().performTextInput("rebel@example.com")
        Espresso.closeSoftKeyboard()
        screenshot("07-settings")
        compose.onNodeWithText("СОХРАНИТЬ").performScrollTo().performClick()
        compose.onNodeWithText("rebel@example.com").assertExists()
        screenshot("08-profile-member")
        compose.onNodeWithText("Выйти из профиля").performScrollTo().performClick()
        compose.onNodeWithText("Выйти").performClick()
        compose.onNodeWithText("Логин").performTextInput(login)
        compose.onNodeWithText("Пароль").performTextInput("secret1")
        Espresso.closeSoftKeyboard()
        compose.onNodeWithText("ВОЙТИ В ИГРУ").performScrollTo().performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Профиль").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Профиль").performClick()
        compose.onNodeWithText("rebel@example.com").assertExists()
        compose.onNodeWithText("Выйти из профиля").performScrollTo().performClick()
        compose.onNodeWithText("Выйти").performClick()
    }

    private fun screenshot(name: String) {
        val directory = File(compose.activity.getExternalFilesDir(null), "ui-review").apply { mkdirs() }
        val file = File(directory, "$name.png")
        file.outputStream().use {
            compose.onAllNodes(isRoot()).onLast().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        // Keep review artifacts when Gradle uninstalls the test application.
        listOf(
            "mkdir -p /sdcard/Download/requiem-ui-review",
            "cp ${file.absolutePath} /sdcard/Download/requiem-ui-review/$name.png"
        ).forEach { command ->
            ParcelFileDescriptor.AutoCloseInputStream(InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command))
                .use { it.readBytes() }
        }
    }
}
