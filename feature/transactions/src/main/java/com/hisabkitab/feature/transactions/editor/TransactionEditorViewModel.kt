package com.hisabkitab.feature.transactions.editor

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisabkitab.feature.transactions.R
import com.hisabkitab.core.designsystem.R as DesignR
import com.hisabkitab.core.common.calculator.AmountInput
import com.hisabkitab.core.common.calculator.ExpressionEvaluator
import com.hisabkitab.core.common.money.MAX_AMOUNT_MINOR
import com.hisabkitab.core.common.money.toMinorUnits
import com.hisabkitab.core.common.money.toPlainAmountString
import com.hisabkitab.core.data.repository.CategoryRepository
import com.hisabkitab.core.data.repository.TransactionRepository
import com.hisabkitab.core.model.Category
import com.hisabkitab.core.model.TransactionDraft
import com.hisabkitab.core.model.TransactionType
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class TransactionEditorUiState(
    val isEditing: Boolean,
    val isLoading: Boolean = false,
    val type: TransactionType = TransactionType.EXPENSE,
    val expression: String = "",
    val amountMinor: Long? = null,
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val date: LocalDate,
    val time: LocalTime,
    val note: String = "",
    @StringRes val errorMessage: Int? = null,
    val isSaved: Boolean = false,
) {
    val showsCalculation: Boolean get() = AmountInput.hasOperator(expression)
    val canSave: Boolean get() = !isLoading && (amountMinor ?: 0) > 0 && selectedCategoryId != null
}

/** Form fields owned by the view model; categories are merged in from the repository. */
private data class EditorForm(
    val isLoading: Boolean,
    val type: TransactionType,
    val expression: String = "",
    val selectedCategoryId: Long? = null,
    val date: LocalDate,
    val time: LocalTime,
    val note: String = "",
    @StringRes val errorMessage: Int? = null,
    val isSaved: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = TransactionEditorViewModel.Factory::class)
class TransactionEditorViewModel @AssistedInject constructor(
    @Assisted private val transactionId: Long?,
    @Assisted initialType: TransactionType,
    private val transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    private val clock: Clock,
) : ViewModel() {

    private val form = MutableStateFlow(
        EditorForm(
            isLoading = transactionId != null,
            type = initialType,
            date = LocalDate.now(clock),
            time = LocalTime.now(clock).withSecond(0).withNano(0),
        ),
    )

    private val categories = form
        .map { it.type }
        .distinctUntilChanged()
        .flatMapLatest { type -> categoryRepository.observeCategories(type) }

    val uiState: StateFlow<TransactionEditorUiState> = combine(form, categories) { form, categories ->
        // Default to the first category until the user picks one (or the picked one is deleted).
        val selectedId = form.selectedCategoryId?.takeIf { id -> categories.any { it.id == id } }
            ?: categories.firstOrNull()?.id
        TransactionEditorUiState(
            isEditing = transactionId != null,
            isLoading = form.isLoading,
            type = form.type,
            expression = form.expression,
            amountMinor = evaluate(form.expression),
            categories = categories,
            selectedCategoryId = selectedId,
            date = form.date,
            time = form.time,
            note = form.note,
            errorMessage = form.errorMessage,
            isSaved = form.isSaved,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionEditorUiState(
            isEditing = transactionId != null,
            isLoading = true,
            type = initialType,
            date = form.value.date,
            time = form.value.time,
        ),
    )

    init {
        if (transactionId != null) loadTransaction(transactionId)
    }

    private fun loadTransaction(id: Long) {
        viewModelScope.launch {
            val transaction = transactionRepository.getTransaction(id)
            if (transaction == null) {
                form.update { it.copy(isLoading = false, errorMessage = R.string.transaction_not_found_title) }
                return@launch
            }
            val dateTime = transaction.occurredAt.atZone(clock.zone)
            form.update {
                it.copy(
                    isLoading = false,
                    type = transaction.type,
                    expression = transaction.amountMinor.toPlainAmountString(),
                    selectedCategoryId = transaction.category.id,
                    date = dateTime.toLocalDate(),
                    time = dateTime.toLocalTime().withSecond(0).withNano(0),
                    note = transaction.note,
                )
            }
        }
    }

    fun onTypeChange(type: TransactionType) = form.update {
        if (it.type == type) it else it.copy(type = type, selectedCategoryId = null)
    }

    fun onDigit(digit: Char) = updateExpression { AmountInput.appendDigit(it, digit) }

    fun onDecimalPoint() = updateExpression { AmountInput.appendDecimalPoint(it) }

    fun onOperator(operator: Char) = updateExpression { AmountInput.appendOperator(it, operator) }

    fun onBackspace() = updateExpression { AmountInput.backspace(it) }

    fun onClearAmount() = updateExpression { "" }

    fun onCategorySelected(id: Long) = form.update { it.copy(selectedCategoryId = id) }

    fun onDateSelected(date: LocalDate) = form.update { it.copy(date = date) }

    fun onTimeSelected(time: LocalTime) = form.update { it.copy(time = time) }

    fun onNoteChange(note: String) = form.update { it.copy(note = note.take(MAX_NOTE_LENGTH)) }

    fun onErrorShown() = form.update { it.copy(errorMessage = null) }

    fun save() {
        val state = uiState.value
        val amount = state.amountMinor
        val categoryId = state.selectedCategoryId
        val error = when {
            amount == null || amount <= 0 -> DesignR.string.error_amount_required
            amount > MAX_AMOUNT_MINOR -> R.string.error_amount_too_large
            categoryId == null -> R.string.error_category_required
            else -> null
        }
        if (error != null || amount == null || categoryId == null) {
            form.update { it.copy(errorMessage = error) }
            return
        }
        viewModelScope.launch {
            transactionRepository.save(
                TransactionDraft(
                    id = transactionId,
                    amountMinor = amount,
                    categoryId = categoryId,
                    occurredAt = LocalDateTime.of(state.date, state.time).atZone(clock.zone).toInstant(),
                    note = state.note,
                ),
            )
            form.update { it.copy(isSaved = true) }
        }
    }

    private fun updateExpression(transform: (String) -> String) = form.update {
        it.copy(expression = transform(it.expression), errorMessage = null)
    }

    private fun evaluate(expression: String): Long? =
        ExpressionEvaluator.evaluate(expression)?.takeIf { it.signum() >= 0 }?.toMinorUnits()

    @AssistedFactory
    interface Factory {
        fun create(transactionId: Long?, initialType: TransactionType): TransactionEditorViewModel
    }

    private companion object {
        const val MAX_NOTE_LENGTH = 200
    }
}
