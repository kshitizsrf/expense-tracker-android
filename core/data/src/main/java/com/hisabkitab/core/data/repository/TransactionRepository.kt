package com.hisabkitab.core.data.repository

import com.hisabkitab.core.common.time.endExclusiveEpochMillis
import com.hisabkitab.core.common.time.startEpochMillis
import com.hisabkitab.core.database.dao.TransactionDao
import com.hisabkitab.core.database.entity.TransactionEntity
import com.hisabkitab.core.database.entity.asModel
import com.hisabkitab.core.model.CategoryTotal
import com.hisabkitab.core.model.DateRange
import com.hisabkitab.core.model.PeriodTotals
import com.hisabkitab.core.model.Transaction
import com.hisabkitab.core.model.TransactionDraft
import com.hisabkitab.core.model.TransactionFilter
import com.hisabkitab.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

interface TransactionRepository {
    fun observeRecent(limit: Int): Flow<List<Transaction>>
    fun observeTransactions(filter: TransactionFilter): Flow<List<Transaction>>
    fun observeTransaction(id: Long): Flow<Transaction?>

    /** Every transaction in [range], newest first. Used for charts and summaries. */
    fun observeTransactionsIn(range: DateRange): Flow<List<Transaction>>
    suspend fun getTransaction(id: Long): Transaction?
    suspend fun getAll(): List<Transaction>
    fun observeTotals(range: DateRange): Flow<PeriodTotals>
    fun observeTotal(type: TransactionType, range: DateRange): Flow<Long>
    fun observeCategoryTotals(type: TransactionType, range: DateRange): Flow<List<CategoryTotal>>
    suspend fun hasTransactionsOn(date: LocalDate): Boolean

    /** Inserts when [TransactionDraft.id] is null, otherwise updates. Returns the id. */
    suspend fun save(draft: TransactionDraft): Long
    suspend fun delete(id: Long)
}

class RoomTransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val clock: Clock,
) : TransactionRepository {

    private val zone get() = clock.zone

    override fun observeRecent(limit: Int): Flow<List<Transaction>> =
        transactionDao.observeRecent(limit).map { rows -> rows.map { it.asModel() } }

    override fun observeTransactions(filter: TransactionFilter): Flow<List<Transaction>> =
        transactionDao.observeFiltered(filter.type, filter.query.trim().escapeForLike())
            .map { rows -> rows.map { it.asModel() } }

    override fun observeTransaction(id: Long): Flow<Transaction?> =
        transactionDao.observeById(id).map { it?.asModel() }

    override fun observeTransactionsIn(range: DateRange): Flow<List<Transaction>> =
        transactionDao.observeBetween(range.startEpochMillis(zone), range.endExclusiveEpochMillis(zone))
            .map { rows -> rows.map { it.asModel() } }

    override suspend fun getTransaction(id: Long): Transaction? = observeTransaction(id).first()

    override suspend fun getAll(): List<Transaction> = transactionDao.getAllWithCategory().map { it.asModel() }

    override fun observeTotals(range: DateRange): Flow<PeriodTotals> =
        transactionDao.observeTotalsByType(range.startEpochMillis(zone), range.endExclusiveEpochMillis(zone))
            .map { rows ->
                PeriodTotals(
                    incomeMinor = rows.firstOrNull { it.type == TransactionType.INCOME }?.total ?: 0,
                    expenseMinor = rows.firstOrNull { it.type == TransactionType.EXPENSE }?.total ?: 0,
                )
            }

    override fun observeTotal(type: TransactionType, range: DateRange): Flow<Long> =
        transactionDao.observeSum(type, range.startEpochMillis(zone), range.endExclusiveEpochMillis(zone))

    override fun observeCategoryTotals(type: TransactionType, range: DateRange): Flow<List<CategoryTotal>> =
        transactionDao.observeCategoryTotals(type, range.startEpochMillis(zone), range.endExclusiveEpochMillis(zone))
            .map { rows -> rows.map { it.asModel() } }

    override suspend fun hasTransactionsOn(date: LocalDate): Boolean {
        val day = DateRange.ofDay(date)
        return transactionDao.hasAnyBetween(day.startEpochMillis(zone), day.endExclusiveEpochMillis(zone))
    }

    override suspend fun save(draft: TransactionDraft): Long {
        val createdAt = draft.id?.let { transactionDao.getCreatedAt(it) } ?: clock.millis()
        val entity = TransactionEntity(
            id = draft.id ?: 0,
            amountMinor = draft.amountMinor,
            categoryId = draft.categoryId,
            occurredAt = draft.occurredAt.toEpochMilli(),
            note = draft.note.trim(),
            createdAt = createdAt,
        )
        val rowId = transactionDao.upsert(entity)
        return draft.id ?: rowId
    }

    override suspend fun delete(id: Long) = transactionDao.deleteById(id)

    /** Escapes LIKE wildcards so user input is matched literally (paired with `ESCAPE '\'`). */
    private fun String.escapeForLike(): String =
        replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
}
