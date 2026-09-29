package com.hisabkitab.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.rememberNavBackStack
import com.hisabkitab.core.designsystem.R as DesignR
import com.hisabkitab.core.designsystem.component.AuroraBackground
import com.hisabkitab.core.designsystem.component.LiquidNavItem
import com.hisabkitab.core.designsystem.component.LiquidNavigationBar
import com.hisabkitab.core.designsystem.component.SpotlightHost
import com.hisabkitab.core.designsystem.theme.LocalChartTransition
import com.hisabkitab.core.designsystem.theme.LocalNavBarClearance
import com.hisabkitab.core.model.NavBarStyle
import com.hisabkitab.core.model.UserPreferences
import com.hisabkitab.core.security.LockScreen
import com.hisabkitab.feature.onboarding.OnboardingScreen
import com.hisabkitab.navigation.HisabKitabNavDisplay
import com.hisabkitab.navigation.HomeKey
import com.hisabkitab.navigation.TopLevelDestination
import com.hisabkitab.navigation.TransactionEditorKey
import com.hisabkitab.navigation.navigateToTopLevel

/**
 * Root composable. The aurora backdrop is drawn once here and every screen is transparent on
 * top of it, so the theme feels continuous during navigation.
 */
@Composable
fun HisabKitabApp(
    preferences: UserPreferences,
    isLocked: Boolean,
    onUnlocked: () -> Unit,
) {
    CompositionLocalProvider(LocalChartTransition provides preferences.chartTransition) {
        AuroraBackground(Modifier.fillMaxSize()) {
            SpotlightHost {
                Crossfade(targetState = preferences.hasCompletedOnboarding, label = "onboarding") { onboarded ->
                    if (onboarded) MainContent(preferences.navBarStyle) else OnboardingScreen()
                }
            }
            AnimatedVisibility(visible = isLocked, enter = fadeIn(), exit = fadeOut()) {
                LockScreen(onUnlocked = onUnlocked)
            }
        }
    }
}

@Composable
private fun MainContent(navBarStyle: NavBarStyle) {
    val backStack = rememberNavBackStack(HomeKey)
    val currentKey = backStack.lastOrNull()
    val currentTab = TopLevelDestination.entries.firstOrNull { it.key == currentKey }

    Box(Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalNavBarClearance provides if (currentTab != null) NAV_BAR_CLEARANCE else 0.dp) {
            HisabKitabNavDisplay(backStack = backStack, modifier = Modifier.fillMaxSize())
        }

        AnimatedVisibility(
            visible = currentTab != null,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
        ) {
            LiquidNavigationBar(
                items = TopLevelDestination.entries.map {
                    LiquidNavItem(stringResource(it.label), it.selectedIcon, it.unselectedIcon)
                },
                selectedIndex = currentTab?.ordinal ?: -1,
                onSelect = { backStack.navigateToTopLevel(TopLevelDestination.entries[it]) },
                centerIcon = Icons.Filled.Add,
                centerContentDescription = stringResource(DesignR.string.add_transaction),
                onCenterClick = { backStack.add(TransactionEditorKey()) },
                style = navBarStyle,
            )
        }
    }
}

/** Bottom space scrollable tab content leaves for the floating bar and its raised button. */
private val NAV_BAR_CLEARANCE = 116.dp
