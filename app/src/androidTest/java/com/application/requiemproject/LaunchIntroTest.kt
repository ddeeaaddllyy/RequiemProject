package com.application.requiemproject

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.size
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.test.platform.app.InstrumentationRegistry
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.compose.*
import com.application.requiemproject.presentation.LaunchIntro
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import android.graphics.Bitmap
import java.io.File

class LaunchIntroTest {
    @get:Rule val compose = createComposeRule()

    @Test fun bundledAnimationParsesAndRendersWithoutNetwork() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val result = LottieCompositionFactory.fromRawResSync(context, R.raw.requiem_launch)
        assertNull(result.exception)
        val composition = requireNotNull(result.value)
        assertEquals(1800f, composition.duration, 1f)
        assertEquals(6, composition.layers.size)
        assertTrue(composition.images.isEmpty())
        compose.setContent {
            LottieAnimation(composition, progress = { .65f }, modifier = Modifier.size(320.dp))
        }
        val screenshot = compose.onRoot().captureToImage().asAndroidBitmap()
        val file = File(context.getExternalFilesDir(null), "launch-intro.png")
        file.outputStream().use { screenshot.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun introFinishesAndRevealsContent() {
        compose.setContent { LaunchIntro { Text("Ready to translate") } }
        compose.waitUntil(6000) {
            compose.onAllNodesWithText("Ready to translate").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("launch-intro").assertDoesNotExist()
        compose.onNodeWithText("Ready to translate").assertIsDisplayed()
    }

    @Test fun skippedIntroDoesNotReplayAfterStateRestoration() {
        compose.mainClock.autoAdvance = false
        val restoration = StateRestorationTester(compose)
        restoration.setContent { LaunchIntro { Text("Ready to translate") } }
        compose.onNodeWithText("Пропустить →").performClick()
        compose.onNodeWithText("Ready to translate").assertIsDisplayed()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithTag("launch-intro").assertDoesNotExist()
        compose.onNodeWithText("Ready to translate").assertIsDisplayed()
    }
}
