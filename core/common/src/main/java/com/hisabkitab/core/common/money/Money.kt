package com.hisabkitab.core.common.money

import java.math.BigDecimal
import java.math.RoundingMode

/** Number of decimal places stored for every amount (minor units = 1/100). */
const val MINOR_UNIT_SCALE = 2

/** Largest amount accepted anywhere in the app (999,999,999.99). */
const val MAX_AMOUNT_MINOR = 99_999_999_999L

/** Converts a major-unit decimal (e.g. 12.345) into minor units (1235), rounding half-up. */
fun BigDecimal.toMinorUnits(): Long =
    setScale(MINOR_UNIT_SCALE, RoundingMode.HALF_UP).movePointRight(MINOR_UNIT_SCALE).toLong()

/** Converts minor units back to a major-unit decimal. */
fun Long.toMajorUnits(): BigDecimal = BigDecimal.valueOf(this, MINOR_UNIT_SCALE)

/** Plain editable text for an amount, without grouping or trailing zeros: 125000 -> "1250". */
fun Long.toPlainAmountString(): String =
    toMajorUnits().stripTrailingZeros().let { if (it.scale() < 0) it.setScale(0) else it }.toPlainString()

/** Parses user-typed text such as "1250.5" into minor units, or null if it is not a valid amount. */
fun parseAmountToMinorUnits(text: String): Long? {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return null
    val value = trimmed.toBigDecimalOrNull() ?: return null
    if (value.signum() < 0) return null
    val minor = value.toMinorUnits()
    return minor.takeIf { it <= MAX_AMOUNT_MINOR }
}
