package com.hisabkitab.core.designsystem.theme

import androidx.annotation.StringRes
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.hisabkitab.core.designsystem.R
import com.hisabkitab.core.model.AppTheme

/**
 * The raw ingredients of a theme. Everything else (Material color scheme, glass surfaces,
 * backdrop) is derived from these, so each theme stays internally consistent.
 *
 * @property deep top of the backdrop gradient; headers and hero numbers sit on it.
 * @property glow bottom of the backdrop gradient.
 * @property accent buttons, selection and focus.
 * @property accentSoft chips, containers and secondary highlights.
 * @property card pastel tint for glass surfaces.
 * @property ink text/icon color used on pastel surfaces.
 * @property aurora colors of the slowly drifting light blobs on the backdrop.
 */
@Immutable
data class ThemeSpec(
    val deep: Color,
    val glow: Color,
    val accent: Color,
    val onAccent: Color,
    val accentSoft: Color,
    val card: Color,
    val ink: Color,
    val aurora: List<Color>,
)

@Immutable
data class ThemeVariants(val light: ThemeSpec, val dark: ThemeSpec)

// The original Hisab Kitab themes, rebuilt from their gradients and card colors.

private val Skyline = ThemeVariants(
    light = ThemeSpec(
        deep = Color(0xFF004E92), glow = Color(0xFFAEDFF7),
        accent = Color(0xFF004E92), onAccent = Color.White, accentSoft = Color(0xFFBBD7FF),
        card = Color(0xFFEDF3FB), ink = Color(0xFF0B2545),
        aurora = listOf(Color(0xFF3F8FD8), Color(0xFF7FD1F7), Color(0xFFBBD7FF)),
    ),
    dark = ThemeSpec(
        deep = Color(0xFF0A1128), glow = Color(0xFF001F54),
        accent = Color(0xFF8EC5FF), onAccent = Color(0xFF00224A), accentSoft = Color(0xFF123A6B),
        card = Color(0xFF101A33), ink = Color(0xFFD6E6FF),
        aurora = listOf(Color(0xFF0B4C9C), Color(0xFF1A6FB8), Color(0xFF0E2E66)),
    ),
)

private val Dawn = ThemeVariants(
    light = ThemeSpec(
        deep = Color(0xFF131B42), glow = Color(0xFFFD794D),
        accent = Color(0xFFE4602F), onAccent = Color.White, accentSoft = Color(0xFFFFC5AF),
        card = Color(0xFFFCE3D9), ink = Color(0xFF202B5F),
        aurora = listOf(Color(0xFFFD794D), Color(0xFFE4526B), Color(0xFF5B3A8C)),
    ),
    dark = ThemeSpec(
        deep = Color(0xFF0B0F26), glow = Color(0xFF7A3218),
        accent = Color(0xFFFF9A73), onAccent = Color(0xFF3A1200), accentSoft = Color(0xFF5C2A1A),
        card = Color(0xFF1C1626), ink = Color(0xFFFFDCCD),
        aurora = listOf(Color(0xFFB8492A), Color(0xFF8A2F4A), Color(0xFF2B2466)),
    ),
)

private val Horizon = ThemeVariants(
    light = ThemeSpec(
        deep = Color(0xFF07161B), glow = Color(0xFF3D737F),
        accent = Color(0xFF3D737F), onAccent = Color.White, accentSoft = Color(0xFFA9D2DB),
        card = Color(0xFFE6E2DD), ink = Color(0xFF10292F),
        aurora = listOf(Color(0xFF4F97A6), Color(0xFF2B5A64), Color(0xFF8FC4CF)),
    ),
    dark = ThemeSpec(
        deep = Color(0xFF030A0D), glow = Color(0xFF1C3A41),
        accent = Color(0xFF8BCBD8), onAccent = Color(0xFF00363F), accentSoft = Color(0xFF1E4750),
        card = Color(0xFF0F1B1F), ink = Color(0xFFCFEAF0),
        aurora = listOf(Color(0xFF2E6E7B), Color(0xFF16434C), Color(0xFF3D737F)),
    ),
)

private val Pinkwalk = ThemeVariants(
    light = ThemeSpec(
        deep = Color(0xFF8178C0), glow = Color(0xFFFACBD3),
        accent = Color(0xFF6E64B5), onAccent = Color.White, accentSoft = Color(0xFFE3C4EC),
        card = Color(0xFFFFEEF1), ink = Color(0xFF3B2F6B),
        aurora = listOf(Color(0xFFCA9DD7), Color(0xFFFACBD3), Color(0xFFA99AE0)),
    ),
    dark = ThemeSpec(
        deep = Color(0xFF1E1A3A), glow = Color(0xFF5A3F6B),
        accent = Color(0xFFD9C2FF), onAccent = Color(0xFF2E2266), accentSoft = Color(0xFF4A3A6E),
        card = Color(0xFF1F1A30), ink = Color(0xFFF3E6FF),
        aurora = listOf(Color(0xFF7A5A9E), Color(0xFF9E5A7E), Color(0xFF4B438F)),
    ),
)

private val Rosewood = ThemeVariants(
    light = ThemeSpec(
        deep = Color(0xFF002559), glow = Color(0xFFE45171),
        accent = Color(0xFF002C6A), onAccent = Color.White, accentSoft = Color(0xFFF8A79B),
        card = Color(0xFFFBF1DE), ink = Color(0xFF1B1F4A),
        aurora = listOf(Color(0xFFE45171), Color(0xFFF8A79B), Color(0xFF3A4FA0)),
    ),
    dark = ThemeSpec(
        deep = Color(0xFF00122E), glow = Color(0xFF6E2239),
        accent = Color(0xFFFF9FB2), onAccent = Color(0xFF4A0018), accentSoft = Color(0xFF4A2338),
        card = Color(0xFF1A1426), ink = Color(0xFFFFE0E6),
        aurora = listOf(Color(0xFFA8324F), Color(0xFF2A3F8F), Color(0xFF7A3A5E)),
    ),
)

fun AppTheme.variants(): ThemeVariants = when (this) {
    AppTheme.SKYLINE, AppTheme.WALLPAPER -> Skyline
    AppTheme.DAWN -> Dawn
    AppTheme.HORIZON -> Horizon
    AppTheme.PINKWALK -> Pinkwalk
    AppTheme.ROSEWOOD -> Rosewood
}

@get:StringRes
val AppTheme.displayName: Int
    get() = when (this) {
        AppTheme.SKYLINE -> R.string.theme_skyline
        AppTheme.DAWN -> R.string.theme_dawn
        AppTheme.HORIZON -> R.string.theme_horizon
        AppTheme.PINKWALK -> R.string.theme_pinkwalk
        AppTheme.ROSEWOOD -> R.string.theme_rosewood
        AppTheme.WALLPAPER -> R.string.theme_wallpaper
    }

@get:StringRes
val AppTheme.tagline: Int
    get() = when (this) {
        AppTheme.SKYLINE -> R.string.theme_skyline_tagline
        AppTheme.DAWN -> R.string.theme_dawn_tagline
        AppTheme.HORIZON -> R.string.theme_horizon_tagline
        AppTheme.PINKWALK -> R.string.theme_pinkwalk_tagline
        AppTheme.ROSEWOOD -> R.string.theme_rosewood_tagline
        AppTheme.WALLPAPER -> R.string.theme_wallpaper_tagline
    }

/** Builds a theme spec from a Material You dynamic scheme (Wallpaper theme). */
internal fun ColorScheme.toThemeSpec(dark: Boolean): ThemeSpec = if (dark) {
    ThemeSpec(
        deep = lerp(primaryContainer, Color.Black, 0.72f), glow = lerp(tertiaryContainer, Color.Black, 0.35f),
        accent = primary, onAccent = onPrimary, accentSoft = secondaryContainer,
        card = surfaceContainer, ink = onSurface,
        aurora = listOf(primaryContainer, tertiaryContainer, secondaryContainer),
    )
} else {
    ThemeSpec(
        deep = primary, glow = tertiaryContainer,
        accent = primary, onAccent = onPrimary, accentSoft = primaryContainer,
        card = surfaceContainerLow, ink = onPrimaryContainer,
        aurora = listOf(tertiary, primaryContainer, secondaryContainer),
    )
}

/** Material 3 roles derived from a spec, so stock components (dialogs, pickers) match. */
internal fun ThemeSpec.toColorScheme(dark: Boolean): ColorScheme = if (dark) {
    val base = lerp(deep, Color(0xFF15171E), 0.55f)
    darkColorScheme(
        primary = accent, onPrimary = onAccent,
        primaryContainer = accentSoft, onPrimaryContainer = ink,
        inversePrimary = lerp(accent, Color.Black, 0.4f),
        secondary = lerp(accent, Color.White, 0.25f), onSecondary = onAccent,
        secondaryContainer = lerp(accentSoft, base, 0.35f), onSecondaryContainer = ink,
        tertiary = lerp(glow, Color.White, 0.45f), onTertiary = deep,
        tertiaryContainer = lerp(glow, base, 0.3f), onTertiaryContainer = ink,
        background = base, onBackground = Color(0xFFECEDF3),
        surface = base, onSurface = Color(0xFFECEDF3),
        surfaceVariant = lerp(base, Color.White, 0.12f), onSurfaceVariant = Color(0xFFB9BDCB),
        surfaceTint = accent,
        inverseSurface = Color(0xFFECEDF3), inverseOnSurface = base,
        outline = Color(0xFF8C91A0), outlineVariant = lerp(base, Color.White, 0.16f),
        surfaceBright = lerp(base, Color.White, 0.14f), surfaceDim = base,
        surfaceContainerLowest = lerp(base, Color.Black, 0.3f),
        surfaceContainerLow = lerp(base, Color.White, 0.03f),
        surfaceContainer = lerp(base, Color.White, 0.06f),
        surfaceContainerHigh = lerp(base, Color.White, 0.09f),
        surfaceContainerHighest = lerp(base, Color.White, 0.13f),
    )
} else {
    val surface = lerp(card, Color.White, 0.55f)
    val onSurface = lerp(ink, Color(0xFF15161C), 0.55f)
    lightColorScheme(
        primary = accent, onPrimary = onAccent,
        primaryContainer = accentSoft, onPrimaryContainer = ink,
        inversePrimary = accentSoft,
        secondary = lerp(accent, ink, 0.35f), onSecondary = Color.White,
        secondaryContainer = lerp(card, accentSoft, 0.55f), onSecondaryContainer = ink,
        tertiary = lerp(glow, ink, 0.4f), onTertiary = Color.White,
        tertiaryContainer = lerp(card, glow, 0.35f), onTertiaryContainer = ink,
        background = surface, onBackground = onSurface,
        surface = surface, onSurface = onSurface,
        surfaceVariant = lerp(card, ink, 0.08f), onSurfaceVariant = lerp(ink, Color(0xFF5E6270), 0.55f),
        surfaceTint = accent,
        inverseSurface = lerp(ink, Color.Black, 0.5f), inverseOnSurface = card,
        outline = lerp(ink, Color(0xFF8A8E9C), 0.6f), outlineVariant = lerp(card, ink, 0.16f),
        surfaceBright = Color.White, surfaceDim = lerp(card, ink, 0.1f),
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = lerp(card, Color.White, 0.7f),
        surfaceContainer = lerp(card, Color.White, 0.45f),
        surfaceContainerHigh = lerp(card, Color.White, 0.2f),
        surfaceContainerHighest = card,
    )
}
