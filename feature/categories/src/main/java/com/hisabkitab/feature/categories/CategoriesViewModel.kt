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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.YearMonth
import javax.inject.Inject

data class CategoriesUiState(
    val expense: List<Category> = emptyList(),
    val income: List<Category> = emptyList(),
    /** This month's total per category id. */
    val monthTotals: Map<Long, CategoryTotal> = emptyMap(),
    val isLoading: Boolean = true,
    /** Categories waiting for the user to confirm deletion, with how many transactions go too. */
    val pendingDelete: PendingCategoryDelete? = null,
) {
    fun categoriesFor(type: TransactionType) = if (type == TransactionType.EXPENSE) expense else income
}

data class PendingCategoryDelete(val ids: Set<Long>, val transactionCount: Int)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    transactionRepository: TransactionRepository,
    clock: Clock,
) : ViewModel() {

    private val thisMonth = DateRange.ofMonth(YearMonth.now(clock))
    private val pendingDelete = MutableStateFlow<PendingCategoryDelete?>(null)

    val uiState: StateFlow<CategoriesUiState> = combine(
        categoryRepository.observeCategories(TransactionType.EXPENSE),
        categoryRepository.observeCategories(TransactionType.INCOME),
        transactionRepository.observeCategoryTotals(TransactionType.EXPENSE, thisMonth),
        transactionRepository.observeCategoryTotals(TransactionType.INCOME, thisMonth),
        pendingDelete,
    ) { expense, income, expenseTotals, incomeTotals, pending ->
        CategoriesUiState(
            expense = expense,
            income = income,
            monthTotals = (expenseTotals + incomeTotals).associateBy { it.category.id },
            isLoading = false,
            pendingDelete = pending,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoriesUiState())

    /** Counts the transactions that would go with [ids], then asks for confirmation. */
    fun requestDelete(ids: Set<Long>) {
        if (ids.isEmpty()) return
        viewModelScope.launch {
            pendingDelete.value = PendingCategoryDelete(ids, ids.sumOf { categoryRepository.transactionCount(it) })
        }
    }

    fun confirmDelete() {
        val pending = pendingDelete.value ?: return
        pendingDelete.value = null
        viewModelScope.launch { categoryRepository.deleteAll(pending.ids) }
    }

    fun cancelDelete() {
        pendingDelete.value = null
    }
}
