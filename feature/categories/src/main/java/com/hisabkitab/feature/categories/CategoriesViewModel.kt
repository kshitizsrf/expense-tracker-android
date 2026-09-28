package com.hisabkitab.feature.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisabkitab.core.data.repository.CategoryRepository
import com.hisabkitab.core.model.Category
import com.hisabkitab.core.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class CategoriesUiState(
    val expense: List<Category> = emptyList(),
    val income: List<Category> = emptyList(),
    val isLoading: Boolean = true,
) {
    fun categoriesFor(type: TransactionType) = if (type == TransactionType.EXPENSE) expense else income
}

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    categoryRepository: CategoryRepository,
) : ViewModel() {

    val uiState: StateFlow<CategoriesUiState> = combine(
        categoryRepository.observeCategories(TransactionType.EXPENSE),
        categoryRepository.observeCategories(TransactionType.INCOME),
    ) { expense, income ->
        CategoriesUiState(expense = expense, income = income, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoriesUiState())
}
