package com.hisabkitab.feature.transactions.editor

import com.hisabkitab.core.designsystem.R as DesignR
import com.hisabkitab.core.common.calculator.AmountInput
import com.hisabkitab.core.model.Category
import com.hisabkitab.core.model.TransactionDraft
import com.hisabkitab.core.model.TransactionType
import com.hisabkitab.testing.FakeCategoryRepository
import com.hisabkitab.testing.FakeTransactionRepository
import com.hisabkitab.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val food = Category(1, "Food", "icon_55", 0, TransactionType.EXPENSE)
    private val travel = Category(2, "Travel", "icon_2", 0, TransactionType.EXPENSE)
    private val salary = Category(3, "Salary", "icon_132", 0, TransactionType.INCOME)

    private val clock = Clock.fixed(Instant.parse("2026-09-17T10:15:30Z"), ZoneOffset.UTC)
    private val categories = FakeCategoryRepository(listOf(food, travel, salary))
    private val transactions = FakeTransactionRepository(categories)

    /** Creates the view model and keeps its state flow collected for the duration of the test. */
    private fun TestScope.createViewModel(id: Long? = null): TransactionEditorViewModel {
        val viewModel = TransactionEditorViewModel(id, TransactionType.EXPENSE, transactions, categories, clock)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel
    }

    @Test
    fun selectsFirstCategoryOfTypeByDefault() = runTest {
        val viewModel = createViewModel()
        assertEquals(food.id, viewModel.uiState.value.selectedCategoryId)

        viewModel.onTypeChange(TransactionType.INCOME)
        assertEquals(salary.id, viewModel.uiState.value.selectedCategoryId)
    }

    @Test
    fun keypadEvaluatesExpressionIntoAmount() = runTest {
        val viewModel = createViewModel()

        "120".forEach(viewModel::onDigit)
        viewModel.onOperator(AmountInput.PLUS)
        "30".forEach(viewModel::onDigit)

        val state = viewModel.uiState.value
        assertTrue(state.showsCalculation)
        assertEquals(150_00L, state.amountMinor)
        assertTrue(state.canSave)
    }

    @Test
    fun cannotSaveWithoutAmount() = runTest {
        val viewModel = createViewModel()

        viewModel.save()

        assertFalse(viewModel.uiState.value.isSaved)
        assertEquals(DesignR.string.error_amount_required, viewModel.uiState.value.errorMessage)
        assertTrue(transactions.saved.isEmpty())
    }

    @Test
    fun savesDraftWithSelectedValues() = runTest {
        val viewModel = createViewModel()

        "250".forEach(viewModel::onDigit)
        viewModel.onCategorySelected(travel.id)
        viewModel.onNoteChange("Cab to airport")
        viewModel.save()

        assertTrue(viewModel.uiState.value.isSaved)
        val draft = transactions.saved.single()
        assertEquals(250_00L, draft.amountMinor)
        assertEquals(travel.id, draft.categoryId)
        assertEquals("Cab to airport", draft.note)
        assertEquals(Instant.parse("2026-09-17T10:15:00Z"), draft.occurredAt)
    }

    @Test
    fun editingLoadsExistingTransaction() = runTest {
        val id = transactions.save(
            TransactionDraft(
                id = null,
                amountMinor = 45_50,
                categoryId = travel.id,
                occurredAt = Instant.parse("2026-09-10T08:30:00Z"),
                note = "Metro card",
            ),
        )

        val state = createViewModel(id).uiState.value

        assertTrue(state.isEditing)
        assertFalse(state.isLoading)
        assertEquals("45.5", state.expression)
        assertEquals(travel.id, state.selectedCategoryId)
        assertEquals("Metro card", state.note)
    }
}
