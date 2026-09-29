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

/** Material 3 type scale in Plus Jakarta Sans, with tabular figures where amounts are shown. */
val AppTypography = Typography(
    displayLarge = Base.displayLarge.jakarta(FontWeight.Bold, letterSpacing = (-1.5).sp).tabular(),
    displayMedium = Base.displayMedium.jakarta(FontWeight.Bold, letterSpacing = (-1).sp).tabular(),
    displaySmall = Base.displaySmall.jakarta(FontWeight.Bold, letterSpacing = (-0.5).sp).tabular(),
    headlineLarge = Base.headlineLarge.jakarta(FontWeight.Bold).tabular(),
    headlineMedium = Base.headlineMedium.jakarta(FontWeight.Bold, letterSpacing = (-0.25).sp).tabular(),
    headlineSmall = Base.headlineSmall.jakarta(FontWeight.SemiBold).tabular(),
    titleLarge = Base.titleLarge.jakarta(FontWeight.Bold),
    titleMedium = Base.titleMedium.jakarta(FontWeight.SemiBold).tabular(),
    titleSmall = Base.titleSmall.jakarta(FontWeight.SemiBold),
    bodyLarge = Base.bodyLarge.jakarta(FontWeight.Normal),
    bodyMedium = Base.bodyMedium.jakarta(FontWeight.Normal),
    bodySmall = Base.bodySmall.jakarta(FontWeight.Normal),
    labelLarge = Base.labelLarge.jakarta(FontWeight.SemiBold).tabular(),
    labelMedium = Base.labelMedium.jakarta(FontWeight.Medium),
    labelSmall = Base.labelSmall.jakarta(FontWeight.Medium),
)

private fun TextStyle.jakarta(weight: FontWeight, letterSpacing: TextUnit = this.letterSpacing) =
    copy(fontFamily = PlusJakartaSans, fontWeight = weight, letterSpacing = letterSpacing)

private fun TextStyle.tabular(): TextStyle = copy(fontFeatureSettings = "tnum")
