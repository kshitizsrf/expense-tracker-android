package com.hisabkitab.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisabkitab.core.common.time.toLocalDate
import com.hisabkitab.core.data.repository.TransactionRepository
import com.hisabkitab.core.model.Transaction
import com.hisabkitab.core.model.TransactionFilter
import com.hisabkitab.core.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

data class TransactionDayGroup(
    val date: LocalDate,
    val transactions: List<Transaction>,
    val netMinor: Long,
)

data class TransactionsUiState(
    val typeFilter: TransactionType? = null,
    val query: String = "",
    val groups: List<TransactionDayGroup> = emptyList(),
    val today: LocalDate,
    val isLoading: Boolean = true,
) {
    val isFiltering: Boolean get() = typeFilter != null || query.isNotBlank()
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    private val clock: Clock,
) : ViewModel() {

    private val typeFilter = MutableStateFlow<TransactionType?>(null)
    private val query = MutableStateFlow("")

    private val groups = combine(
        typeFilter,
        query.debounce(SEARCH_DEBOUNCE_MS).onStart { emit(query.value) }.distinctUntilChanged(),
    ) { type, text -> TransactionFilter(type = type, query = text) }
        .flatMapLatest { filter -> transactionRepository.observeTransactions(filter) }
        .map { transactions -> transactions.groupByDay() }

    val uiState: StateFlow<TransactionsUiState> = combine(typeFilter, query, groups) { type, text, groups ->
        TransactionsUiState(
            typeFilter = type,
            query = text,
            groups = groups,
            today = LocalDate.now(clock),
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionsUiState(today = LocalDate.now(clock)),
    )

    fun onTypeFilterChange(type: TransactionType?) {
        typeFilter.value = type
    }

    fun onQueryChange(text: String) {
        query.value = text
    }

    private fun List<Transaction>.groupByDay(): List<TransactionDayGroup> =
        groupBy { it.occurredAt.toLocalDate(clock.zone) }.map { (date, items) ->
            TransactionDayGroup(
                date = date,
                transactions = items,
                netMinor = items.sumOf { if (it.type == TransactionType.INCOME) it.amountMinor else -it.amountMinor },
            )
        }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 250L
    }
}
