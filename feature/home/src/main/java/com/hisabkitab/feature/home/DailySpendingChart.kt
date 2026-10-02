package com.hisabkitab.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.hisabkitab.core.designsystem.component.Sparkline
import com.hisabkitab.core.designsystem.component.glass
import com.hisabkitab.core.designsystem.theme.HisabKitabTheme
import com.hisabkitab.core.ui.LocalMoneyFormatter
import com.hisabkitab.core.ui.relativeDayLabel
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

/**
 * The month's daily-spending line. Touch and drag across it, as in a stock app, to read any
 * single day: a tooltip follows the finger with that day's spending, income and entry count.
 */
@Composable
internal fun DailySpendingChart(
    month: YearMonth,
    spending: List<Long>,
    income: List<Long>,
    counts: List<Int>,
    averageMinor: Long,
    entryCount: Int,
    modifier: Modifier = Modifier,
) {
    val colors = HisabKitabTheme.colors
    val haptics = LocalHapticFeedback.current
    var selected by remember(month, spending.size) { mutableStateOf<Int?>(null) }

    Column(modifier) {
        BoxWithConstraints(Modifier.fillMaxWidth().height(LABEL_ROW_HEIGHT)) {
            val index = selected
            if (index == null) {
                val formatter = LocalMoneyFormatter.current
                Row(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        stringResource(R.string.daily_spending),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onBackdropMuted,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false).padding(end = 12.dp),
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        stringResource(R.string.daily_average_short, formatter.format(averageMinor)) + " · " +
                            pluralStringResource(R.plurals.scrub_entries, entryCount, entryCount),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onBackdrop,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            } else {
                // Keep the tooltip centred on the finger but inside the card.
                val density = LocalDensity.current
                var tooltipWidth by remember { mutableIntStateOf(0) }
                val widthPx = with(density) { maxWidth.toPx() }
                val x = if (spending.size > 1) widthPx * index / (spending.size - 1) else 0f
                val left = (x - tooltipWidth / 2f).coerceIn(0f, (widthPx - tooltipWidth).coerceAtLeast(0f))
                DayTooltip(
                    date = month.atDay(index + 1),
                    spentMinor = spending.getOrElse(index) { 0 },
                    earnedMinor = income.getOrElse(index) { 0 },
                    count = counts.getOrElse(index) { 0 },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        // Taller than the label row: it grows upward over the space above the chart.
                        .wrapContentHeight(align = Alignment.Bottom, unbounded = true)
                        .onSizeChanged { tooltipWidth = it.width }
                        .offset { IntOffset(left.roundToInt(), 0) },
                )
            }
        }
        Sparkline(
            values = spending.map { it.toFloat() },
            color = colors.onBackdrop,
            selectedIndex = selected,
            onSelect = { index ->
                if (index != null && index != selected) {
                    haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                }
                selected = index
            },
            modifier = Modifier.fillMaxWidth().height(CHART_HEIGHT).padding(top = 6.dp, bottom = 2.dp),
        )
    }
}

@Composable
private fun DayTooltip(date: LocalDate, spentMinor: Long, earnedMinor: Long, count: Int, modifier: Modifier = Modifier) {
    val formatter = LocalMoneyFormatter.current
    val colors = HisabKitabTheme.colors
    Column(
        modifier = modifier
            .glass(RoundedCornerShape(14.dp), strong = true)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            buildString {
                append(relativeDayLabel(date, LocalDate.now()))
                append(" · ")
                append(pluralStringResource(R.plurals.scrub_entries, count, count))
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        AnimatedContent(
            targetState = spentMinor to earnedMinor,
            transitionSpec = { fadeIn().togetherWith(fadeOut()) },
            label = "dayAmounts",
        ) { (spent, earned) ->
            Row {
                Text(
                    stringResource(R.string.scrub_spent, formatter.format(spent)),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.expense,
                    maxLines = 1,
                )
                if (earned > 0) {
                    Text(
                        "  " + stringResource(R.string.scrub_earned, formatter.format(earned)),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.income,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/** Just the label line; the scrubbing tooltip overflows upward instead of reserving space. */
private val LABEL_ROW_HEIGHT = 18.dp

/** Taller than a decorative sparkline so it is easy to drag along. */
private val CHART_HEIGHT = 72.dp
