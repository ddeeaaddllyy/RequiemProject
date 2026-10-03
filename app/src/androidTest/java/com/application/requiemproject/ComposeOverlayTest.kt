package com.application.requiemproject

import android.os.ParcelFileDescriptor
import android.provider.Settings
import androidx.test.platform.app.InstrumentationRegistry
import com.application.requiemproject.domain.model.TextBlock
import com.application.requiemproject.domain.model.TextBounds
import com.application.requiemproject.presentation.overlay.OverlayManager
import org.junit.Assert.assertTrue
import org.junit.Test

class ComposeOverlayTest {
    @Test fun overlayCanAttachRenderDisposeAndReattach() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        fun permission(mode: String) {
            ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(
                "appops set ${context.packageName} SYSTEM_ALERT_WINDOW $mode"
            )).use { it.readBytes() }
        }
        permission("allow")
        try {
            assertTrue(Settings.canDrawOverlays(context))
            val overlay = OverlayManager(context)
            try {
                repeat(2) {
                    instrumentation.runOnMainSync {
                        overlay.showOverlay()
                        overlay.updateTextOnScreen(listOf(TextBlock("Перевод", TextBounds(30, 150, 350, 220))))
                        overlay.updateAccessibilityOverlay(listOf(TextBlock("Original", TextBounds(30, 150, 350, 220))))
                    }
                    instrumentation.waitForIdleSync()
                    instrumentation.runOnMainSync { overlay.removeOverlay() }
                    instrumentation.waitForIdleSync()
                }
            } finally {
                instrumentation.runOnMainSync { overlay.removeOverlay() }
            }
        } finally {
            permission("default")
        }
    }
}
