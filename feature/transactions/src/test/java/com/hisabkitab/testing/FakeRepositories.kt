package com.hisabkitab.testing

import com.hisabkitab.core.data.repository.CategoryRepository
import com.hisabkitab.core.data.repository.TransactionRepository
import com.hisabkitab.core.model.Category
import com.hisabkitab.core.model.CategoryTotal
import com.hisabkitab.core.model.DateRange
import com.hisabkitab.core.model.PeriodTotals
import com.hisabkitab.core.model.Transaction
import com.hisabkitab.core.model.TransactionDraft
import com.hisabkitab.core.model.TransactionFilter
import com.hisabkitab.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.ZoneOffset

class FakeCategoryRepository(initial: List<Category> = emptyList()) : CategoryRepository {
    val categories = MutableStateFlow(initial)

    override fun observeCategories(type: TransactionType): Flow<List<Category>> =
        categories.map { list -> list.filter { it.type == type } }

    override suspend fun getCategory(id: Long) = categories.value.firstOrNull { it.id == id }

    override suspend fun getAll() = categories.value

    override suspend fun isNameTaken(name: String, type: TransactionType, excludeId: Long?) =
        categories.value.any { it.type == type && it.id != excludeId && it.name.equals(name, ignoreCase = true) }

    override suspend fun save(category: Category): Long {
        val id = if (category.id == 0L) (categories.value.maxOfOrNull { it.id } ?: 0) + 1 else category.id
        categories.update { list -> list.filterNot { it.id == id } + category.copy(id = id) }
        return id
    }

    override suspend fun delete(id: Long) = categories.update { list -> list.filterNot { it.id == id } }

    override suspend fun transactionCount(categoryId: Long) = 0
}

/** Stores drafts in memory; only the operations the tests need are fully implemented. */
class FakeTransactionRepository(
    private val categories: FakeCategoryRepository,
) : TransactionRepository {
    val saved = mutableListOf<TransactionDraft>()
    private val transactions = MutableStateFlow<List<Transaction>>(emptyList())

    override fun observeRecent(limit: Int) = transactions.map { it.take(limit) }

    override fun observeTransactions(filter: TransactionFilter) = transactions

    override fun observeTransaction(id: Long) = transactions.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun getTransaction(id: Long) = transactions.value.firstOrNull { it.id == id }

    override suspend fun getAll() = transactions.value

    override fun observeTotals(range: DateRange) = MutableStateFlow(PeriodTotals())

    override fun observeTotal(type: TransactionType, range: DateRange) = MutableStateFlow(0L)

    override fun observeCategoryTotals(type: TransactionType, range: DateRange) =
        MutableStateFlow(emptyList<CategoryTotal>())

    override suspend fun hasTransactionsOn(date: LocalDate) =
        transactions.value.any { it.occurredAt.atZone(ZoneOffset.UTC).toLocalDate() == date }

    override suspend fun save(draft: TransactionDraft): Long {
        saved += draft
        val id = draft.id ?: saved.size.toLong()
        val category = requireNotNull(categories.getCategory(draft.categoryId))
        transactions.update { list ->
            list.filterNot { it.id == id } + Transaction(id, draft.amountMinor, category, draft.occurredAt, draft.note)
        }
        return id
    }

    override suspend fun delete(id: Long) = transactions.update { list -> list.filterNot { it.id == id } }
}
