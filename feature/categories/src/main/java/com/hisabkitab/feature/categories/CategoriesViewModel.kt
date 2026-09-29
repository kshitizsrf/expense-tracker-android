package com.hisabkitab.feature.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisabkitab.core.data.repository.CategoryRepository
import com.hisabkitab.core.data.repository.TransactionRepository
import com.hisabkitab.core.model.Category
import com.hisabkitab.core.model.CategoryTotal
import com.hisabkitab.core.model.DateRange
import com.hisabkitab.core.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.YearMonth
import javax.inject.Inject

data class CategoriesUiState(
    val expense: List<Category> = emptyList(),
    val income: List<Category> = emptyList(),
    /** This month's total per category id. */
    val monthTotals: Map<Long, CategoryTotal> = emptyMap(),
    val isLoading: Boolean = true,
) {
    fun categoriesFor(type: TransactionType) = if (type == TransactionType.EXPENSE) expense else income
}

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    categoryRepository: CategoryRepository,
    transactionRepository: TransactionRepository,
    clock: Clock,
) : ViewModel() {

    private val thisMonth = DateRange.ofMonth(YearMonth.now(clock))

    val uiState: StateFlow<CategoriesUiState> = combine(
        categoryRepository.observeCategories(TransactionType.EXPENSE),
        categoryRepository.observeCategories(TransactionType.INCOME),
        transactionRepository.observeCategoryTotals(TransactionType.EXPENSE, thisMonth),
        transactionRepository.observeCategoryTotals(TransactionType.INCOME, thisMonth),
    ) { expense, income, expenseTotals, incomeTotals ->
        CategoriesUiState(
            expense = expense,
            income = income,
            monthTotals = (expenseTotals + incomeTotals).associateBy { it.category.id },
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoriesUiState())
}
