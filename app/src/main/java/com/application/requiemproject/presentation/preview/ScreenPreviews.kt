package com.application.requiemproject.presentation.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.application.requiemproject.domain.model.*
import com.application.requiemproject.presentation.account.*
import com.application.requiemproject.presentation.components.RequiemBackground
import com.application.requiemproject.presentation.home.HomeScreen
import com.application.requiemproject.presentation.theme.RequiemTheme

@Preview(name = "Translation / compact", widthDp = 360, heightDp = 800, showBackground = true)
@Preview(name = "Translation / large text", widthDp = 411, heightDp = 891, fontScale = 1.5f, showBackground = true)
@Composable
private fun HomePreview() {
    RequiemTheme {
        RequiemBackground {
            HomeScreen(TranslationSettings(AppLanguage.ENGLISH, AppLanguage.RUSSIAN, ScanSource.OCR),
                null, null, {}, {}, {}, {}, {}, {}, {}, {})
        }
    }
}

@Preview(name = "Sign in", widthDp = 411, heightDp = 891)
@Composable
private fun AuthPreview() {
    RequiemTheme { RequiemBackground { AuthScreen(AccountUiState(loading = false), {}, {}, {}, {}, {}) } }
}

@Preview(name = "Profile", widthDp = 411, heightDp = 891)
@Composable
private fun ProfilePreview() {
    RequiemTheme {
        RequiemBackground { ProfileScreen(AccountUiState(account = Account(1, "Rebel", "rebel@example.com"), loading = false), {}, {}, {}, {}, {}, {}) }
    }
}
