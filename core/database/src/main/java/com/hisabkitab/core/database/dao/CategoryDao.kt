package com.hisabkitab.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.hisabkitab.core.database.entity.CategoryEntity
import com.hisabkitab.core.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY id")
    fun observeByType(type: TransactionType): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): CategoryEntity?

    @Query("SELECT * FROM categories ORDER BY type, id")
    suspend fun getAll(): List<CategoryEntity>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM categories
            WHERE type = :type AND name = :name COLLATE NOCASE AND id != :excludeId
        )
        """,
    )
    suspend fun existsWithName(name: String, type: TransactionType, excludeId: Long): Boolean

    /** Returns the new row id on insert, or -1 when an existing row was updated. */
    @Upsert
    suspend fun upsert(category: CategoryEntity): Long

    @Insert
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}
