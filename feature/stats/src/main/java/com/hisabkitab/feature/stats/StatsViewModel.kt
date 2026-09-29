package com.hisabkitab.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisabkitab.core.common.time.toLocalDate
import com.hisabkitab.core.data.repository.TransactionRepository
import com.hisabkitab.core.model.CategoryTotal
import com.hisabkitab.core.model.DateRange
import com.hisabkitab.core.model.Transaction
import com.hisabkitab.core.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

enum class StatsPeriod {
    THIS_MONTH,
    LAST_MONTH,
    LAST_3_MONTHS,
    THIS_YEAR,
    CUSTOM,
}

/** The ways the same data can be visualised. */
enum class ChartType {
    DONUT,
    BARS,
    TREND,
    CALENDAR,
    CASH_FLOW,
}

/** One point on the trend chart: a day, or a month for long ranges. */
data class TrendPoint(val start: LocalDate, val amountMinor: Long)

data class MonthFlow(val month: YearMonth, val incomeMinor: Long, val expenseMinor: Long) {
    val netMinor: Long get() = incomeMinor - expenseMinor
}

data class StatsUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val period: StatsPeriod = StatsPeriod.THIS_MONTH,
    val chartType: ChartType = ChartType.DONUT,
    val range: DateRange,
    val customRange: DateRange? = null,
    val categoryTotals: List<CategoryTotal> = emptyList(),
    val totalMinor: Long = 0,
    val transactionCount: Int = 0,
    val dailyAverageMinor: Long = 0,
    /** Total of the same-length period right before [range]; null if nothing was recorded. */
    val previousTotalMinor: Long? = null,
    val trend: List<TrendPoint> = emptyList(),
    val trendIsMonthly: Boolean = false,
    val daily: Map<LocalDate, Long> = emptyMap(),
    val cashFlow: List<MonthFlow> = emptyList(),
    val isLoading: Boolean = true,
) {
    /** Change versus the previous period as a fraction (0.12 = +12%), or null if unknown. */
    val changeVsPrevious: Float?
        get() = previousTotalMinor?.takeIf { it > 0 }?.let { (totalMinor - it).toFloat() / it }
}

private data class StatsSelection(
    val type: TransactionType = TransactionType.EXPENSE,
    val period: StatsPeriod = StatsPeriod.THIS_MONTH,
    val customRange: DateRange? = null,
    val chartType: ChartType = ChartType.DONUT,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val clock: Clock,
) : ViewModel() {

    private val selection = MutableStateFlow(StatsSelection())

    val uiState: StateFlow<StatsUiState> = selection
        .flatMapLatest { selection ->
            val range = rangeFor(selection)
            val previousRange = DateRange(range.start.minusDays(range.dayCount), range.start.minusDays(1))
            val cashFlowEnd = YearMonth.from(range.endInclusive)
            val cashFlowRange = DateRange(cashFlowEnd.minusMonths(CASH_FLOW_MONTHS - 1L).atDay(1), cashFlowEnd.atEndOfMonth())
            combine(
                transactionRepository.observeTransactionsIn(range),
                transactionRepository.observeTransactionsIn(previousRange),
                transactionRepository.observeTransactionsIn(cashFlowRange),
            ) { current, previous, cashFlow ->
                buildState(selection, range, current, previous, cashFlowEnd, cashFlow)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatsUiState(range = rangeFor(selection.value)),
        )

    fun onTypeChange(type: TransactionType) = selection.update { it.copy(type = type) }

    fun onPeriodChange(period: StatsPeriod) = selection.update { it.copy(period = period) }

    fun onCustomRangeSelected(range: DateRange) =
        selection.update { it.copy(period = StatsPeriod.CUSTOM, customRange = range) }

    fun onChartTypeChange(chartType: ChartType) = selection.update { it.copy(chartType = chartType) }

    private fun buildState(
        selection: StatsSelection,
        range: DateRange,
        current: List<Transaction>,
        previous: List<Transaction>,
        cashFlowEnd: YearMonth,
        cashFlow: List<Transaction>,
    ): StatsUiState {
        val zone = clock.zone
        val ofType = current.filter { it.type == selection.type }
        val total = ofType.sumOf { it.amountMinor }
        val daily = ofType.groupBy { it.occurredAt.toLocalDate(zone) }.mapValues { (_, items) -> items.sumOf { it.amountMinor } }
        val today = LocalDate.now(clock)
        val elapsedDays = when {
            today.isBefore(range.start) -> 0L
            today.isAfter(range.endInclusive) -> range.dayCount
            else -> ChronoUnit.DAYS.between(range.start, today) + 1
        }.coerceAtLeast(1L)
        val monthly = range.dayCount > DAILY_TREND_MAX_DAYS
        val previousTotal = previous.filter { it.type == selection.type }.sumOf { it.amountMinor }

        return StatsUiState(
            type = selection.type,
            period = selection.period,
            chartType = selection.chartType,
            range = range,
            customRange = selection.customRange,
            categoryTotals = ofType.groupBy { it.category }
                .map { (category, items) -> CategoryTotal(category, items.sumOf { it.amountMinor }, items.size) }
                .sortedByDescending { it.totalMinor },
            totalMinor = total,
            transactionCount = ofType.size,
            dailyAverageMinor = total / elapsedDays,
            previousTotalMinor = previousTotal.takeIf { previous.any { it.type == selection.type } },
            trend = if (monthly) monthlyTrend(range, daily) else dailyTrend(range, daily),
            trendIsMonthly = monthly,
            daily = daily,
            cashFlow = cashFlow(cashFlowEnd, cashFlow, zone),
            isLoading = false,
        )
    }

    private fun dailyTrend(range: DateRange, daily: Map<LocalDate, Long>): List<TrendPoint> =
        generateSequence(range.start) { it.plusDays(1) }
            .takeWhile { !it.isAfter(range.endInclusive) }
            .map { TrendPoint(it, daily[it] ?: 0) }
            .toList()

    private fun monthlyTrend(range: DateRange, daily: Map<LocalDate, Long>): List<TrendPoint> {
        val byMonth = daily.entries.groupBy { YearMonth.from(it.key) }.mapValues { (_, e) -> e.sumOf { it.value } }
        return generateSequence(YearMonth.from(range.start)) { it.plusMonths(1) }
            .takeWhile { !it.isAfter(YearMonth.from(range.endInclusive)) }
            .map { TrendPoint(it.atDay(1), byMonth[it] ?: 0) }
            .toList()
    }

    private fun cashFlow(end: YearMonth, transactions: List<Transaction>, zone: ZoneId): List<MonthFlow> {
        val grouped = transactions.groupBy { YearMonth.from(it.occurredAt.toLocalDate(zone)) }
        return (CASH_FLOW_MONTHS - 1 downTo 0).map { back ->
            val month = end.minusMonths(back.toLong())
            val items = grouped[month].orEmpty()
            MonthFlow(
                month = month,
                incomeMinor = items.filter { it.type == TransactionType.INCOME }.sumOf { it.amountMinor },
                expenseMinor = items.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountMinor },
            )
        }
    }

    private fun rangeFor(selection: StatsSelection): DateRange {
        val today = LocalDate.now(clock)
        val thisMonth = YearMonth.from(today)
        return when (selection.period) {
            StatsPeriod.THIS_MONTH -> DateRange.ofMonth(thisMonth)
            StatsPeriod.LAST_MONTH -> DateRange.ofMonth(thisMonth.minusMonths(1))
            StatsPeriod.LAST_3_MONTHS -> DateRange(thisMonth.minusMonths(2).atDay(1), thisMonth.atEndOfMonth())
            StatsPeriod.THIS_YEAR -> DateRange.ofYear(today.year)
            StatsPeriod.CUSTOM -> selection.customRange ?: DateRange.ofMonth(thisMonth)
        }
    }

    private companion object {
        const val CASH_FLOW_MONTHS = 6
        const val DAILY_TREND_MAX_DAYS = 62
    }
}
