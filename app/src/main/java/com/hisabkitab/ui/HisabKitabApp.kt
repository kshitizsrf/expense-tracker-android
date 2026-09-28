package com.hisabkitab.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.rememberNavBackStack
import com.hisabkitab.navigation.HisabKitabNavDisplay
import com.hisabkitab.navigation.HomeKey
import com.hisabkitab.navigation.TopLevelDestination
import com.hisabkitab.navigation.navigateToTopLevel

/** Root composable: bottom navigation for the four tabs plus the navigation host. */
@Composable
fun HisabKitabApp() {
    val backStack = rememberNavBackStack(HomeKey)
    val currentKey = backStack.lastOrNull()
    val currentTab = TopLevelDestination.entries.firstOrNull { it.key == currentKey }

    Scaffold(
        // Screens own their insets; the outer scaffold only reserves space for the bottom bar.
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            AnimatedVisibility(
                visible = currentTab != null,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        val selected = destination == currentTab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { backStack.navigateToTopLevel(destination) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = null,
                                )
                            },
                            label = { Text(stringResource(destination.label)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        HisabKitabNavDisplay(
            backStack = backStack,
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding),
        )
    }
}
