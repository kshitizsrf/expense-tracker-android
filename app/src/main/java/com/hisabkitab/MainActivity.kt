package com.hisabkitab

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.hisabkitab.core.common.money.MoneyFormatter
import com.hisabkitab.core.designsystem.theme.HisabKitabTheme
import com.hisabkitab.core.model.ThemeMode
import com.hisabkitab.core.security.AppLockManager
import com.hisabkitab.core.ui.LocalMoneyFormatter
import com.hisabkitab.ui.HisabKitabApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    @Inject lateinit var appLockManager: AppLockManager

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

                // The top of every theme's backdrop is deep, so status bar icons are always light;
                // navigation bar icons follow the app's light/dark mode.
                DisposableEffect(darkTheme) {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
                        navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    )
                    onDispose {}
                }

                val moneyFormatter = remember(preferences.currencyCode) { MoneyFormatter(preferences.currencyCode) }

                // With app lock on, hide the app's contents in Recents and screenshots.
                DisposableEffect(preferences.appLockEnabled) {
                    if (preferences.appLockEnabled) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    }
                    onDispose {}
                }

                val isLocked by appLockManager.isLocked.collectAsStateWithLifecycle()

                HisabKitabTheme(theme = preferences.theme, darkTheme = darkTheme) {
                    CompositionLocalProvider(LocalMoneyFormatter provides moneyFormatter) {
                        HisabKitabApp(
                            preferences = preferences,
                            isLocked = isLocked && preferences.hasCompletedOnboarding,
                            onUnlocked = appLockManager::unlock,
                        )
                    }
                }

                NotificationPermissionRequest(
                    shouldRequest = preferences.hasCompletedOnboarding &&
                        preferences.reminder.enabled &&
                        !preferences.hasRequestedNotificationPermission,
                    onRequested = viewModel::onNotificationPermissionRequested,
                )
            }
        }
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
