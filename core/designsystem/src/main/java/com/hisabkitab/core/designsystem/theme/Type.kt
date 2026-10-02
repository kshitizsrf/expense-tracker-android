package com.hisabkitab.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.hisabkitab.core.designsystem.R

/**
 * Plus Jakarta Sans (SIL Open Font License, see FONT_LICENSE_OFL.txt), bundled as one
 * variable font; each weight is an instance of its `wght` axis.
 */
@OptIn(ExperimentalTextApi::class)
private fun jakarta(weight: FontWeight) = Font(
    resId = R.font.plus_jakarta_sans,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val PlusJakartaSans = FontFamily(
    listOf(FontWeight.Light, FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold, FontWeight.ExtraBold)
        .map(::jakarta),
)

private val Base = Typography()

/**
 * Material 3 type scale in Plus Jakarta Sans, with tabular figures where amounts are shown.
 * Jakarta's tall x-height reads larger than Roboto at the same size, so the display, headline
 * and title roles are scaled down to keep headers compact.
 */
val AppTypography = Typography(
    displayLarge = Base.displayLarge.jakarta(FontWeight.Bold, scale = 0.84f, letterSpacing = (-1.5).sp).tabular(),
    displayMedium = Base.displayMedium.jakarta(FontWeight.Bold, scale = 0.84f, letterSpacing = (-1).sp).tabular(),
    displaySmall = Base.displaySmall.jakarta(FontWeight.Bold, scale = 0.86f, letterSpacing = (-0.5).sp).tabular(),
    headlineLarge = Base.headlineLarge.jakarta(FontWeight.Bold, scale = 0.86f).tabular(),
    headlineMedium = Base.headlineMedium.jakarta(FontWeight.Bold, scale = 0.86f, letterSpacing = (-0.25).sp).tabular(),
    headlineSmall = Base.headlineSmall.jakarta(FontWeight.SemiBold, scale = 0.88f).tabular(),
    titleLarge = Base.titleLarge.jakarta(FontWeight.Bold, scale = 0.9f),
    titleMedium = Base.titleMedium.jakarta(FontWeight.SemiBold, scale = 0.94f).tabular(),
    titleSmall = Base.titleSmall.jakarta(FontWeight.SemiBold, scale = 0.96f),
    bodyLarge = Base.bodyLarge.jakarta(FontWeight.Normal, scale = 0.94f),
    bodyMedium = Base.bodyMedium.jakarta(FontWeight.Normal, scale = 0.96f),
    bodySmall = Base.bodySmall.jakarta(FontWeight.Normal),
    labelLarge = Base.labelLarge.jakarta(FontWeight.SemiBold, scale = 0.96f).tabular(),
    labelMedium = Base.labelMedium.jakarta(FontWeight.Medium),
    labelSmall = Base.labelSmall.jakarta(FontWeight.Medium),
)

private fun TextStyle.jakarta(weight: FontWeight, scale: Float = 1f, letterSpacing: TextUnit = this.letterSpacing) =
    copy(
        fontFamily = PlusJakartaSans,
        fontWeight = weight,
        fontSize = fontSize * scale,
        lineHeight = lineHeight * scale,
        letterSpacing = letterSpacing,
    )

private fun TextStyle.tabular(): TextStyle = copy(fontFeatureSettings = "tnum")
