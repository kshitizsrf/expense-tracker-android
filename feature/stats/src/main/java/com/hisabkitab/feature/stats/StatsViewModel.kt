package com.hisabkitab.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisabkitab.core.data.repository.TransactionRepository
import com.hisabkitab.core.model.CategoryTotal
import com.hisabkitab.core.model.DateRange
import com.hisabkitab.core.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

enum class StatsPeriod {
    THIS_MONTH,
    LAST_MONTH,
    LAST_3_MONTHS,
    THIS_YEAR,
    CUSTOM,
}

data class StatsUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val period: StatsPeriod = StatsPeriod.THIS_MONTH,
    val range: DateRange,
    val customRange: DateRange? = null,
    val totals: List<CategoryTotal> = emptyList(),
    val isLoading: Boolean = true,
) {
    val grandTotalMinor: Long get() = totals.sumOf { it.totalMinor }
}

private data class StatsSelection(
    val type: TransactionType = TransactionType.EXPENSE,
    val period: StatsPeriod = StatsPeriod.THIS_MONTH,
    val customRange: DateRange? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatsViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    private val clock: Clock,
) : ViewModel() {

    private val selection = MutableStateFlow(StatsSelection())

    val uiState: StateFlow<StatsUiState> = selection
        .flatMapLatest { selection ->
            val range = rangeFor(selection)
            transactionRepository.observeCategoryTotals(selection.type, range).map { totals ->
                StatsUiState(
                    type = selection.type,
                    period = selection.period,
                    range = range,
                    customRange = selection.customRange,
                    totals = totals,
                    isLoading = false,
                )
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
}
