package com.hisabkitab.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hisabkitab.core.model.ColorPalette

/** Semantic colors Material 3 has no role for: money in (income) and money out (expense). */
@Immutable
data class FinanceColors(
    val income: Color,
    val incomeContainer: Color,
    val onIncomeContainer: Color,
    val expense: Color,
    val expenseContainer: Color,
    val onExpenseContainer: Color,
    val warning: Color,
)

private val LightFinanceColors = FinanceColors(
    income = Color(0xFF1B7F3B),
    incomeContainer = Color(0xFFC9F0D2),
    onIncomeContainer = Color(0xFF00210B),
    expense = Color(0xFFC4302B),
    expenseContainer = Color(0xFFFFDAD6),
    onExpenseContainer = Color(0xFF410002),
    warning = Color(0xFFB26A00),
)

private val DarkFinanceColors = FinanceColors(
    income = Color(0xFF7FDA93),
    incomeContainer = Color(0xFF005226),
    onIncomeContainer = Color(0xFFC9F0D2),
    expense = Color(0xFFFFB4AB),
    expenseContainer = Color(0xFF93000A),
    onExpenseContainer = Color(0xFFFFDAD6),
    warning = Color(0xFFFFB951),
)

val LocalFinanceColors = staticCompositionLocalOf { LightFinanceColors }

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

fun supportsDynamicColor(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
fun HisabKitabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: ColorPalette = ColorPalette.OCEAN,
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (useDynamicColor && supportsDynamicColor()) {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        palette.colorScheme(darkTheme)
    }

    CompositionLocalProvider(
        LocalFinanceColors provides if (darkTheme) DarkFinanceColors else LightFinanceColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}

/** Accessors for app-specific theme values, mirroring `MaterialTheme`. */
object HisabKitabTheme {
    val financeColors: FinanceColors
        @Composable get() = LocalFinanceColors.current
}
