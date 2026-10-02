package com.hisabkitab.core.model

import java.time.LocalTime

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

/**
 * The app's signature themes. The first five are the original Hisab Kitab themes; each one is
 * a full-screen gradient with matching glass surfaces, in both light and dark variants.
 * [WALLPAPER] derives the colors from the device wallpaper (Android 12+).
 */
enum class AppTheme {
    SKYLINE,
    DAWN,
    HORIZON,
    PINKWALK,
    ROSEWOOD,

    /** Soft mint and cream with gentle slate text: the lowest-contrast, easiest-on-the-eyes theme. */
    SEASHELL,
    WALLPAPER,
}

/** Visual style of the floating bottom navigation bar. */
enum class NavBarStyle {
    /** A soft glow and dot light up under the selected icon. */
    GLOW,

    /** A pill that stretches like liquid toward the selected tab. */
    LIQUID,

    /** The selected icon rises into a floating bubble above the bar. */
    BUBBLE,
}

/** How charts animate when switching between chart types in Insights. */
enum class ChartTransition {
    ZOOM,
    SLIDE,
    FLIP,
}

data class ReminderSettings(
    val enabled: Boolean,
    val time: LocalTime,
)

data class UserPreferences(
    /** What the app calls the user in greetings; blank when they have not set one. */
    val userName: String,
    val themeMode: ThemeMode,
    val theme: AppTheme,
    val currencyCode: String,
    val reminder: ReminderSettings,
    val budget: Budget?,
    val hasRequestedNotificationPermission: Boolean,
    val hasCompletedOnboarding: Boolean,
    val appLockEnabled: Boolean,
    val navBarStyle: NavBarStyle,
    val chartTransition: ChartTransition,
)
