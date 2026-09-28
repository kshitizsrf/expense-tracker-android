package com.hisabkitab.feature.transactions.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisabkitab.core.data.repository.TransactionRepository
import com.hisabkitab.core.model.Transaction
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface TransactionDetailUiState {
    data object Loading : TransactionDetailUiState
    data object NotFound : TransactionDetailUiState
    data class Success(val transaction: Transaction) : TransactionDetailUiState
    data object Deleted : TransactionDetailUiState
}

@HiltViewModel(assistedFactory = TransactionDetailViewModel.Factory::class)
class TransactionDetailViewModel @AssistedInject constructor(
    @Assisted private val transactionId: Long,
    private val transactionRepository: TransactionRepository,
) : ViewModel() {

    private enum class DeleteState { IDLE, DELETING, DONE }

    private val deleteState = MutableStateFlow(DeleteState.IDLE)

    val uiState: StateFlow<TransactionDetailUiState> = combine(
        transactionRepository.observeTransaction(transactionId),
        deleteState,
    ) { transaction, delete ->
        when {
            delete == DeleteState.DONE -> TransactionDetailUiState.Deleted
            delete == DeleteState.DELETING -> TransactionDetailUiState.Loading
            transaction == null -> TransactionDetailUiState.NotFound
            else -> TransactionDetailUiState.Success(transaction)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionDetailUiState.Loading)

    fun delete() {
        if (deleteState.value != DeleteState.IDLE) return
        deleteState.value = DeleteState.DELETING
        viewModelScope.launch {
            // Finish the delete before signalling the screen to close (closing clears this scope).
            transactionRepository.delete(transactionId)
            deleteState.value = DeleteState.DONE
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(transactionId: Long): TransactionDetailViewModel
    }
}
