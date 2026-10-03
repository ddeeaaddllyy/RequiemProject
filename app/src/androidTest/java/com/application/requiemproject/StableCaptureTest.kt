package com.application.requiemproject

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.application.requiemproject.data.platform.service.ScreenCaptureService
import com.application.requiemproject.data.repository.OCRRepository
import com.application.requiemproject.data.repository.TranslationSettingsRepository
import com.application.requiemproject.di.appModule
import com.application.requiemproject.domain.model.*
import com.application.requiemproject.presentation.MainActivity
import org.junit.Assert.*
import org.junit.Rule
import org.junit.rules.RuleChain
import org.junit.Test
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import java.util.concurrent.atomic.AtomicInteger

class StableCaptureTest {
    val compose = createAndroidComposeRule<MainActivity>()
    @get:Rule val rules: RuleChain = RuleChain.outerRule(SignedOutRule()).around(compose)

    @Test fun captureKeepsTranslationVisibleAndOnlyRescansWhenRefreshIsPressed() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val calls = AtomicInteger()
        val fakeOcr = object : OCRRepository() {
            override fun recognizeText(bitmap: Bitmap, scale: Float, yOffset: Int): List<TextBlock> {
                check(scale == 1f && yOffset == 0) { "OCR must receive native screen coordinates" }
                val number = calls.incrementAndGet()
                return listOf(TextBlock(if (number == 1) "Stable text" else "Next screen", TextBounds(80, 400, 640, 560)))
            }
        }
        val settings = GlobalContext.get().get<TranslationSettingsRepository>()
        val originalSettings = settings.getSettings()
        val overrides = module { factory<OCRRepository> { fakeOcr } }
        loadKoinModules(overrides)
        settings.save(TranslationSettings(AppLanguage.ENGLISH, AppLanguage.ENGLISH, ScanSource.OCR))
        shell("appops set ${context.packageName} SYSTEM_ALERT_WINDOW allow")
        if (Build.VERSION.SDK_INT >= 33) shell("pm grant ${context.packageName} android.permission.POST_NOTIFICATIONS")
        try {
            compose.waitUntil(10_000) {
                compose.onAllNodes(hasText("Продолжить без аккаунта →") and isEnabled()).fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText("Продолжить без аккаунта →").performScrollTo().performClick()
            compose.onNodeWithText("НАЧАТЬ ПЕРЕВОД").performClick()
            var consent: AccessibilityNodeInfo? = null
            compose.waitUntil(15_000) {
                consent = findConsent(instrumentation.uiAutomation.rootInActiveWindow)
                consent != null
            }
            assertTrue(consent!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            compose.waitUntil(15_000) {
                compose.onAllNodesWithContentDescription("Перевести: Stable text").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithContentDescription("Перевести: Stable text").performClick()
            compose.onNodeWithContentDescription("Перевод: Stable text").assertExists()
            // Real MediaProjection remains active while UI and time change underneath it.
            SystemClock.sleep(3500)
            assertEquals(1, calls.get())
            compose.onNodeWithContentDescription("Перевод: Stable text").assertExists()
            compose.onNodeWithText("Обновить").performClick()
            compose.waitUntil(15_000) {
                compose.onAllNodesWithContentDescription("Перевести: Next screen").fetchSemanticsNodes().isNotEmpty()
            }
            SystemClock.sleep(2000)
            assertEquals(2, calls.get())
            compose.onNodeWithContentDescription("Перевод: Stable text").assertDoesNotExist()
        } finally {
            instrumentation.runOnMainSync { context.stopService(Intent(context, ScreenCaptureService::class.java)) }
            settings.save(originalSettings)
            loadKoinModules(appModule)
            shell("appops set ${context.packageName} SYSTEM_ALERT_WINDOW default")
        }
    }

    private fun findConsent(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        val text = node.text?.toString().orEmpty()
        if (node.isClickable && node.isEnabled && text in listOf("Share screen", "Start now", "Начать", "Начать сейчас")) return node
        for (index in 0 until node.childCount) findConsent(node.getChild(index))?.let { return it }
        return null
    }

    private fun shell(command: String) {
        ParcelFileDescriptor.AutoCloseInputStream(InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command))
            .use { it.readBytes() }
    }
}
