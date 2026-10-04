package com.application.requiemproject

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.graphics.asAndroidBitmap
import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import androidx.test.platform.app.InstrumentationRegistry
import com.application.requiemproject.presentation.account.AuthenticationSuccess
import com.application.requiemproject.presentation.account.AuthenticationSuccessScreen
import com.application.requiemproject.presentation.theme.RequiemTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.File

class AuthenticationAnimationTest {
    @get:Rule val compose = createComposeRule()

    @Test fun loginCallingCardFinishesOnceAndRevealsTheApp() {
        compose.mainClock.autoAdvance = false
        var completions = 0
        compose.setContent {
            var show by remember { mutableStateOf(true) }
            RequiemTheme {
                if (show) AuthenticationSuccessScreen(AuthenticationSuccess.LOGIN, "Rebel", {
                    completions++
                    show = false
                }) else Text("Translation room")
            }
        }
        compose.mainClock.advanceTimeBy(350)
        compose.onNodeWithText("ДОСТУП\nОТКРЫТ.").assertIsDisplayed()
        compose.onNodeWithText("Rebel").assertIsDisplayed()
        screenshot("11-login-success")
        compose.mainClock.advanceTimeBy(1500)
        compose.onNodeWithTag("authentication-success").assertDoesNotExist()
        compose.onNodeWithText("Translation room").assertIsDisplayed()
        assertEquals(1, completions)
    }

    @Test fun registrationHasItsOwnCallingCardAndCanBeSkipped() {
        compose.mainClock.autoAdvance = false
        var completions = 0
        compose.setContent {
            var show by remember { mutableStateOf(true) }
            RequiemTheme {
                if (show) AuthenticationSuccessScreen(AuthenticationSuccess.REGISTRATION, "NewRebel", {
                    completions++
                    show = false
                }) else Text("Translation room")
            }
        }
        compose.mainClock.advanceTimeBy(350)
        compose.onNodeWithText("АЛЬТЕР ЭГО\nСОЗДАНО.").assertIsDisplayed()
        screenshot("12-registration-success")
        compose.onNodeWithText("Продолжить →").performClick()
        compose.mainClock.advanceTimeBy(32)
        compose.onNodeWithTag("authentication-success").assertDoesNotExist()
        compose.mainClock.advanceTimeBy(2000)
        assertEquals(1, completions)
    }

    private fun screenshot(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.getExternalFilesDir(null), "requiem-ui-review").apply { mkdirs() }
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val file = File(directory, "$name.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        // Preserve review images when Gradle uninstalls the app after device tests.
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        listOf("mkdir -p /sdcard/Download/requiem-ui-review",
            "cp ${file.absolutePath} /sdcard/Download/requiem-ui-review/$name.png").forEach { command ->
            ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(command)).use { it.readBytes() }
        }
    }
}
