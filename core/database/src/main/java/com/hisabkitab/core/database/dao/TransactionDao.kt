package com.hisabkitab.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.hisabkitab.core.database.entity.CategoryTotalRow
import com.hisabkitab.core.database.entity.TransactionEntity
import com.hisabkitab.core.database.entity.TransactionWithCategory
import com.hisabkitab.core.database.entity.TypeTotalRow
import com.hisabkitab.core.model.TransactionType
import kotlinx.coroutines.flow.Flow

/** All time ranges are half-open: `start <= occurred_at < end` (epoch millis). */
@Dao
interface TransactionDao {

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY occurred_at DESC, id DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<TransactionWithCategory>>

    /**
     * @param type null for all types.
     * @param query already LIKE-escaped with `\`; empty matches everything.
     */
    @Transaction
    @Query(
        """
        SELECT transactions.* FROM transactions
        INNER JOIN categories ON categories.id = transactions.category_id
        WHERE (:type IS NULL OR categories.type = :type)
          AND (:query = ''
               OR transactions.note LIKE '%' || :query || '%' ESCAPE '\'
               OR categories.name LIKE '%' || :query || '%' ESCAPE '\')
        ORDER BY transactions.occurred_at DESC, transactions.id DESC
        """,
    )
    fun observeFiltered(type: TransactionType?, query: String): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE occurred_at >= :start AND occurred_at < :end
        ORDER BY occurred_at DESC, id DESC
        """,
    )
    fun observeBetween(start: Long, end: Long): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :id")
    fun observeById(id: Long): Flow<TransactionWithCategory?>

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY occurred_at DESC, id DESC")
    suspend fun getAllWithCategory(): List<TransactionWithCategory>

    @Query(
        """
        SELECT categories.type AS type, SUM(transactions.amount_minor) AS total
        FROM transactions
        INNER JOIN categories ON categories.id = transactions.category_id
        WHERE transactions.occurred_at >= :start AND transactions.occurred_at < :end
        GROUP BY categories.type
        """,
    )
    fun observeTotalsByType(start: Long, end: Long): Flow<List<TypeTotalRow>>

    @Query(
        """
        SELECT COALESCE(SUM(transactions.amount_minor), 0)
        FROM transactions
        INNER JOIN categories ON categories.id = transactions.category_id
        WHERE categories.type = :type
          AND transactions.occurred_at >= :start AND transactions.occurred_at < :end
        """,
    )
    fun observeSum(type: TransactionType, start: Long, end: Long): Flow<Long>

    @Query(
        """
        SELECT categories.*, SUM(transactions.amount_minor) AS total,
               COUNT(transactions.id) AS transaction_count
        FROM transactions
        INNER JOIN categories ON categories.id = transactions.category_id
        WHERE categories.type = :type
          AND transactions.occurred_at >= :start AND transactions.occurred_at < :end
        GROUP BY categories.id
        ORDER BY total DESC
        """,
    )
    fun observeCategoryTotals(type: TransactionType, start: Long, end: Long): Flow<List<CategoryTotalRow>>

    @Query("SELECT EXISTS(SELECT 1 FROM transactions WHERE occurred_at >= :start AND occurred_at < :end)")
    suspend fun hasAnyBetween(start: Long, end: Long): Boolean

    @Query("SELECT COUNT(*) FROM transactions WHERE category_id = :categoryId")
    suspend fun countForCategory(categoryId: Long): Int

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int

    @Query("SELECT created_at FROM transactions WHERE id = :id")
    suspend fun getCreatedAt(id: Long): Long?

    /** Returns the new row id on insert, or -1 when an existing row was updated. */
    @Upsert
    suspend fun upsert(transaction: TransactionEntity): Long

    @Insert
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM transactions WHERE category_id = :categoryId")
    suspend fun deleteForCategory(categoryId: Long)
}
