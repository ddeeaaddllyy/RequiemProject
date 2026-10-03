package com.application.requiemproject.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.application.requiemproject.presentation.theme.*

val SlashShape = GenericShape { size, _ ->
    moveTo(size.width * .035f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width * .965f, size.height)
    lineTo(0f, size.height)
    close()
}

val CallingCardShape = GenericShape { size, _ ->
    moveTo(0f, size.height * .06f)
    lineTo(size.width * .72f, 0f)
    lineTo(size.width * .76f, size.height * .04f)
    lineTo(size.width, size.height * .02f)
    lineTo(size.width, size.height)
    lineTo(0f, size.height)
    close()
}

@Composable
fun CallingCardHeader(kicker: String, title: String) {
    Box(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        RequiemInsignia(Modifier.align(Alignment.CenterEnd).size(88.dp), Gold.copy(alpha = .3f))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Eyebrow(kicker, Gold)
            Text(title, color = Ink, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic,
                fontSize = 26.sp, modifier = Modifier.graphicsLayer { rotationZ = -3f }
                    .clip(SlashShape).background(Paper).padding(horizontal = 18.dp, vertical = 8.dp))
        }
    }
}

@Composable
fun RequiemBackground(content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(Ink)) {
        Canvas(Modifier.matchParentSize()) {
            val step = 18.dp.toPx()
            for (x in 0..(size.width / step).toInt()) {
                for (y in 0..(size.height / step).toInt()) {
                    drawCircle(Paper.copy(alpha = .035f), 1.dp.toPx(), Offset(x * step, y * step))
                }
            }
            rotate(-19f, Offset(size.width, size.height * .35f)) {
                drawRect(Red.copy(alpha = .075f), Offset(size.width * .78f, -size.height), Size(size.width * .65f, size.height * 3))
            }
        }
        content()
    }
}

@Composable
fun Eyebrow(text: String, color: Color = Muted, modifier: Modifier = Modifier) {
    Text(text, modifier, style = MaterialTheme.typography.labelSmall, color = color)
}

@Composable
fun ScreenHeading(kicker: String, title: String, subtitle: String) {
    Column(Modifier.padding(top = 18.dp, bottom = 22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Eyebrow(kicker, Gold)
        Text(title, style = MaterialTheme.typography.headlineLarge, fontStyle = FontStyle.Italic,
            modifier = Modifier.graphicsLayer { rotationZ = -2f })
        Text(subtitle, color = Muted, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ActionButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .96f else 1f, spring(), label = "button press")
    Row(modifier.fillMaxWidth().heightIn(min = 60.dp).graphicsLayer { scaleX = scale; scaleY = scale }
        .clip(SlashShape).background(if (enabled) Red else Panel)
        .clickable(interactionSource = interaction, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
        .padding(horizontal = 24.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text, Modifier.weight(1f), color = if (enabled) Paper else Muted, fontWeight = FontWeight.Black, fontSize = 17.sp)
        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = if (enabled) Paper else Muted)
    }
}

@Composable
fun SectionLabel(number: String, title: String, end: String = "") {
    Row(Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(number, color = Red, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Spacer(Modifier.width(10.dp))
        Text(title, Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Eyebrow(end)
    }
}

/** Original geometric insignia, drawn in Compose, independent of image assets. */
@Composable
fun RequiemInsignia(modifier: Modifier = Modifier, color: Color = Paper) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val star = Path().apply {
            moveTo(w * .52f, 0f); lineTo(w * .61f, h * .32f)
            lineTo(w, h * .22f); lineTo(w * .74f, h * .52f)
            lineTo(w * .92f, h * .92f); lineTo(w * .54f, h * .74f)
            lineTo(w * .22f, h); lineTo(w * .26f, h * .62f)
            lineTo(0f, h * .42f); lineTo(w * .37f, h * .37f); close()
        }
        drawPath(star, color, style = Stroke(width = w * .035f))
        val bolt = Path().apply {
            moveTo(w * .52f, h * .2f); lineTo(w * .3f, h * .57f)
            lineTo(w * .5f, h * .54f); lineTo(w * .44f, h * .82f)
            lineTo(w * .71f, h * .4f); lineTo(w * .51f, h * .44f); close()
        }
        drawPath(bolt, color)
    }
}

@Composable
fun ErrorMessage(message: String?) {
    if (message != null) Text(message, Modifier.fillMaxWidth().padding(vertical = 12.dp), color = MaterialTheme.colorScheme.error)
}
