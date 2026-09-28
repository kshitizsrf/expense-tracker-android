package com.hisabkitab

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hisabkitab.core.common.money.MoneyFormatter
import com.hisabkitab.core.designsystem.theme.HisabKitabTheme
import com.hisabkitab.core.model.ThemeMode
import com.hisabkitab.core.ui.LocalMoneyFormatter
import com.hisabkitab.ui.HisabKitabApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Loading)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState = it }
            }
        }
        // Keep the splash screen until preferences (theme, currency) are loaded to avoid a flash.
        splashScreen.setKeepOnScreenCondition { uiState is MainActivityUiState.Loading }

        enableEdgeToEdge()

        setContent {
            val state = uiState
            if (state is MainActivityUiState.Success) {
                val preferences = state.preferences
                val darkTheme = when (preferences.themeMode) {
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                }

                // Match status/navigation bar icon colors to the app theme, not just the system's.
                DisposableEffect(darkTheme) {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                        navigationBarStyle = SystemBarStyle.auto(LIGHT_SCRIM, DARK_SCRIM) { darkTheme },
                    )
                    onDispose {}
                }

                val moneyFormatter = remember(preferences.currencyCode) { MoneyFormatter(preferences.currencyCode) }

                HisabKitabTheme(
                    darkTheme = darkTheme,
                    palette = preferences.palette,
                    useDynamicColor = preferences.useDynamicColor,
                ) {
                    CompositionLocalProvider(LocalMoneyFormatter provides moneyFormatter) {
                        HisabKitabApp()
                    }
                }

                NotificationPermissionRequest(
                    shouldRequest = preferences.reminder.enabled && !preferences.hasRequestedNotificationPermission,
                    onRequested = viewModel::onNotificationPermissionRequested,
                )
            }
        }
    }

    private companion object {
        // Default scrims used by enableEdgeToEdge for 3-button navigation.
        val LIGHT_SCRIM = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
        val DARK_SCRIM = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
    }
}

/** Asks for notification permission once (Android 13+) so the daily reminder can be shown. */
@Composable
private fun NotificationPermissionRequest(shouldRequest: Boolean, onRequested: () -> Unit) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(shouldRequest) {
        if (!shouldRequest) return@LaunchedEffect
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        onRequested()
    }
}
