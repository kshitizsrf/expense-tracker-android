package com.hisabkitab.core.common.time

import com.hisabkitab.core.model.DateRange
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.WeekFields
import java.util.Locale

/** First day of the week for the user's locale (Monday in India/Europe, Sunday in the US). */
fun localeFirstDayOfWeek(locale: Locale = Locale.getDefault()): DayOfWeek = WeekFields.of(locale).firstDayOfWeek

/** Start of [DateRange.start] in [zone], as epoch millis (inclusive). */
fun DateRange.startEpochMillis(zone: ZoneId): Long = start.atStartOfDay(zone).toInstant().toEpochMilli()

/** Start of the day after [DateRange.endInclusive] in [zone], as epoch millis (exclusive). */
fun DateRange.endExclusiveEpochMillis(zone: ZoneId): Long =
    endInclusive.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

fun Instant.toLocalDate(zone: ZoneId): LocalDate = atZone(zone).toLocalDate()

/** Material date pickers work in UTC midnight millis. */
fun LocalDate.toUtcEpochMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

fun utcEpochMillisToLocalDate(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
