package com.hisabkitab.core.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/** An inclusive range of calendar days. */
data class DateRange(
    val start: LocalDate,
    val endInclusive: LocalDate,
) {
    init {
        require(!endInclusive.isBefore(start)) { "endInclusive ($endInclusive) is before start ($start)" }
    }

    val dayCount: Long get() = ChronoUnit.DAYS.between(start, endInclusive) + 1

    operator fun contains(date: LocalDate): Boolean =
        !date.isBefore(start) && !date.isAfter(endInclusive)

    companion object {
        fun ofDay(date: LocalDate) = DateRange(date, date)

        fun ofWeek(date: LocalDate, firstDayOfWeek: DayOfWeek): DateRange {
            val start = date.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
            return DateRange(start, start.plusDays(6))
        }

        fun ofMonth(month: YearMonth) = DateRange(month.atDay(1), month.atEndOfMonth())

        fun ofYear(year: Int) = DateRange(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31))
    }
}
