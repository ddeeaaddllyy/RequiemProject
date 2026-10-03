package com.application.requiemproject.presentation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.application.requiemproject.R
import com.application.requiemproject.data.platform.service.ScreenCaptureService
import com.application.requiemproject.domain.model.ScanSource
import com.application.requiemproject.presentation.account.AccountViewModel
import com.application.requiemproject.presentation.help.HelpViewModel
import com.application.requiemproject.presentation.home.HomeViewModel
import com.application.requiemproject.presentation.navigation.Destination
import com.application.requiemproject.presentation.navigation.NavigationViewModel

/** Android permission and activity-result boundary; screen logic lives in ViewModels. */
class MainActivity : ComponentActivity() {
    private val account: AccountViewModel by viewModel()
    private val home: HomeViewModel by viewModel()
    private val help: HelpViewModel by viewModel()
    private val navigation: NavigationViewModel by viewModel()

    private val notifications = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) requestCapture() else home.captureMessage("Уведомления отключены. Разрешите их в настройках приложения, чтобы управлять захватом.")
    }
    private val overlay = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (Settings.canDrawOverlays(this)) requestCapture()
        else home.captureMessage("Для перевода поверх приложений разрешите наложение окон.")
    }
    private val projection = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
            try {
                ContextCompat.startForegroundService(this, Intent(this, ScreenCaptureService::class.java).apply {
                    putExtra("RESULT_CODE", result.resultCode)
                    putExtra("DATA", data)
                })
                home.captureMessage("Запрос захвата отправлен. Откройте нужное приложение; управление доступно в уведомлении Requiem.")
            } catch (_: Exception) {
                home.captureMessage("Не удалось запустить захват. Попробуйте снова.")
            }
        } else home.captureMessage("Захват отменён. Вы можете начать снова в любой момент.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT), navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        val shortcuts = listOf(
            "search" to "Справка", "profile" to "Профиль"
        ).map { (destination, label) ->
            ShortcutInfo.Builder(this, "${destination}_shortcut")
                .setShortLabel(label)
                .setIcon(Icon.createWithResource(this, R.drawable.ic_launcher_foreground))
                .setIntent(Intent(this, MainActivity::class.java).setAction(Intent.ACTION_VIEW).putExtra("open_fragment", destination))
                .build()
        }
        lifecycleScope.launch(Dispatchers.IO) {
            getSystemService(ShortcutManager::class.java).dynamicShortcuts = shortcuts
        }
        if (savedInstanceState == null) {
            when (intent.getStringExtra("open_fragment")) {
                "search" -> navigation.navigate(Destination.HELP)
                "profile" -> navigation.navigate(Destination.PROFILE)
            }
        }
        setContent {
            RequiemApp(account, home, help, navigation, ::requestCapture,
                { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) })
        }
    }

    private fun requestCapture() {
        home.captureMessage(null)
        if (home.settings.value.scanSource == ScanSource.ACCESSIBILITY) {
            val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).orEmpty()
            val serviceName = "$packageName/${com.application.requiemproject.data.platform.service.AccessibilityCaptureService::class.java.name}"
            if (!enabled.split(':').any { it.equals(serviceName, true) }) {
                home.captureMessage("Сначала включите службу Requiem в специальных возможностях Android.")
                return
            }
        }
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notifications.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        if (!Settings.canDrawOverlays(this)) {
            overlay.launch(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            return
        }
        projection.launch(getSystemService(MediaProjectionManager::class.java).createScreenCaptureIntent())
    }
}
