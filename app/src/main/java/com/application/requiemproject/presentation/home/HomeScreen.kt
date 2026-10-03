package com.application.requiemproject.presentation.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.requiemproject.domain.model.*
import com.application.requiemproject.presentation.components.*
import com.application.requiemproject.presentation.theme.*

fun AppLanguage.localizedName() = when (this) {
    AppLanguage.ENGLISH -> "Английский"
    AppLanguage.RUSSIAN -> "Русский"
    AppLanguage.JAPANESE -> "Японский"
    AppLanguage.GERMAN -> "Немецкий"
    AppLanguage.FRENCH -> "Французский"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    settings: TranslationSettings,
    picker: String?,
    message: String?,
    onOpenLanguage: (Boolean) -> Unit,
    onCloseLanguage: () -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSwap: () -> Unit,
    onScanSource: (ScanSource) -> Unit,
    onStart: () -> Unit,
    onAccessibilitySettings: () -> Unit,
    onHelp: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(start = 22.dp, end = 22.dp, bottom = 12.dp)) {
            item { ScreenHeading("01 / THE TRANSLATION ROOM", "СЛОМАЙ\nЯЗЫКОВОЙ БАРЬЕР.", "Твой экран. Твой язык. Твои правила.") }
            item { MissionCard() }
            item { SectionLabel("01", "Направление перевода", "LANGUAGES") }
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    LanguageCard(settings.sourceLanguage, "ИСХОДНЫЙ", Modifier.weight(1f)) { onOpenLanguage(true) }
                    IconButton(onClick = onSwap, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.SwapHoriz, "Поменять языки местами", tint = Red)
                    }
                    LanguageCard(settings.targetLanguage, "ПЕРЕВОД", Modifier.weight(1f)) { onOpenLanguage(false) }
                }
            }
            item { SectionLabel("02", "Способ распознавания", "SCAN MODE") }
            item {
                Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ScanOption("OCR", "Текст на экране", settings.scanSource == ScanSource.OCR, Modifier.weight(1f)) { onScanSource(ScanSource.OCR) }
                    ScanOption("ACCESSIBILITY", "Элементы интерфейса", settings.scanSource == ScanSource.ACCESSIBILITY, Modifier.weight(1f)) { onScanSource(ScanSource.ACCESSIBILITY) }
                }
            }
            if (settings.scanSource == ScanSource.ACCESSIBILITY) {
                item {
                    Column(Modifier.padding(top = 12.dp).background(Panel).padding(16.dp)) {
                        Text("Включите Requiem в специальных возможностях Android. Распознанный текст отправляется MyMemory для перевода.", style = MaterialTheme.typography.bodyMedium, color = Muted)
                        TextButton(onClick = onAccessibilitySettings) { Text("Открыть специальные возможности", color = Gold) }
                    }
                }
            }
            item {
                Row(Modifier.padding(top = 14.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Shield, null, Modifier.size(16.dp), tint = Muted)
                    Spacer(Modifier.width(8.dp))
                    Text("Захват начнётся после вашего разрешения. Текст обрабатывается сервисом MyMemory.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                }
            }
            item {
                TextButton(onClick = onHelp, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    Text("Первый раз здесь? Откройте досье →", color = Paper)
                }
            }
        }
        Column(Modifier.fillMaxWidth().background(Ink).padding(horizontal = 22.dp)) {
            if (message != null) Text(message, Modifier.padding(top = 10.dp), color = Gold, style = MaterialTheme.typography.bodyMedium)
            ActionButton("НАЧАТЬ ПЕРЕВОД", Modifier.padding(vertical = 10.dp), onClick = onStart)
        }
    }
    if (picker != null) {
        ModalBottomSheet(onDismissRequest = onCloseLanguage, containerColor = Ink, contentColor = Paper) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 28.dp)) {
                Eyebrow("CHOOSE YOUR LANGUAGE", Gold)
                Text(if (picker == "source") "С какого языка?" else "На какой язык?", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(vertical = 16.dp))
                Column(Modifier.selectableGroup()) {
                    AppLanguage.entries.forEach { language ->
                        val selected = language == if (picker == "source") settings.sourceLanguage else settings.targetLanguage
                        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(SlashShape)
                            .background(if (selected) Red else Panel)
                            .selectable(selected, role = Role.RadioButton, onClick = { onSelectLanguage(language) })
                            .padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(language.translationCode.uppercase(), Modifier.width(45.dp), fontWeight = FontWeight.Black)
                            Text(language.localizedName(), Modifier.weight(1f))
                            if (selected) Icon(Icons.Default.Check, null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionCard() {
    val transition = rememberInfiniteTransition(label = "insignia")
    val angle by transition.animateFloat(-7f, 5f, infiniteRepeatable(tween(3600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "insignia tilt")
    Box(Modifier.fillMaxWidth().clip(SlashShape).background(Red).padding(22.dp)) {
        RequiemInsignia(Modifier.align(Alignment.CenterEnd).size(132.dp).graphicsLayer { rotationZ = angle; alpha = .22f })
        Column(Modifier.fillMaxWidth(.78f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Eyebrow("OPERATION / REQUIEM", Paper)
            Text("МИР БЕЗ\nСУБТИТРОВ?", fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, fontSize = 27.sp, lineHeight = 29.sp)
            Text("Перепишем правила.", fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).background(Paper))
                Spacer(Modifier.width(8.dp))
                Eyebrow("SCREEN TRANSLATOR", Paper)
            }
        }
    }
}

@Composable
private fun LanguageCard(language: AppLanguage, label: String, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier.clip(SlashShape).background(Panel).clickable(role = Role.Button, onClick = onClick).padding(15.dp)) {
        Eyebrow(label)
        Text(language.translationCode.uppercase(), fontWeight = FontWeight.Black, fontSize = 36.sp, modifier = Modifier.padding(top = 10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(language.localizedName(), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Icon(Icons.Default.ArrowDropDown, null, Modifier.size(18.dp), tint = Red)
        }
    }
}

@Composable
private fun ScanOption(title: String, subtitle: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val color by animateColorAsState(if (selected) Paper else Panel, label = "scan selection")
    Column(modifier.clip(SlashShape).background(color).selectable(selected, role = Role.RadioButton, onClick = onClick).padding(16.dp)) {
        Icon(if (selected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, null, tint = if (selected) Red else Muted, modifier = Modifier.size(20.dp))
        Text(title, fontWeight = FontWeight.Black, color = if (selected) Ink else Paper, fontSize = 13.sp, modifier = Modifier.padding(top = 12.dp))
        Text(subtitle, color = if (selected) Ink.copy(alpha = .7f) else Muted, fontSize = 11.sp, lineHeight = 16.sp)
    }
}
