package com.hisabkitab.feature.stats

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.CalendarViewMonth
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.DonutLarge
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.StackedBarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hisabkitab.core.common.time.localeFirstDayOfWeek
import com.hisabkitab.core.designsystem.R as DesignR
import com.hisabkitab.core.designsystem.component.BackdropHeader
import com.hisabkitab.core.designsystem.component.CardHeader
import com.hisabkitab.core.designsystem.component.CategoryIconBadge
import com.hisabkitab.core.designsystem.component.DateRangePickerModal
import com.hisabkitab.core.designsystem.component.EmptyState
import com.hisabkitab.core.designsystem.component.GlassCard
import com.hisabkitab.core.designsystem.component.GlassChip
import com.hisabkitab.core.designsystem.component.SlidingSegmentedControl
import com.hisabkitab.core.designsystem.component.SpotlightItem
import com.hisabkitab.core.designsystem.component.spotlightOnClick
import com.hisabkitab.core.designsystem.theme.HisabKitabTheme
import com.hisabkitab.core.designsystem.theme.LocalChartTransition
import com.hisabkitab.core.designsystem.theme.LocalNavBarClearance
import com.hisabkitab.core.model.ChartTransition
import com.hisabkitab.core.model.CategoryTotal
import com.hisabkitab.core.model.DateRange
import com.hisabkitab.core.model.TransactionType
import com.hisabkitab.core.ui.DateFormats
import com.hisabkitab.core.ui.displayName
import com.hisabkitab.core.ui.LocalMoneyFormatter
import com.hisabkitab.feature.stats.charts.CalendarHeatmap
import com.hisabkitab.feature.stats.charts.CashFlowBar
import com.hisabkitab.feature.stats.charts.CashFlowChart
import com.hisabkitab.feature.stats.charts.DonutChart
import com.hisabkitab.feature.stats.charts.DonutSlice
import com.hisabkitab.feature.stats.charts.RankedBars
import com.hisabkitab.feature.stats.charts.TrendChart
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    StatsScreen(
        uiState = uiState,
        onTypeChange = viewModel::onTypeChange,
        onPeriodChange = viewModel::onPeriodChange,
        onCustomRangeSelected = viewModel::onCustomRangeSelected,
        onChartTypeChange = viewModel::onChartTypeChange,
    )
}

@Composable
internal fun StatsScreen(
    uiState: StatsUiState,
    onTypeChange: (TransactionType) -> Unit,
    onPeriodChange: (StatsPeriod) -> Unit,
    onCustomRangeSelected: (DateRange) -> Unit,
    onChartTypeChange: (ChartType) -> Unit,
) {
    var showRangePicker by rememberSaveable { mutableStateOf(false) }
    val systemBars = WindowInsets.systemBars.asPaddingValues()
    val accent = if (uiState.type == TransactionType.EXPENSE) HisabKitabTheme.colors.expense else HisabKitabTheme.colors.income

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = systemBars.calculateTopPadding(),
            bottom = systemBars.calculateBottomPadding() + LocalNavBarClearance.current + 8.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "header") {
            BackdropHeader(title = stringResource(R.string.insights_title), subtitle = stringResource(R.string.insights_subtitle))
        }
        item(key = "type") {
            SlidingSegmentedControl(
                options = listOf(
                    TransactionType.EXPENSE to stringResource(DesignR.string.expenses),
                    TransactionType.INCOME to stringResource(DesignR.string.income),
                ),
                selected = uiState.type,
                onSelect = onTypeChange,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        item(key = "periods") {
            PeriodChips(uiState.period, uiState.customRange, onPeriodChange) { showRangePicker = true }
        }
        item(key = "summary") { SummaryRow(uiState) }
        item(key = "chart-types") { ChartTypeChips(uiState.chartType, onChartTypeChange) }
        item(key = "chart") {
            GlassCard(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                if (uiState.totalMinor == 0L && uiState.chartType != ChartType.CASH_FLOW && !uiState.isLoading) {
                    EmptyState(
                        icon = Icons.Outlined.PieChart,
                        title = stringResource(R.string.stats_empty_title),
                        message = stringResource(R.string.stats_empty_message),
                    )
                } else {
                    val transition = LocalChartTransition.current
                    AnimatedContent(
                        targetState = uiState.chartType,
                        transitionSpec = { chartTransitionSpec(transition, forward = targetState.ordinal > initialState.ordinal) },
                        label = "chart",
                    ) { chart ->
                        val flip by this.transition.animateFloat(transitionSpec = { tween(420) }, label = "flip") { state ->
                            when {
                                transition != ChartTransition.FLIP -> 0f
                                state == EnterExitState.PreEnter -> -90f
                                state == EnterExitState.PostExit -> 90f
                                else -> 0f
                            }
                        }
                        Column(
                            Modifier.graphicsLayer {
                                rotationY = flip
                                cameraDistance = 16 * density
                            },
                        ) {
                            when (chart) {
                                ChartType.DONUT -> DonutSection(uiState, accent)
                                ChartType.BARS -> {
                                    CardHeader(stringResource(R.string.chart_bars_title), subtitle = DateFormats.range(uiState.range))
                                    Spacer(Modifier.height(16.dp))
                                    RankedBars(uiState.categoryTotals)
                                }
                                ChartType.TREND -> TrendSection(uiState, accent)
                                ChartType.CALENDAR -> CalendarSection(uiState, accent)
                                ChartType.CASH_FLOW -> CashFlowSection(uiState)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRangePicker) {
        DateRangePickerModal(
            initialRange = uiState.customRange ?: uiState.range,
            onRangeSelected = onCustomRangeSelected,
            onDismiss = { showRangePicker = false },
        )
    }
}

@Composable
private fun PeriodChips(
    selected: StatsPeriod,
    customRange: DateRange?,
    onSelect: (StatsPeriod) -> Unit,
    onCustom: () -> Unit,
) {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(StatsPeriod.entries) { period ->
            val label = when (period) {
                StatsPeriod.THIS_MONTH -> stringResource(R.string.period_this_month)
                StatsPeriod.LAST_MONTH -> stringResource(R.string.period_last_month)
                StatsPeriod.LAST_3_MONTHS -> stringResource(R.string.period_last_3_months)
                StatsPeriod.THIS_YEAR -> stringResource(R.string.period_this_year)
                StatsPeriod.CUSTOM -> customRange?.let(DateFormats::compactRange) ?: stringResource(DesignR.string.period_custom)
            }
            GlassChip(
                label = label,
                selected = period == selected,
                onClick = { if (period == StatsPeriod.CUSTOM) onCustom() else onSelect(period) },
                icon = if (period == StatsPeriod.CUSTOM) Icons.Outlined.DateRange else null,
            )
        }
    }
}

@Composable
private fun ChartTypeChips(selected: ChartType, onSelect: (ChartType) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(ChartType.entries) { type ->
            val (icon, label) = when (type) {
                ChartType.DONUT -> Icons.Outlined.DonutLarge to R.string.chart_donut
                ChartType.BARS -> Icons.Outlined.Leaderboard to R.string.chart_bars
                ChartType.TREND -> Icons.AutoMirrored.Outlined.ShowChart to R.string.chart_trend
                ChartType.CALENDAR -> Icons.Outlined.CalendarViewMonth to R.string.chart_calendar
                ChartType.CASH_FLOW -> Icons.Outlined.StackedBarChart to R.string.chart_cash_flow
            }
            GlassChip(label = stringResource(label), selected = type == selected, onClick = { onSelect(type) }, icon = icon)
        }
    }
}

@Composable
private fun SummaryRow(uiState: StatsUiState) {
    val formatter = LocalMoneyFormatter.current
    val colors = HisabKitabTheme.colors
    val change = uiState.changeVsPrevious
    // For expenses, spending more than before is bad (red); for income it's good (green).
    val increaseIsGood = uiState.type == TransactionType.INCOME
    val changeColor = when {
        change == null -> MaterialTheme.colorScheme.onSurfaceVariant
        (change > 0) == increaseIsGood -> colors.income
        else -> colors.expense
    }
    Row(
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val rangeLabel = DateFormats.range(uiState.range)
        val totalTitle = stringResource(R.string.stat_total)
        val averageTitle = stringResource(R.string.stat_daily_average)
        val entriesLabel = stringResource(R.string.stat_entries)
        val previousLabel = stringResource(R.string.stat_previous_period)
        SummaryTile(
            totalTitle,
            formatter.format(uiState.totalMinor),
            Modifier.weight(1.3f),
            spotlight = SpotlightItem(
                title = totalTitle,
                value = formatter.format(uiState.totalMinor),
                caption = rangeLabel,
                icon = Icons.Outlined.PieChart,
                details = buildList {
                    add(entriesLabel to uiState.transactionCount.toString())
                    add(averageTitle to formatter.format(uiState.dailyAverageMinor))
                    uiState.previousTotalMinor?.let { add(previousLabel to formatter.format(it)) }
                },
            ),
        )
        SummaryTile(
            averageTitle,
            formatter.format(uiState.dailyAverageMinor),
            Modifier.weight(1.1f),
            spotlight = SpotlightItem(averageTitle, formatter.format(uiState.dailyAverageMinor), rangeLabel, Icons.Outlined.CalendarViewMonth),
        )
        SummaryTile(
            label = stringResource(R.string.stat_vs_previous),
            value = change?.let { "${if (it >= 0) "+" else "−"}${abs(it * 100).roundToInt()}%" } ?: stringResource(R.string.stat_no_comparison),
            modifier = Modifier.weight(1f),
            valueColor = changeColor,
            icon = when {
                change == null -> null
                change >= 0 -> Icons.AutoMirrored.Outlined.TrendingUp
                else -> Icons.AutoMirrored.Outlined.TrendingDown
            },
        )
    }
}

@Composable
private fun SummaryTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.Unspecified,
    icon: ImageVector? = null,
    spotlight: SpotlightItem = SpotlightItem(label, value, icon = icon, valueColor = valueColor.takeIf { it != Color.Unspecified }),
) {
    GlassCard(
        modifier = modifier.clip(MaterialTheme.shapes.large).spotlightOnClick { spotlight },
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = valueColor, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
            }
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ColumnScope.DonutSection(uiState: StatsUiState, accent: Color) {
    val formatter = LocalMoneyFormatter.current
    var selected by remember(uiState.categoryTotals) { mutableStateOf<Int?>(null) }
    val totals = uiState.categoryTotals
    val slices = remember(totals) { totals.map { DonutSlice(it.totalMinor.toFloat(), Color(it.category.color)) } }
    val focus = selected?.let { totals.getOrNull(it) }
    val totalLabel = stringResource(if (uiState.type == TransactionType.EXPENSE) R.string.total_spent else R.string.total_earned)

    CardHeader(stringResource(R.string.chart_donut_title), subtitle = stringResource(R.string.chart_hint_donut))
    Spacer(Modifier.height(12.dp))
    DonutChart(
        slices = slices,
        selectedIndex = selected,
        onSelect = { selected = it },
        contentDescription = "$totalLabel ${formatter.format(uiState.totalMinor)}",
        modifier = Modifier.align(Alignment.CenterHorizontally).size(240.dp),
    ) {
        AnimatedContent(targetState = focus, label = "donutCenter") { item ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(140.dp)) {
                if (item == null) {
                    Text(totalLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatter.format(uiState.totalMinor), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        pluralStringResource(R.plurals.transaction_count, uiState.transactionCount, uiState.transactionCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                    )
                } else {
                    CategoryIconBadge(item.category.iconKey, item.category.color, size = 36.dp, filled = true)
                    Text(item.category.displayName(), style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                    Text(formatter.format(item.totalMinor), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(
                        stringResource(R.string.percent_of_total, (item.totalMinor * 100f / uiState.totalMinor.coerceAtLeast(1)).roundToInt()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    totals.forEachIndexed { index, total ->
        LegendRow(total, uiState.totalMinor, dimmed = selected != null && selected != index) {
            selected = if (selected == index) null else index
        }
    }
}

@Composable
private fun LegendRow(total: CategoryTotal, grandTotal: Long, dimmed: Boolean, onClick: () -> Unit) {
    val formatter = LocalMoneyFormatter.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val alpha = if (dimmed) 0.45f else 1f
        Box(Modifier.size(12.dp).clip(CircleShape).background(Color(total.category.color).copy(alpha = alpha)))
        Spacer(Modifier.width(12.dp))
        Text(
            total.category.displayName(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            "${(total.totalMinor * 100f / grandTotal.coerceAtLeast(1)).roundToInt()}%",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            formatter.format(total.totalMinor),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
        )
    }
}

@Composable
private fun TrendSection(uiState: StatsUiState, accent: Color) {
    val formatter = LocalMoneyFormatter.current
    val points = remember(uiState.trend, uiState.trendIsMonthly) {
        uiState.trend.map { point ->
            val label = if (uiState.trendIsMonthly) {
                DateFormats.shortMonth(YearMonth.from(point.start))
            } else {
                DateFormats.dayMonth(point.start)
            }
            label to point.amountMinor
        }
    }
    CardHeader(
        stringResource(if (uiState.trendIsMonthly) R.string.chart_trend_title_monthly else R.string.chart_trend_title_daily),
        subtitle = stringResource(R.string.chart_hint_trend),
    )
    Spacer(Modifier.height(12.dp))
    TrendChart(
        points = points,
        color = accent,
        formatValue = formatter::format,
        contentDescription = stringResource(R.string.chart_trend),
        modifier = Modifier.fillMaxWidth().height(240.dp),
    )
}

@Composable
private fun CalendarSection(uiState: StatsUiState, accent: Color) {
    val formatter = LocalMoneyFormatter.current
    var selectedDate by remember(uiState.range) { mutableStateOf<LocalDate?>(null) }
    CardHeader(
        stringResource(if (uiState.type == TransactionType.EXPENSE) R.string.chart_calendar_title else R.string.chart_calendar_title_income),
        subtitle = selectedDate?.let { date ->
            val amount = uiState.daily[date]
            stringResource(R.string.day_total, DateFormats.medium(date), amount?.let(formatter::format) ?: stringResource(R.string.nothing_on_day))
        } ?: stringResource(R.string.chart_hint_calendar),
    )
    Spacer(Modifier.height(16.dp))
    CalendarHeatmap(
        range = uiState.range,
        daily = uiState.daily,
        color = accent,
        firstDayOfWeek = localeFirstDayOfWeek(),
        selectedDate = selectedDate,
        onSelectDate = { selectedDate = it },
    )
}

@Composable
private fun CashFlowSection(uiState: StatsUiState) {
    val formatter = LocalMoneyFormatter.current
    val colors = HisabKitabTheme.colors
    var selected by remember(uiState.cashFlow) { mutableIntStateOf(uiState.cashFlow.lastIndex.coerceAtLeast(0)) }
    val bars = remember(uiState.cashFlow) {
        uiState.cashFlow.map { CashFlowBar(DateFormats.shortMonth(it.month), it.incomeMinor, it.expenseMinor) }
    }
    CardHeader(stringResource(R.string.chart_cash_flow_title), subtitle = stringResource(R.string.chart_hint_cash_flow))
    Spacer(Modifier.height(12.dp))
    CashFlowChart(
        bars = bars,
        incomeColor = colors.income,
        expenseColor = colors.expense,
        selectedIndex = selected,
        onSelect = { selected = it },
        contentDescription = stringResource(R.string.chart_cash_flow),
        modifier = Modifier.fillMaxWidth().height(220.dp),
    )
    uiState.cashFlow.getOrNull(selected)?.let { month ->
        Spacer(Modifier.height(16.dp))
        Text(DateFormats.monthYear(month.month), style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val monthLabel = DateFormats.monthYear(month.month)
            FlowFigure(stringResource(DesignR.string.income), formatter.format(month.incomeMinor), colors.income, Modifier.weight(1f), monthLabel)
            FlowFigure(stringResource(DesignR.string.expenses), formatter.format(month.expenseMinor), colors.expense, Modifier.weight(1f), monthLabel)
            FlowFigure(
                stringResource(R.string.net),
                formatter.formatNet(month.netMinor),
                if (month.netMinor >= 0) colors.income else colors.expense,
                Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun FlowFigure(label: String, value: String, color: Color, modifier: Modifier = Modifier, caption: String? = null) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .spotlightOnClick { SpotlightItem(title = label, value = value, caption = caption, valueColor = color) }
            .background(color.copy(alpha = 0.10f))
            .padding(12.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Enter/exit animation for switching chart types, per the user's Motion setting. */
private fun AnimatedContentTransitionScope<ChartType>.chartTransitionSpec(
    transition: ChartTransition,
    forward: Boolean,
): ContentTransform {
    val direction = if (forward) 1 else -1
    return when (transition) {
        ChartTransition.FADE ->
            (fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.96f)).togetherWith(fadeOut(tween(150)))
        ChartTransition.SLIDE ->
            (slideInHorizontally(tween(380)) { it * direction } + fadeIn(tween(380)))
                .togetherWith(slideOutHorizontally(tween(380)) { -it * direction } + fadeOut(tween(250)))
        // The rotation itself is applied with graphicsLayer; here we only sequence the halves.
        ChartTransition.FLIP ->
            fadeIn(tween(220, delayMillis = 200)).togetherWith(fadeOut(tween(200)))
        ChartTransition.ZOOM ->
            (scaleIn(tween(350), initialScale = 1.15f) + fadeIn(tween(350)))
                .togetherWith(scaleOut(tween(250), targetScale = 0.85f) + fadeOut(tween(250)))
    }
}
