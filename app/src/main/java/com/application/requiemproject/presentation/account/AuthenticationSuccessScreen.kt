package com.application.requiemproject.presentation.account

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.requiemproject.presentation.components.Eyebrow
import com.application.requiemproject.presentation.components.RequiemInsignia
import com.application.requiemproject.presentation.components.SlashShape
import com.application.requiemproject.presentation.theme.*

/** A one-shot calling card after explicit authentication, never on session restore. */
@Composable
fun AuthenticationSuccessScreen(
    success: AuthenticationSuccess,
    name: String,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = remember(success) { Animatable(0f) }
    val finish by rememberUpdatedState(onFinished)
    LaunchedEffect(success) {
        // Compose honours the system animator duration scale, including disabled animations.
        progress.animateTo(1f, tween(1400, easing = LinearEasing))
        finish()
    }
    val entrance = (progress.value / .22f).coerceIn(0f, 1f)
    val exit = ((progress.value - .82f) / .18f).coerceIn(0f, 1f)
    Box(modifier.testTag("authentication-success").background(Ink), contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val left = size.width * (-1.4f + progress.value * 3.8f)
            val slash = Path().apply {
                moveTo(left, 0f)
                lineTo(left + size.width * .7f, 0f)
                lineTo(left + size.width * .2f, size.height)
                lineTo(left - size.width * .5f, size.height)
                close()
            }
            drawPath(slash, Red.copy(alpha = .28f))
        }
        Column(
            Modifier.widthIn(max = 540.dp).fillMaxWidth().padding(28.dp).verticalScroll(rememberScrollState())
                .graphicsLayer {
                    alpha = entrance * (1f - exit)
                    translationX = ((1f - entrance) * 90f - exit * 60f).dp.toPx()
                    rotationZ = -4f + entrance * 2f
                    scaleX = .92f + entrance * .08f
                    scaleY = scaleX
                }
                .semantics { liveRegion = LiveRegionMode.Polite },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            RequiemInsignia(Modifier.size(108.dp), Gold)
            Eyebrow(if (success == AuthenticationSuccess.REGISTRATION) "NEW REBEL / NEW CHAPTER" else "WELCOME BACK / THE REBEL", Gold)
            Text(
                if (success == AuthenticationSuccess.REGISTRATION) "АЛЬТЕР ЭГО\nСОЗДАНО." else "ДОСТУП\nОТКРЫТ.",
                Modifier.fillMaxWidth().clip(SlashShape).background(Paper).padding(22.dp),
                color = Ink, style = MaterialTheme.typography.headlineLarge,
                fontStyle = FontStyle.Italic, fontWeight = FontWeight.Black
            )
            Text(name, color = Paper, fontWeight = FontWeight.Bold, fontSize = 22.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Eyebrow("TAKE YOUR TIME. MAKE IT YOURS.", Muted)
            TextButton(onClick = onFinished) {
                Text("Продолжить →", color = Paper)
            }
        }
    }
}
