package com.hisabkitab.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisabkitab.core.common.time.localeFirstDayOfWeek
import com.hisabkitab.core.common.time.toLocalDate
import com.hisabkitab.core.data.repository.TransactionRepository
import com.hisabkitab.core.data.repository.UserPreferencesRepository
import com.hisabkitab.core.model.BudgetStatus
import com.hisabkitab.core.model.CategoryTotal
import com.hisabkitab.core.model.DateRange
import com.hisabkitab.core.model.PeriodTotals
import com.hisabkitab.core.model.Transaction
import com.hisabkitab.core.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/** Quick facts shown next to the daily-spending chart. */
data class MonthInsights(
    val averageDailySpendMinor: Long = 0,
    val transactionCount: Int = 0,
)

data class HomeUiState(
    val month: YearMonth,
    val isCurrentMonth: Boolean,
    val totals: PeriodTotals = PeriodTotals(),
    /** Expense per day of the month (up to today for the current month), for the sparkline. */
    val dailySpending: List<Long> = emptyList(),
    /** Income per day, aligned with [dailySpending]; shown while scrubbing the chart. */
    val dailyIncome: List<Long> = emptyList(),
    /** Number of entries per day, aligned with [dailySpending]. */
    val dailyCount: List<Int> = emptyList(),
    val insights: MonthInsights = MonthInsights(),
    val budgetStatus: BudgetStatus? = null,
    val recentTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    userPreferencesRepository: UserPreferencesRepository,
    private val clock: Clock,
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.now(clock))

    private val monthTransactions = selectedMonth.flatMapLatest { month ->
        transactionRepository.observeTransactionsIn(DateRange.ofMonth(month)).map { month to it }
    }

    private val budgetStatus = userPreferencesRepository.userPreferences
        .map { it.budget }
        .distinctUntilChanged()
        .flatMapLatest { budget ->
            if (budget == null) {
                flowOf(null)
            } else {
                val today = LocalDate.now(clock)
                val range = budget.rangeFor(today, localeFirstDayOfWeek())
                transactionRepository.observeTotal(TransactionType.EXPENSE, range)
                    .map { spent -> BudgetStatus(budget, range, spent, today) }
            }
        }

    val uiState: StateFlow<HomeUiState> = combine(
        monthTransactions,
        budgetStatus,
        transactionRepository.observeRecent(RECENT_LIMIT),
    ) { (month, transactions), budget, recent ->
        val today = LocalDate.now(clock)
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
        val lastDay = if (month == YearMonth.from(today)) today.dayOfMonth else month.lengthOfMonth()
        val perDay = LongArray(lastDay)
        val incomePerDay = LongArray(lastDay)
        val countPerDay = IntArray(lastDay)
        transactions.forEach { t ->
            val day = t.occurredAt.toLocalDate(clock.zone).dayOfMonth
            if (day > lastDay) return@forEach
            countPerDay[day - 1]++
            if (t.type == TransactionType.EXPENSE) perDay[day - 1] += t.amountMinor else incomePerDay[day - 1] += t.amountMinor
        }
        val expenseTotal = expenses.sumOf { it.amountMinor }
        HomeUiState(
            month = month,
            isCurrentMonth = month == YearMonth.from(today),
            totals = PeriodTotals(
                incomeMinor = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amountMinor },
                expenseMinor = expenseTotal,
            ),
            dailySpending = perDay.toList(),
            dailyIncome = incomePerDay.toList(),
            dailyCount = countPerDay.toList(),
            insights = MonthInsights(
                averageDailySpendMinor = if (lastDay > 0) expenseTotal / lastDay else 0,
                transactionCount = transactions.size,
            ),
            budgetStatus = budget,
            recentTransactions = recent,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(month = selectedMonth.value, isCurrentMonth = true),
    )

    /** The name to greet the user with; blank when none is set. */
    val userName: StateFlow<String> = userPreferencesRepository.userPreferences
        .map { it.userName }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    fun showPreviousMonth() = selectedMonth.update { it.minusMonths(1) }

    fun showNextMonth() = selectedMonth.update { it.plusMonths(1) }

    fun showCurrentMonth() = selectedMonth.update { YearMonth.now(clock) }

    private companion object {
        const val RECENT_LIMIT = 5
    }
}
