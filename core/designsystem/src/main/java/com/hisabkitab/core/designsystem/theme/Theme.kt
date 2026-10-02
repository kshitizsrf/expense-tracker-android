package com.hisabkitab.core.designsystem.theme

import androidx.compose.ui.graphics.luminance
import androidx.annotation.ChecksSdkIntAtLeast
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hisabkitab.core.model.AppTheme
import com.hisabkitab.core.model.ChartTransition

/** App-specific colors that Material 3 has no role for. */
@Immutable
data class HisabColors(
    /** Backdrop gradient, top to bottom. */
    val backdrop: List<Color>,
    val aurora: List<Color>,
    /** Text and icons drawn directly on the backdrop (headers, hero numbers). */
    val onBackdrop: Color,
    val onBackdropMuted: Color,
    /** Readable color for content placed on a solid [onBackdrop] fill (selected chips, thumbs). */
    val onBackdropInverse: Color,
    /** Translucent glass surfaces and their light-catching edge. */
    val glass: Color,
    val glassStrong: Color,
    val glassEdge: Color,
    /** Accent pair used for gradient buttons and highlights. */
    val accentGradient: List<Color>,
    val income: Color,
    val expense: Color,
    val warning: Color,
    val isDark: Boolean,
)

val LocalHisabColors = staticCompositionLocalOf {
    HisabColors(
        backdrop = listOf(Color(0xFF004E92), Color(0xFFAEDFF7)),
        aurora = emptyList(),
        onBackdrop = Color.White,
        onBackdropMuted = Color.White.copy(alpha = 0.75f),
        onBackdropInverse = Color(0xFF003F77),
        glass = Color.White.copy(alpha = 0.8f),
        glassStrong = Color.White.copy(alpha = 0.92f),
        glassEdge = Color.White.copy(alpha = 0.7f),
        accentGradient = listOf(Color(0xFF004E92), Color(0xFF3F8FD8)),
        income = Color(0xFF138A4B),
        expense = Color(0xFFD6344A),
        warning = Color(0xFFB26A00),
        isDark = false,
    )
}

/** Space the floating navigation bar occupies, so scrolling content can clear it. */
val LocalNavBarClearance = staticCompositionLocalOf<Dp> { 0.dp }

/** How Insights animates between chart types (chosen in Settings). */
val LocalChartTransition = staticCompositionLocalOf { ChartTransition.ZOOM }

/** True when the user turned animations off in system settings. */
val LocalReducedMotion = staticCompositionLocalOf { false }

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
fun supportsDynamicColor(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
fun HisabKitabTheme(
    theme: AppTheme = AppTheme.SKYLINE,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val useWallpaper = theme == AppTheme.WALLPAPER && supportsDynamicColor()

    val spec: ThemeSpec
    val colorScheme = if (useWallpaper) {
        val dynamic = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        spec = dynamic.toThemeSpec(darkTheme)
        dynamic
    } else {
        val variants = theme.variants()
        spec = if (darkTheme) variants.dark else variants.light
        spec.toColorScheme(darkTheme)
    }

    val hisabColors = remember(spec, darkTheme) { spec.toHisabColors(darkTheme) }
    val reducedMotion = remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }

    CompositionLocalProvider(
        LocalHisabColors provides hisabColors,
        LocalReducedMotion provides reducedMotion,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}

private val ThemeSpec.lightBackdrop: Boolean get() = onBackdrop.luminance() < 0.5f

private fun ThemeSpec.toHisabColors(dark: Boolean) = HisabColors(
    backdrop = listOf(deep, lerp(deep, glow, 0.55f), glow),
    aurora = aurora,
    onBackdrop = onBackdrop,
    onBackdropMuted = onBackdrop.copy(alpha = if (lightBackdrop) 0.68f else 0.74f),
    // Deep tones darkened a touch so text on white chips always clears 4.5:1.
    onBackdropInverse = if (lightBackdrop) lerp(deep, Color.White, 0.55f) else lerp(deep, Color.Black, 0.25f),
    glass = if (dark) lerp(card, Color.Black, 0.25f).copy(alpha = 0.62f) else lerp(card, Color.White, 0.5f).copy(alpha = 0.82f),
    glassStrong = if (dark) lerp(card, Color.Black, 0.1f).copy(alpha = 0.86f) else lerp(card, Color.White, 0.65f).copy(alpha = 0.94f),
    // On a light backdrop a white edge disappears, so use a faint ink line instead.
    glassEdge = when {
        dark -> Color.White.copy(alpha = 0.12f)
        lightBackdrop -> ink.copy(alpha = 0.1f)
        else -> Color.White.copy(alpha = 0.75f)
    },
    accentGradient = if (dark) listOf(accent, lerp(accent, glow, 0.5f)) else listOf(accent, lerp(accent, glow, 0.6f)),
    income = if (dark) Color(0xFF6EE7A0) else Color(0xFF138A4B),
    expense = if (dark) Color(0xFFFF8A9B) else Color(0xFFD6344A),
    warning = if (dark) Color(0xFFFFC266) else Color(0xFFB26A00),
    isDark = dark,
)

/** Accessors for app-specific theme values, mirroring `MaterialTheme`. */
object HisabKitabTheme {
    val colors: HisabColors
        @Composable get() = LocalHisabColors.current
}
