package com.hisabkitab.core.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

enum class BudgetPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    CUSTOM,
}

/**
 * A spending limit. Daily, weekly and monthly budgets repeat automatically;
 * a custom budget covers exactly [customRange].
 */
data class Budget(
    val amountMinor: Long,
    val period: BudgetPeriod,
    val customRange: DateRange? = null,
) {
    /** The period that applies on [today]. */
    fun rangeFor(today: LocalDate, firstDayOfWeek: DayOfWeek): DateRange = when (period) {
        BudgetPeriod.DAILY -> DateRange.ofDay(today)
        BudgetPeriod.WEEKLY -> DateRange.ofWeek(today, firstDayOfWeek)
        BudgetPeriod.MONTHLY -> DateRange.ofMonth(YearMonth.from(today))
        BudgetPeriod.CUSTOM -> customRange ?: DateRange.ofMonth(YearMonth.from(today))
    }
}

/** How much of a [budget] has been spent within its current [range]. */
data class BudgetStatus(
    val budget: Budget,
    val range: DateRange,
    val spentMinor: Long,
    val today: LocalDate,
) {
    val remainingMinor: Long get() = budget.amountMinor - spentMinor

    /** Fraction spent; can exceed 1 when over budget. */
    val progress: Float
        get() = if (budget.amountMinor <= 0) 0f else spentMinor.toFloat() / budget.amountMinor

    val isOverBudget: Boolean get() = spentMinor > budget.amountMinor

    val isActive: Boolean get() = today in range

    val hasEnded: Boolean get() = today.isAfter(range.endInclusive)

    /** Days left in the period, including today. */
    val daysLeft: Long
        get() = if (isActive) ChronoUnit.DAYS.between(today, range.endInclusive) + 1 else 0

    /** What can still be spent per day without going over. */
    val dailyAllowanceMinor: Long
        get() = if (daysLeft > 0 && remainingMinor > 0) remainingMinor / daysLeft else 0
}
