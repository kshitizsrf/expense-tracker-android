package com.hisabkitab.feature.budget

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisabkitab.feature.budget.R
import com.hisabkitab.core.designsystem.R as DesignR
import com.hisabkitab.core.common.money.parseAmountToMinorUnits
import com.hisabkitab.core.common.money.toPlainAmountString
import com.hisabkitab.core.common.time.localeFirstDayOfWeek
import com.hisabkitab.core.data.repository.UserPreferencesRepository
import com.hisabkitab.core.model.Budget
import com.hisabkitab.core.model.BudgetPeriod
import com.hisabkitab.core.model.DateRange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

data class BudgetUiState(
    val isLoading: Boolean = true,
    val hasExistingBudget: Boolean = false,
    val amountText: String = "",
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val customRange: DateRange? = null,
    val previewRange: DateRange? = null,
    @StringRes val error: Int? = null,
    val isFinished: Boolean = false,
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val budget = userPreferencesRepository.userPreferences.first().budget
            _uiState.update {
                withPreview(
                    it.copy(
                        isLoading = false,
                        hasExistingBudget = budget != null,
                        amountText = budget?.amountMinor?.toPlainAmountString().orEmpty(),
                        period = budget?.period ?: BudgetPeriod.MONTHLY,
                        customRange = budget?.customRange,
                    ),
                )
            }
        }
    }

    fun onAmountChange(text: String) {
        val cleaned = text.replace(',', '.')
        if (cleaned.isEmpty() || AMOUNT_PATTERN.matches(cleaned)) {
            _uiState.update { it.copy(amountText = cleaned, error = null) }
        }
    }

    fun onPeriodChange(period: BudgetPeriod) = _uiState.update { withPreview(it.copy(period = period, error = null)) }

    fun onCustomRangeSelected(range: DateRange) =
        _uiState.update { withPreview(it.copy(period = BudgetPeriod.CUSTOM, customRange = range, error = null)) }

    fun save() {
        val state = _uiState.value
        val amount = parseAmountToMinorUnits(state.amountText)
        val error = when {
            amount == null || amount <= 0 -> DesignR.string.error_amount_required
            state.period == BudgetPeriod.CUSTOM && state.customRange == null -> R.string.error_dates_required
            else -> null
        }
        if (error != null || amount == null) {
            _uiState.update { it.copy(error = error) }
            return
        }
        viewModelScope.launch {
            userPreferencesRepository.setBudget(
                Budget(
                    amountMinor = amount,
                    period = state.period,
                    customRange = state.customRange.takeIf { state.period == BudgetPeriod.CUSTOM },
                ),
            )
            _uiState.update { it.copy(isFinished = true) }
        }
    }

    fun remove() {
        viewModelScope.launch {
            userPreferencesRepository.setBudget(null)
            _uiState.update { it.copy(isFinished = true) }
        }
    }

    private fun withPreview(state: BudgetUiState): BudgetUiState {
        val preview = if (state.period == BudgetPeriod.CUSTOM && state.customRange == null) {
            null
        } else {
            Budget(0, state.period, state.customRange).rangeFor(LocalDate.now(clock), localeFirstDayOfWeek())
        }
        return state.copy(previewRange = preview)
    }

    private companion object {
        val AMOUNT_PATTERN = Regex("""^\d{0,9}(\.\d{0,2})?$""")
    }
}
