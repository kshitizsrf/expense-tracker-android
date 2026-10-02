package com.hisabkitab.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration

/** [part] as a percentage of [whole], e.g. 12.5 for 1/8. Zero when [whole] is not positive. */
fun percentOf(part: Long, whole: Long): Double = if (whole <= 0) 0.0 else part * 100.0 / whole

/** A percentage with exactly two decimals in the app's locale, without the % sign: "12.50". */
@Composable
@ReadOnlyComposable
fun percentNumber(value: Double): String = String.format(LocalConfiguration.current.locales[0], "%.2f", value)

/** A percentage with two decimals and the % sign: "12.50%". */
@Composable
@ReadOnlyComposable
fun percentText(value: Double): String = percentNumber(value) + "%"
