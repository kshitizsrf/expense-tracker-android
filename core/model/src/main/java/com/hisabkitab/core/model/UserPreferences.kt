package com.hisabkitab.core.model

import java.time.LocalTime

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

/** Built-in color schemes; each has a light and a dark variant. */
enum class ColorPalette {
    OCEAN,
    CORAL,
    TEAL,
    LAVENDER,
    SUNSET,
}

data class ReminderSettings(
    val enabled: Boolean,
    val time: LocalTime,
)

data class UserPreferences(
    val themeMode: ThemeMode,
    val palette: ColorPalette,
    val useDynamicColor: Boolean,
    val currencyCode: String,
    val reminder: ReminderSettings,
    val budget: Budget?,
    val hasRequestedNotificationPermission: Boolean,
)
