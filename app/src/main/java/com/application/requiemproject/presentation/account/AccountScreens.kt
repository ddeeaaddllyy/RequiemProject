package com.application.requiemproject.presentation.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.requiemproject.presentation.components.*
import com.application.requiemproject.presentation.theme.*

@Composable
fun AuthScreen(state: AccountUiState, onLogin: (String) -> Unit, onPassword: (String) -> Unit, onSubmit: () -> Unit, onToggle: () -> Unit, onGuest: () -> Unit) {
    var showPassword by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(14.dp))
        Box(Modifier.fillMaxWidth().clip(SlashShape).background(Red).padding(26.dp), contentAlignment = Alignment.Center) {
            RequiemInsignia(Modifier.size(130.dp))
        }
        Spacer(Modifier.height(26.dp))
        Eyebrow("YOUR NEXT CHAPTER STARTS HERE", Gold)
        Text(if (state.register) "НОВОЕ\nАЛЬТЕР ЭГО." else "С ВОЗВРАЩЕНИЕМ,\nБУНТАРЬ.",
            style = MaterialTheme.typography.headlineLarge, fontStyle = FontStyle.Italic,
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp).graphicsLayer { rotationZ = -2f })
        Text(if (state.register) "Создай локальный профиль и задай свои правила." else "Войди в локальный профиль или начни без аккаунта.", color = Muted, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.login, onLogin, Modifier.fillMaxWidth().padding(top = 24.dp), label = { Text("Логин") }, singleLine = true, enabled = !state.loading,
            leadingIcon = { Icon(Icons.Default.PersonOutline, null) })
        OutlinedTextField(state.password, onPassword, Modifier.fillMaxWidth().padding(top = 12.dp), label = { Text("Пароль") }, singleLine = true, enabled = !state.loading,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            trailingIcon = { IconButton(onClick = { showPassword = !showPassword }) { Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, if (showPassword) "Скрыть пароль" else "Показать пароль") } })
        if (state.register) Text("Логин от 5 символов. Пароль от 6 символов и хотя бы одна цифра.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
        ErrorMessage(state.error)
        ActionButton(if (state.loading) "ПОДОЖДИТЕ…" else if (state.register) "СОЗДАТЬ ПРОФИЛЬ" else "ВОЙТИ В ИГРУ", Modifier.padding(top = 22.dp), enabled = !state.loading, onClick = onSubmit)
        TextButton(onClick = onToggle, enabled = !state.loading, modifier = Modifier.padding(top = 12.dp)) {
            Text(if (state.register) "Уже есть профиль? Войти" else "Нет профиля? Создать", color = Paper)
        }
        TextButton(onClick = onGuest, enabled = !state.loading) { Text("Продолжить без аккаунта →", color = Muted) }
        Spacer(Modifier.height(20.dp))
        Eyebrow("REQUIEM / BREAK THE LANGUAGE BARRIER")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(state: AccountUiState, onEdit: (Boolean) -> Unit, onEmail: (String) -> Unit, onSave: () -> Unit, onSignOut: () -> Unit, onLogin: () -> Unit, onHelp: () -> Unit) {
    var confirmSignOut by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp).padding(bottom = 28.dp)) {
        ScreenHeading("03 / YOUR ALTER EGO", "ЛИЧНОЕ\nДЕЛО.", "У каждой истории есть главный герой.")
        Box(Modifier.fillMaxWidth().clip(SlashShape).background(Paper).padding(24.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(78.dp).clip(SlashShape).background(Red), contentAlignment = Alignment.Center) { RequiemInsignia(Modifier.size(56.dp)) }
                    Column(Modifier.padding(start = 18.dp).weight(1f)) {
                        Eyebrow("CODENAME", Ink.copy(alpha = .6f))
                        Text(state.account?.name ?: "Гость", style = MaterialTheme.typography.headlineMedium, color = Ink)
                        Text(if (state.account == null) "Свободный агент" else "Локальный профиль", color = Ink.copy(alpha = .7f), fontSize = 13.sp)
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 22.dp), color = Ink.copy(alpha = .2f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Eyebrow("REQUIEM PROJECT", Ink)
                    Eyebrow("★ THE REBEL", Red)
                }
            }
        }
        SectionLabel("01", "Твой профиль")
        ProfileRow(Icons.Default.AlternateEmail, "Email", state.account?.email ?: "Не указан") { if (state.account != null) onEdit(true) else onLogin() }
        ProfileRow(Icons.Default.Tune, "Настройки профиля", if (state.account == null) "Войдите, чтобы настроить" else "Контактные данные") { if (state.account != null) onEdit(true) else onLogin() }
        SectionLabel("02", "За кулисами")
        ProfileRow(Icons.Default.AutoStories, "Досье Requiem", "Помощь и приватность", onHelp)
        Spacer(Modifier.height(24.dp))
        if (state.account == null) {
            ActionButton("СОЗДАТЬ СВОЁ АЛЬТЕР ЭГО", onClick = onLogin)
        } else {
            OutlinedButton(onClick = { confirmSignOut = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp), shape = SlashShape) {
                Icon(Icons.Default.Logout, null); Spacer(Modifier.width(10.dp)); Text("Выйти из профиля", color = Paper)
            }
        }
        Spacer(Modifier.height(30.dp))
        Eyebrow("TAKE YOUR WORDS BACK.", Gold)
        Text("Requiem · Экранный переводчик", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
    }
    if (state.editing) {
        ModalBottomSheet(onDismissRequest = { onEdit(false) }, containerColor = Ink,
            shape = CallingCardShape, scrimColor = Red.copy(alpha = .32f),
            dragHandle = { Box(Modifier.padding(top = 22.dp).size(48.dp, 5.dp).background(Red)) }) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).imePadding().padding(24.dp)) {
                CallingCardHeader("EDIT YOUR DOSSIER", "НАСТРОЙКИ")
                Text("Email используется в запросах к сервису перевода MyMemory.", color = Muted)
                OutlinedTextField(state.email, onEmail, Modifier.fillMaxWidth().padding(top = 18.dp), label = { Text("Email") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), enabled = !state.loading)
                ErrorMessage(state.error)
                ActionButton(if (state.loading) "СОХРАНЕНИЕ…" else "СОХРАНИТЬ", Modifier.padding(top = 20.dp), enabled = !state.loading, onClick = onSave)
                TextButton(onClick = { onEdit(false) }, modifier = Modifier.fillMaxWidth()) { Text("Отмена", color = Muted) }
            }
        }
    }
    if (confirmSignOut) {
        AlertDialog(onDismissRequest = { confirmSignOut = false }, title = { Text("Покинуть профиль?") },
            text = { Text("Ваш локальный аккаунт сохранится. Вы сможете войти снова.") },
            confirmButton = { TextButton(onClick = { confirmSignOut = false; onSignOut() }) { Text("Выйти") } },
            dismissButton = { TextButton(onClick = { confirmSignOut = false }) { Text("Остаться") } })
    }
}

@Composable
private fun ProfileRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Panel, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Red)
            Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Muted, fontSize = 12.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Muted)
        }
    }
}
