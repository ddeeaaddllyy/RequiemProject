package com.application.requiemproject.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.application.requiemproject.presentation.account.*
import com.application.requiemproject.presentation.components.*
import com.application.requiemproject.presentation.help.*
import com.application.requiemproject.presentation.home.*
import com.application.requiemproject.presentation.navigation.*
import com.application.requiemproject.presentation.theme.*

@Composable
fun RequiemApp(account: AccountViewModel, home: HomeViewModel, help: HelpViewModel, navigation: NavigationViewModel, onStartCapture: () -> Unit, onAccessibilitySettings: () -> Unit) {
    val accountState by account.state.collectAsStateWithLifecycle()
    val destination by navigation.destination.collectAsStateWithLifecycle()
    val authenticated = accountState.account != null || accountState.guest
    BackHandler(!authenticated && accountState.register) { account.toggleRegistration() }
    BackHandler(authenticated && destination != Destination.HOME && !accountState.editing) { navigation.navigate(Destination.HOME) }
    RequiemTheme {
        RequiemBackground {
            Column(Modifier.fillMaxSize().safeDrawingPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(Modifier.widthIn(max = 700.dp).fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(25.dp).clip(SlashShape).background(Red), contentAlignment = Alignment.Center) { Text("R", fontWeight = FontWeight.Black, fontSize = 18.sp) }
                    Text("REQUIEM", Modifier.weight(1f).padding(start = 9.dp), fontWeight = FontWeight.Black, letterSpacing = 3.sp, fontSize = 14.sp)
                    Eyebrow("BREAK THE BARRIER", Gold)
                }
                HorizontalDivider(color = Paper.copy(alpha = .12f))
                if (!authenticated) {
                    Box(Modifier.weight(1f).widthIn(max = 600.dp)) {
                        AuthScreen(accountState, account::login, account::password, account::authenticate, account::toggleRegistration, account::guest)
                    }
                } else {
                    AnimatedContent(destination, Modifier.weight(1f).widthIn(max = 700.dp).fillMaxWidth(), transitionSpec = {
                        (fadeIn(tween(230)) + slideInHorizontally(tween(300)) { it / 8 }) togetherWith
                            (fadeOut(tween(140)) + slideOutHorizontally(tween(230)) { -it / 10 })
                    }, label = "screen transition") { screen ->
                        when (screen) {
                            Destination.HOME -> {
                                val settings by home.settings.collectAsStateWithLifecycle()
                                val picker by home.languagePicker.collectAsStateWithLifecycle()
                                val message by home.captureMessage.collectAsStateWithLifecycle()
                                HomeScreen(settings, picker, message, home::openLanguages, home::closeLanguages, home::selectLanguage,
                                    home::swapLanguages, home::selectScanSource, onStartCapture, onAccessibilitySettings,
                                    { navigation.navigate(Destination.HELP) })
                            }
                            Destination.HELP -> {
                                val query by help.query.collectAsStateWithLifecycle()
                                val category by help.category.collectAsStateWithLifecycle()
                                val expanded by help.expanded.collectAsStateWithLifecycle()
                                val articles by help.articles.collectAsStateWithLifecycle()
                                HelpScreen(query, category, expanded, articles, help::query, help::category, help::toggle)
                            }
                            Destination.PROFILE -> ProfileScreen(accountState, account::editProfile, account::email, account::saveProfile,
                                { account.signOut(); navigation.navigate(Destination.HOME) }, account::showLogin,
                                { navigation.navigate(Destination.HELP) })
                        }
                    }
                    BottomNavigation(destination, navigation::navigate)
                }
            }
        }
    }
}

@Composable
private fun BottomNavigation(current: Destination, onSelect: (Destination) -> Unit) {
    Row(Modifier.widthIn(max = 700.dp).fillMaxWidth().background(Ink).selectableGroup().padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Destination.entries.forEach { destination ->
            val selected = current == destination
            val color by animateColorAsState(if (selected) Red else Ink, label = "navigation selection")
            Column(Modifier.weight(1f).clip(SlashShape).background(color)
                .selectable(selected, role = Role.Tab, onClick = { onSelect(destination) }).padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(when (destination) { Destination.HOME -> Icons.Default.Translate; Destination.HELP -> Icons.Default.AutoStories; Destination.PROFILE -> Icons.Default.PersonOutline },
                    null, tint = if (selected) Paper else Muted, modifier = Modifier.size(23.dp))
                Text(destination.label, color = if (selected) Paper else Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
