package com.application.requiemproject.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Ink = Color(0xFF0D0D10)
val Panel = Color(0xFF1B1B20)
val Paper = Color(0xFFF4F0E8)
val Red = Color(0xFFF32040)
val Muted = Color(0xFFA5A3AD)
val Gold = Color(0xFFEAC977)

@Composable
fun RequiemTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(primary = Red, onPrimary = Paper, background = Ink,
            onBackground = Paper, surface = Panel, onSurface = Paper, secondary = Gold,
            onSecondary = Ink, outline = Muted, error = Color(0xFFFF8090)),
        typography = Typography(
            headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black, fontSize = 40.sp, lineHeight = 42.sp, letterSpacing = (-1.5).sp),
            headlineMedium = TextStyle(fontWeight = FontWeight.Black, fontSize = 28.sp, lineHeight = 32.sp),
            titleLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 21.sp),
            bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
            bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
            labelSmall = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp, letterSpacing = 1.5.sp)
        )
    ) {
        CompositionLocalProvider(LocalContentColor provides Paper, content = content)
    }
}
