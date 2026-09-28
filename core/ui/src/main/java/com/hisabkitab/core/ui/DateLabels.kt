package com.hisabkitab.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hisabkitab.core.ui.R
import com.hisabkitab.core.model.DateRange
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** Locale-aware date/time formatting used across screens. */
object DateFormats {
    private val locale: Locale get() = Locale.getDefault()

    fun monthYear(month: YearMonth): String = month.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale))

    fun dayMonth(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("d MMM", locale))

    fun weekdayDayMonth(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("EEE, d MMM", locale))

    fun medium(date: LocalDate): String = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))

    fun full(date: LocalDate): String = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale))

    fun time(time: LocalTime): String = time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale))

    /** "1 Sep – 30 Sep" within one year, otherwise "28 Dec 2025 – 3 Jan 2026". */
    fun range(range: DateRange): String = if (range.start.year == range.endInclusive.year) {
        "${dayMonth(range.start)} – ${dayMonth(range.endInclusive)}"
    } else {
        "${medium(range.start)} – ${medium(range.endInclusive)}"
    }
}

/** "Today", "Yesterday", or a short date (with the year when it differs from this year). */
@Composable
fun relativeDayLabel(date: LocalDate, today: LocalDate): String = when (date) {
    today -> stringResource(R.string.today)
    today.minusDays(1) -> stringResource(R.string.yesterday)
    else -> if (date.year == today.year) DateFormats.weekdayDayMonth(date) else DateFormats.medium(date)
}
