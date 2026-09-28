package com.hisabkitab.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class BudgetTest {

    private val today = LocalDate.of(2026, 9, 17) // Thursday

    @Test
    fun monthlyBudgetCoversCalendarMonth() {
        val range = Budget(100_00, BudgetPeriod.MONTHLY).rangeFor(today, DayOfWeek.MONDAY)
        assertEquals(DateRange(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)), range)
    }

    @Test
    fun weeklyBudgetRespectsFirstDayOfWeek() {
        val monday = Budget(100_00, BudgetPeriod.WEEKLY).rangeFor(today, DayOfWeek.MONDAY)
        assertEquals(DateRange(LocalDate.of(2026, 9, 14), LocalDate.of(2026, 9, 20)), monday)

        val sunday = Budget(100_00, BudgetPeriod.WEEKLY).rangeFor(today, DayOfWeek.SUNDAY)
        assertEquals(DateRange(LocalDate.of(2026, 9, 13), LocalDate.of(2026, 9, 19)), sunday)
    }

    @Test
    fun dailyBudgetIsToday() {
        assertEquals(DateRange.ofDay(today), Budget(100_00, BudgetPeriod.DAILY).rangeFor(today, DayOfWeek.MONDAY))
    }

    @Test
    fun statusComputesRemainingAndDailyAllowance() {
        val budget = Budget(30_000_00, BudgetPeriod.MONTHLY)
        val status = BudgetStatus(budget, budget.rangeFor(today, DayOfWeek.MONDAY), spentMinor = 16_000_00, today = today)

        assertEquals(14_000_00L, status.remainingMinor)
        assertEquals(14L, status.daysLeft) // 17th..30th inclusive
        assertEquals(1_000_00L, status.dailyAllowanceMinor)
        assertFalse(status.isOverBudget)
        assertTrue(status.isActive)
    }

    @Test
    fun overBudgetHasNoAllowance() {
        val budget = Budget(1_000_00, BudgetPeriod.MONTHLY)
        val status = BudgetStatus(budget, budget.rangeFor(today, DayOfWeek.MONDAY), spentMinor = 1_500_00, today = today)

        assertTrue(status.isOverBudget)
        assertEquals(0L, status.dailyAllowanceMinor)
        assertEquals(1.5f, status.progress, 0.0001f)
    }

    @Test
    fun customBudgetInThePastHasEnded() {
        val range = DateRange(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31))
        val budget = Budget(1_000_00, BudgetPeriod.CUSTOM, range)
        val status = BudgetStatus(budget, budget.rangeFor(today, DayOfWeek.MONDAY), 0, today)

        assertTrue(status.hasEnded)
        assertFalse(status.isActive)
        assertEquals(0L, status.daysLeft)
    }
}
