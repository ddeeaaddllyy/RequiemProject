package com.application.requiemproject

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.application.requiemproject.data.repository.EncryptedProviderConfigurationRepository
import com.application.requiemproject.data.repository.TranslationSettingsRepository
import com.application.requiemproject.domain.model.*
import com.application.requiemproject.domain.repository.ProviderConfigurationRepository
import com.application.requiemproject.presentation.MainActivity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.koin.core.context.GlobalContext
import java.io.File
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.graphics.Bitmap

class ProviderSettingsTest {
    val compose = createAndroidComposeRule<MainActivity>()
    @get:Rule val rules: RuleChain = RuleChain.outerRule(SignedOutRule()).around(compose)

    @Test fun providerConfigurationIsEncryptedAndCanBeReadByNewRepository() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = GlobalContext.get().get<ProviderConfigurationRepository>()
        val original = repository.read(TranslationProvider.CUSTOM)
        try {
            val config = ProviderConfiguration("only-test-secret-123", "test-model", "https://example.com/v1")
            repository.save(TranslationProvider.CUSTOM, config)
            assertEquals(config, EncryptedProviderConfigurationRepository(context).read(TranslationProvider.CUSTOM))
            val bytes = File(context.noBackupFilesDir, "translation-providers/CUSTOM.bin").readBytes()
            assertFalse(String(bytes, Charsets.ISO_8859_1).contains(config.apiKey))
            assertFalse(config.toString().contains(config.apiKey))
        } finally { repository.save(TranslationProvider.CUSTOM, original) }
    }

    @Test fun userCanChooseProviderSaveKeyAndRestoreSelectionAfterRecreation() {
        val settings = GlobalContext.get().get<TranslationSettingsRepository>()
        val repository = GlobalContext.get().get<ProviderConfigurationRepository>()
        val originalSettings = settings.getSettings()
        val originalKey = runBlocking { repository.read(TranslationProvider.OPENAI) }
        try {
            compose.waitUntil(10_000) { compose.onAllNodes(hasText("Продолжить без аккаунта →") and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Продолжить без аккаунта →").performScrollTo().performClick()
            compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasContentDescription("Выбрать переводчика"))
            compose.onNodeWithContentDescription("Выбрать переводчика").performClick()
            compose.onNodeWithText("ChatGPT / OpenAI").performClick()
            compose.waitUntil(10_000) { compose.onAllNodes(hasText("API-ключ") and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
            saveScreenshot()
            compose.onNodeWithText("API-ключ").performScrollTo().performTextInput("only-test-secret-123")
            Espresso.closeSoftKeyboard()
            compose.onNodeWithText("СОХРАНИТЬ ПЕРЕВОДЧИКА").performScrollTo().performClick()
            compose.waitUntil(10_000) { settings.getSettings().provider == TranslationProvider.OPENAI &&
                compose.onAllNodesWithText("СОХРАНИТЬ ПЕРЕВОДЧИКА").fetchSemanticsNodes().isEmpty() }
            compose.activityRule.scenario.recreate()
            compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("ChatGPT / OpenAI"))
            compose.onNodeWithText("ChatGPT / OpenAI").assertIsDisplayed()
            val reopened = TranslationSettingsRepository(InstrumentationRegistry.getInstrumentation().targetContext)
            assertEquals(TranslationProvider.OPENAI, reopened.getSettings().provider)
            assertEquals("only-test-secret-123", runBlocking { repository.read(TranslationProvider.OPENAI) }.apiKey)
        } finally {
            settings.save(originalSettings)
            runBlocking { repository.save(TranslationProvider.OPENAI, originalKey) }
        }
    }

    private fun saveScreenshot() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        SystemClock.sleep(250)
        val bitmap = instrumentation.uiAutomation.takeScreenshot() ?: return
        val file = File(instrumentation.targetContext.getExternalFilesDir(null), "10-provider-settings.png")
        try { file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) } }
        finally { bitmap.recycle() }
        listOf("mkdir -p /sdcard/Download/requiem-ui-review",
            "cp ${file.absolutePath} /sdcard/Download/requiem-ui-review/10-provider-settings.png").forEach { command ->
            ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(command)).use { it.readBytes() }
        }
    }
}
