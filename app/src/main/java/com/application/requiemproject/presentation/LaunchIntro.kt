package com.application.requiemproject.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.application.requiemproject.R
import com.application.requiemproject.presentation.theme.*
import kotlinx.coroutines.delay

/** An offline, one-shot launch intro. Activity restoration never replays it. */
@Composable
fun LaunchIntro(showOnLaunch: Boolean = true, content: @Composable () -> Unit) {
    var finished by rememberSaveable { mutableStateOf(!showOnLaunch) }
    if (finished) {
        content()
        return
    }
    val composition = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.requiem_launch))
    val animation = animateLottieCompositionAsState(composition.value, iterations = 1)
    LaunchedEffect(composition.isFailure, animation.isAtEnd) {
        if (composition.isFailure || (composition.value != null && animation.isAtEnd)) finished = true
    }
    // Loading failures or unusually slow device animation scales must not block entry.
    LaunchedEffect(Unit) {
        delay(4000)
        finished = true
    }
    BackHandler { finished = true }
    RequiemTheme {
        BoxWithConstraints(Modifier.fillMaxSize().background(Ink).safeDrawingPadding()) {
            val artworkSize = minOf(maxWidth, maxHeight * .60f, 420.dp)
            Column(
                Modifier.fillMaxSize().testTag("launch-intro"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(Modifier.weight(1f))
                LottieAnimation(
                    composition.value, progress = { animation.progress },
                    modifier = Modifier.size(artworkSize)
                )
                Text("REQUIEM", color = Paper, fontSize = 36.sp, fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic, letterSpacing = 4.sp)
                Text("BREAK THE BARRIER", Modifier.padding(top = 8.dp), color = Gold,
                    fontSize = 11.sp, letterSpacing = 3.sp)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { finished = true }, modifier = Modifier.padding(bottom = 16.dp)) {
                    Text("Пропустить →", color = Paper)
                }
            }
        }
    }
}
