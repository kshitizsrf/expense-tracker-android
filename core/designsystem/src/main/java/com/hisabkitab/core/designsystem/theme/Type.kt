package com.hisabkitab.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

private val Base = Typography()

/** Material 3 type scale with tabular figures on number-heavy styles, so amounts line up. */
val AppTypography = Typography(
    displayLarge = Base.displayLarge.tabular(),
    displayMedium = Base.displayMedium.tabular(),
    displaySmall = Base.displaySmall.tabular().copy(fontWeight = FontWeight.SemiBold),
    headlineLarge = Base.headlineLarge.tabular().copy(fontWeight = FontWeight.SemiBold),
    headlineMedium = Base.headlineMedium.tabular().copy(fontWeight = FontWeight.SemiBold),
    headlineSmall = Base.headlineSmall.tabular(),
    titleLarge = Base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = Base.titleMedium.tabular().copy(fontWeight = FontWeight.SemiBold),
    titleSmall = Base.titleSmall,
    bodyLarge = Base.bodyLarge,
    bodyMedium = Base.bodyMedium,
    bodySmall = Base.bodySmall,
    labelLarge = Base.labelLarge.tabular(),
    labelMedium = Base.labelMedium,
    labelSmall = Base.labelSmall,
)

private fun TextStyle.tabular(): TextStyle = copy(fontFeatureSettings = "tnum")
