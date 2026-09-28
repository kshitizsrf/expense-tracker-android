package com.hisabkitab.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.hisabkitab.core.model.ColorPalette

/*
 * Five palettes derived from the original app's themes (Ocean was the default blue #004E92).
 * Accent roles differ per palette; neutral surfaces are shared so every palette stays calm
 * and legible in both light and dark mode.
 */

private data class Accents(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
)

private val OceanLight = Accents(
    primary = Color(0xFF0F5BA7), onPrimary = Color.White,
    primaryContainer = Color(0xFFD4E3FF), onPrimaryContainer = Color(0xFF001C3A),
    secondary = Color(0xFF545F71), onSecondary = Color.White,
    secondaryContainer = Color(0xFFD8E3F8), onSecondaryContainer = Color(0xFF111C2B),
    tertiary = Color(0xFF006A60), onTertiary = Color.White,
    tertiaryContainer = Color(0xFF9EF2E4), onTertiaryContainer = Color(0xFF00201C),
)
private val OceanDark = Accents(
    primary = Color(0xFFA5C8FF), onPrimary = Color(0xFF00315F),
    primaryContainer = Color(0xFF004786), onPrimaryContainer = Color(0xFFD4E3FF),
    secondary = Color(0xFFBCC7DB), onSecondary = Color(0xFF263141),
    secondaryContainer = Color(0xFF3C4758), onSecondaryContainer = Color(0xFFD8E3F8),
    tertiary = Color(0xFF82D5C8), onTertiary = Color(0xFF003731),
    tertiaryContainer = Color(0xFF005048), onTertiaryContainer = Color(0xFF9EF2E4),
)

private val CoralLight = Accents(
    primary = Color(0xFFA63B14), onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBCF), onPrimaryContainer = Color(0xFF380D00),
    secondary = Color(0xFF77574C), onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBCF), onSecondaryContainer = Color(0xFF2C160D),
    tertiary = Color(0xFF6B5E2F), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF4E2A7), onTertiaryContainer = Color(0xFF231B00),
)
private val CoralDark = Accents(
    primary = Color(0xFFFFB59C), onPrimary = Color(0xFF5C1900),
    primaryContainer = Color(0xFF7F2A0A), onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = Color(0xFFE7BDB0), onSecondary = Color(0xFF442A21),
    secondaryContainer = Color(0xFF5D4036), onSecondaryContainer = Color(0xFFFFDBCF),
    tertiary = Color(0xFFD8C68D), onTertiary = Color(0xFF3A3005),
    tertiaryContainer = Color(0xFF524619), onTertiaryContainer = Color(0xFFF4E2A7),
)

private val TealLight = Accents(
    primary = Color(0xFF2E6874), onPrimary = Color.White,
    primaryContainer = Color(0xFFB3EBFA), onPrimaryContainer = Color(0xFF001F26),
    secondary = Color(0xFF4B6268), onSecondary = Color.White,
    secondaryContainer = Color(0xFFCEE7EE), onSecondaryContainer = Color(0xFF061F24),
    tertiary = Color(0xFF565D7E), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDDE1FF), onTertiaryContainer = Color(0xFF131A37),
)
private val TealDark = Accents(
    primary = Color(0xFF97CFDD), onPrimary = Color(0xFF00363F),
    primaryContainer = Color(0xFF0F4F5B), onPrimaryContainer = Color(0xFFB3EBFA),
    secondary = Color(0xFFB2CBD2), onSecondary = Color(0xFF1C3439),
    secondaryContainer = Color(0xFF334A50), onSecondaryContainer = Color(0xFFCEE7EE),
    tertiary = Color(0xFFBEC4EB), onTertiary = Color(0xFF282F4D),
    tertiaryContainer = Color(0xFF3E4565), onTertiaryContainer = Color(0xFFDDE1FF),
)

private val LavenderLight = Accents(
    primary = Color(0xFF5B5498), onPrimary = Color.White,
    primaryContainer = Color(0xFFE4DFFF), onPrimaryContainer = Color(0xFF170F50),
    secondary = Color(0xFF5F5C71), onSecondary = Color.White,
    secondaryContainer = Color(0xFFE5DFF9), onSecondaryContainer = Color(0xFF1C192B),
    tertiary = Color(0xFF7B5266), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E8), onTertiaryContainer = Color(0xFF2F1123),
)
private val LavenderDark = Accents(
    primary = Color(0xFFC6BFFF), onPrimary = Color(0xFF2D2567),
    primaryContainer = Color(0xFF443C7F), onPrimaryContainer = Color(0xFFE4DFFF),
    secondary = Color(0xFFC8C3DC), onSecondary = Color(0xFF312E41),
    secondaryContainer = Color(0xFF474459), onSecondaryContainer = Color(0xFFE5DFF9),
    tertiary = Color(0xFFECB8CF), onTertiary = Color(0xFF482537),
    tertiaryContainer = Color(0xFF613B4E), onTertiaryContainer = Color(0xFFFFD8E8),
)

private val SunsetLight = Accents(
    primary = Color(0xFF3A5BA0), onPrimary = Color.White,
    primaryContainer = Color(0xFFDAE2FF), onPrimaryContainer = Color(0xFF001946),
    secondary = Color(0xFF8E4A40), onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDAD4), onSecondaryContainer = Color(0xFF3A0905),
    tertiary = Color(0xFF6E5D0E), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF9E287), onTertiaryContainer = Color(0xFF221B00),
)
private val SunsetDark = Accents(
    primary = Color(0xFFB1C5FF), onPrimary = Color(0xFF002C6F),
    primaryContainer = Color(0xFF214388), onPrimaryContainer = Color(0xFFDAE2FF),
    secondary = Color(0xFFFFB4A8), onSecondary = Color(0xFF561E16),
    secondaryContainer = Color(0xFF73342A), onSecondaryContainer = Color(0xFFFFDAD4),
    tertiary = Color(0xFFDCC66E), onTertiary = Color(0xFF3A3000),
    tertiaryContainer = Color(0xFF544600), onTertiaryContainer = Color(0xFFF9E287),
)

private fun Accents.toLightScheme(): ColorScheme = lightColorScheme(
    primary = primary, onPrimary = onPrimary,
    primaryContainer = primaryContainer, onPrimaryContainer = onPrimaryContainer,
    inversePrimary = primaryContainer,
    secondary = secondary, onSecondary = onSecondary,
    secondaryContainer = secondaryContainer, onSecondaryContainer = onSecondaryContainer,
    tertiary = tertiary, onTertiary = onTertiary,
    tertiaryContainer = tertiaryContainer, onTertiaryContainer = onTertiaryContainer,
    background = Color(0xFFF9F9FC), onBackground = Color(0xFF191C20),
    surface = Color(0xFFF9F9FC), onSurface = Color(0xFF191C20),
    surfaceVariant = Color(0xFFE0E2EC), onSurfaceVariant = Color(0xFF43474E),
    surfaceTint = primary,
    inverseSurface = Color(0xFF2E3035), inverseOnSurface = Color(0xFFF0F0F4),
    outline = Color(0xFF73777F), outlineVariant = Color(0xFFC3C6CF),
    surfaceBright = Color(0xFFF9F9FC), surfaceDim = Color(0xFFD9D9DD),
    surfaceContainerLowest = Color(0xFFFFFFFF), surfaceContainerLow = Color(0xFFF3F3F6),
    surfaceContainer = Color(0xFFEDEDF1), surfaceContainerHigh = Color(0xFFE8E8EB),
    surfaceContainerHighest = Color(0xFFE2E2E5),
)

private fun Accents.toDarkScheme(): ColorScheme = darkColorScheme(
    primary = primary, onPrimary = onPrimary,
    primaryContainer = primaryContainer, onPrimaryContainer = onPrimaryContainer,
    inversePrimary = primaryContainer,
    secondary = secondary, onSecondary = onSecondary,
    secondaryContainer = secondaryContainer, onSecondaryContainer = onSecondaryContainer,
    tertiary = tertiary, onTertiary = onTertiary,
    tertiaryContainer = tertiaryContainer, onTertiaryContainer = onTertiaryContainer,
    background = Color(0xFF111318), onBackground = Color(0xFFE2E2E9),
    surface = Color(0xFF111318), onSurface = Color(0xFFE2E2E9),
    surfaceVariant = Color(0xFF43474E), onSurfaceVariant = Color(0xFFC3C6CF),
    surfaceTint = primary,
    inverseSurface = Color(0xFFE2E2E9), inverseOnSurface = Color(0xFF2E3035),
    outline = Color(0xFF8D9199), outlineVariant = Color(0xFF43474E),
    surfaceBright = Color(0xFF37393E), surfaceDim = Color(0xFF111318),
    surfaceContainerLowest = Color(0xFF0C0E13), surfaceContainerLow = Color(0xFF191C20),
    surfaceContainer = Color(0xFF1D2024), surfaceContainerHigh = Color(0xFF282A2F),
    surfaceContainerHighest = Color(0xFF33353A),
)

fun ColorPalette.colorScheme(darkTheme: Boolean): ColorScheme {
    val (light, dark) = when (this) {
        ColorPalette.OCEAN -> OceanLight to OceanDark
        ColorPalette.CORAL -> CoralLight to CoralDark
        ColorPalette.TEAL -> TealLight to TealDark
        ColorPalette.LAVENDER -> LavenderLight to LavenderDark
        ColorPalette.SUNSET -> SunsetLight to SunsetDark
    }
    return if (darkTheme) dark.toDarkScheme() else light.toLightScheme()
}

/** Swatch shown in the palette picker. */
val ColorPalette.swatch: Color
    get() = when (this) {
        ColorPalette.OCEAN -> OceanLight.primary
        ColorPalette.CORAL -> CoralLight.primary
        ColorPalette.TEAL -> TealLight.primary
        ColorPalette.LAVENDER -> LavenderLight.primary
        ColorPalette.SUNSET -> SunsetLight.primary
    }
