package com.hisabkitab.core.common.money

import com.hisabkitab.core.model.TransactionType
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.abs

/**
 * Formats minor-unit amounts for display in the user's locale and chosen currency.
 * Not thread-safe (wraps [NumberFormat]); create one per UI.
 */
class MoneyFormatter(
    currencyCode: String,
    private val locale: Locale = Locale.getDefault(),
) {
    val currency: Currency = runCatching { Currency.getInstance(currencyCode) }
        .getOrElse { Currency.getInstance(FALLBACK_CURRENCY) }

    val symbol: String get() = currency.getSymbol(locale)

    private val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance(locale).apply {
        currency = this@MoneyFormatter.currency
        // setCurrency does not reliably update fraction digits, so pin them explicitly.
        val fractionDigits = this@MoneyFormatter.currency.defaultFractionDigits.coerceAtLeast(0)
        minimumFractionDigits = fractionDigits
        maximumFractionDigits = fractionDigits
    }

    /** "₹1,250.50" */
    fun format(amountMinor: Long): String = numberFormat.format(amountMinor.toMajorUnits())

    /** "−₹1,250.50" for expenses, "+₹1,250.50" for income. */
    fun formatSigned(amountMinor: Long, type: TransactionType): String {
        val sign = if (type == TransactionType.EXPENSE) MINUS_SIGN else PLUS_SIGN
        return sign + format(abs(amountMinor))
    }

    /** "+₹10", "−₹10" or "₹0" depending on the sign of [amountMinor]. */
    fun formatNet(amountMinor: Long): String = when {
        amountMinor > 0 -> PLUS_SIGN + format(amountMinor)
        amountMinor < 0 -> MINUS_SIGN + format(-amountMinor)
        else -> format(0)
    }

    companion object {
        const val FALLBACK_CURRENCY = "INR"
        private const val MINUS_SIGN = "−"
        private const val PLUS_SIGN = "+"

        /** The currency of the device's region, or INR if the region has none. */
        fun defaultCurrencyCode(locale: Locale = Locale.getDefault()): String =
            runCatching { Currency.getInstance(locale).currencyCode }.getOrDefault(FALLBACK_CURRENCY)
    }
}
