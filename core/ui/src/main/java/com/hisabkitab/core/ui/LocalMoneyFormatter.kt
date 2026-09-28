package com.hisabkitab.core.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.hisabkitab.core.common.money.MoneyFormatter

/** Currency formatter for the user's chosen currency, provided once at the app root. */
val LocalMoneyFormatter = staticCompositionLocalOf { MoneyFormatter(MoneyFormatter.defaultCurrencyCode()) }
