package com.hisabkitab.core.data.repository

import androidx.room.withTransaction
import com.hisabkitab.core.database.HisabKitabDatabase
import com.hisabkitab.core.database.dao.CategoryDao
import com.hisabkitab.core.database.dao.TransactionDao
import com.hisabkitab.core.database.entity.asEntity
import com.hisabkitab.core.database.entity.asModel
import com.hisabkitab.core.model.Category
import com.hisabkitab.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface CategoryRepository {
    fun observeCategories(type: TransactionType): Flow<List<Category>>
    suspend fun getCategory(id: Long): Category?
    suspend fun getAll(): List<Category>
    suspend fun isNameTaken(name: String, type: TransactionType, excludeId: Long?): Boolean

    /** Inserts when [Category.id] is 0, otherwise updates. Returns the category id. */
    suspend fun save(category: Category): Long

    /** Deletes the category and every transaction in it. */
    suspend fun delete(id: Long)
    suspend fun transactionCount(categoryId: Long): Int
}

class RoomCategoryRepository @Inject constructor(
    private val database: HisabKitabDatabase,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
) : CategoryRepository {

    override fun observeCategories(type: TransactionType): Flow<List<Category>> =
        categoryDao.observeByType(type).map { entities -> entities.map { it.asModel() } }

    override suspend fun getCategory(id: Long): Category? = categoryDao.getById(id)?.asModel()

    override suspend fun getAll(): List<Category> = categoryDao.getAll().map { it.asModel() }

    override suspend fun isNameTaken(name: String, type: TransactionType, excludeId: Long?): Boolean =
        categoryDao.existsWithName(name.trim(), type, excludeId ?: -1L)

    override suspend fun save(category: Category): Long {
        val rowId = categoryDao.upsert(category.copy(name = category.name.trim()).asEntity())
        return if (category.id == 0L) rowId else category.id
    }

    override suspend fun delete(id: Long) {
        database.withTransaction {
            // The foreign key cascades too; deleting explicitly keeps this correct regardless.
            transactionDao.deleteForCategory(id)
            categoryDao.deleteById(id)
        }
    }

    override suspend fun transactionCount(categoryId: Long): Int = transactionDao.countForCategory(categoryId)
}
