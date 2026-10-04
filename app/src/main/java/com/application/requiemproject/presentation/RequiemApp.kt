package com.application.requiemproject.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
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
    BackHandler(accountState.authenticationSuccess != null) { account.finishAuthenticationAnimation() }
    BackHandler(!authenticated && accountState.register) { account.toggleRegistration() }
    BackHandler(authenticated && accountState.authenticationSuccess == null && destination != Destination.HOME && !accountState.editing) { navigation.navigate(Destination.HOME) }
    RequiemTheme {
        RequiemBackground {
            Column(Modifier.fillMaxSize().safeDrawingPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(Modifier.widthIn(max = 700.dp).fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(25.dp).clip(SlashShape).background(Red), contentAlignment = Alignment.Center) { Text("R", fontWeight = FontWeight.Black, fontSize = 18.sp) }
                    Text("REQUIEM", Modifier.weight(1f).padding(start = 9.dp), fontWeight = FontWeight.Black, letterSpacing = 3.sp, fontSize = 14.sp)
                    Eyebrow("BREAK THE BARRIER", Gold)
                }
                HorizontalDivider(color = Paper.copy(alpha = .12f))
                if (accountState.restoring) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Red)
                    }
                } else if (accountState.authenticationSuccess != null) {
                    AuthenticationSuccessScreen(
                        accountState.authenticationSuccess!!, accountState.account?.name.orEmpty(),
                        account::finishAuthenticationAnimation, Modifier.weight(1f).fillMaxWidth()
                    )
                } else if (!authenticated) {
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
                                val providerEditor by home.providerEditor.collectAsStateWithLifecycle()
                                HomeScreen(settings, picker, message, home::openLanguages, home::closeLanguages, home::selectLanguage,
                                    home::swapLanguages, home::selectScanSource, onStartCapture, onAccessibilitySettings,
                                    { navigation.navigate(Destination.HELP) }, providerEditor, home::openProviders, home::closeProviders,
                                    home::selectProvider, home::providerKey, home::providerModel, home::providerUrl, home::saveProvider, home::removeProviderKey)
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
    Column(Modifier.widthIn(max = 700.dp).fillMaxWidth().background(Ink).padding(top = 6.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp), verticalAlignment = Alignment.CenterVertically) {
            Eyebrow("TAKE YOUR TIME", Gold)
            HorizontalDivider(Modifier.weight(1f).padding(start = 12.dp), color = Red)
        }
        Row(Modifier.fillMaxWidth().selectableGroup().padding(horizontal = 12.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Destination.entries.forEachIndexed { index, destination ->
            val selected = current == destination
            val color by animateColorAsState(if (selected) Paper else Panel, label = "navigation selection")
            val tilt by animateFloatAsState(if (selected) -4f else 0f, spring(), label = "navigation tilt")
            Box(Modifier.weight(1f)) {
                Box(Modifier.matchParentSize().graphicsLayer { translationX = 4.dp.toPx(); translationY = 4.dp.toPx() }.clip(SlashShape).background(if (selected) Red else Ink))
                Column(Modifier.fillMaxWidth().graphicsLayer { rotationZ = tilt }.clip(SlashShape).background(color)
                .border(1.dp, if (selected) Paper else Muted.copy(alpha = .25f), SlashShape)
                .selectable(selected, role = Role.Tab, onClick = { onSelect(destination) }).padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Eyebrow("0${index + 1}", if (selected) Red else Muted)
                Icon(when (destination) { Destination.HOME -> Icons.Default.Translate; Destination.HELP -> Icons.Default.AutoStories; Destination.PROFILE -> Icons.Default.PersonOutline },
                    null, tint = if (selected) Ink else Muted, modifier = Modifier.size(23.dp))
                Text(destination.label, color = if (selected) Ink else Paper, fontSize = 12.sp, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
        }
    }
}
