package com.hisabkitab.feature.stats.charts

import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hisabkitab.core.model.DateRange
import com.hisabkitab.feature.stats.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import kotlin.math.pow

/**
 * Calendar where each day is shaded by how much was spent (or earned) that day.
 * Up to two months are shown as full calendars with day numbers; longer ranges become a grid
 * of mini calendars, like a year-at-a-glance heatmap. Tapping a day selects it.
 */
@Composable
fun CalendarHeatmap(
    range: DateRange,
    daily: Map<LocalDate, Long>,
    color: Color,
    firstDayOfWeek: DayOfWeek,
    selectedDate: LocalDate?,
    onSelectDate: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val months = generateSequence(YearMonth.from(range.start)) { it.plusMonths(1) }
        .takeWhile { !it.isAfter(YearMonth.from(range.endInclusive)) }
        .toList()
    val max = daily.values.maxOrNull()?.coerceAtLeast(1L) ?: 1L
    val large = months.size <= 2

    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (large) {
            WeekdayHeader(firstDayOfWeek)
            months.forEach { month ->
                MonthGrid(month, range, daily, max, color, firstDayOfWeek, selectedDate, onSelectDate, showNumbers = true, gap = 6.dp)
            }
        } else {
            months.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { month ->
                        Column(Modifier.weight(1f)) {
                            Text(
                                month.month.getDisplayName(TextStyle.SHORT, LocalConfiguration.current.locales[0]),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(4.dp))
                            MonthGrid(month, range, daily, max, color, firstDayOfWeek, selectedDate, onSelectDate, showNumbers = false, gap = 2.dp)
                        }
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
        Legend(color)
    }
}

@Composable
private fun WeekdayHeader(firstDayOfWeek: DayOfWeek) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(7) { i ->
            Text(
                text = firstDayOfWeek.plus(i.toLong()).getDisplayName(TextStyle.NARROW, LocalConfiguration.current.locales[0]),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    range: DateRange,
    daily: Map<LocalDate, Long>,
    max: Long,
    color: Color,
    firstDayOfWeek: DayOfWeek,
    selectedDate: LocalDate?,
    onSelectDate: (LocalDate?) -> Unit,
    showNumbers: Boolean,
    gap: Dp,
) {
    val leading = (month.atDay(1).dayOfWeek.value - firstDayOfWeek.value + 7) % 7
    val cells = List(leading) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
    val empty = MaterialTheme.colorScheme.surfaceContainerHighest
    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        cells.chunked(7).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                (0 until 7).forEach { i ->
                    val date = week.getOrNull(i)
                    Box(Modifier.weight(1f).aspectRatio(1f)) {
                        if (date != null) {
                            val inRange = date in range
                            val amount = daily[date] ?: 0
                            // A power curve keeps small days visible next to one huge day.
                            val intensity = if (amount > 0) 0.18f + 0.82f * (amount.toFloat() / max).pow(0.6f) else 0f
                            val target = when {
                                !inRange -> empty.copy(alpha = 0.35f)
                                amount > 0 -> color.copy(alpha = intensity)
                                else -> empty
                            }
                            val fill by animateColorAsState(target, label = "heat")
                            val selected = date == selectedDate
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(if (showNumbers) 10.dp else 3.dp))
                                    .background(fill)
                                    .then(
                                        if (selected) {
                                            Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(if (showNumbers) 10.dp else 3.dp))
                                        } else {
                                            Modifier
                                        },
                                    )
                                    .clickable(enabled = inRange) { onSelectDate(if (selected) null else date) },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (showNumbers) {
                                    Text(
                                        text = date.dayOfMonth.toString(),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (amount > 0) FontWeight.Bold else FontWeight.Normal,
                                        color = if (intensity > 0.55f) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = if (inRange) 0.9f else 0.35f),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Legend(color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(R.string.heatmap_less), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(2.dp))
        listOf(0f, 0.25f, 0.5f, 0.75f, 1f).forEach { f ->
            Box(
                Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (f == 0f) MaterialTheme.colorScheme.surfaceContainerHighest else color.copy(alpha = 0.18f + 0.82f * f)),
            )
        }
        Spacer(Modifier.width(2.dp))
        Text(stringResource(R.string.heatmap_more), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Fills the parent box (aspect-ratio cell). */
private fun Modifier.fillMaxSize(): Modifier = this.then(Modifier.fillMaxWidth().aspectRatio(1f))
