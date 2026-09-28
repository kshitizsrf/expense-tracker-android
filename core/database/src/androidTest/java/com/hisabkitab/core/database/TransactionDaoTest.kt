package com.hisabkitab.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hisabkitab.core.database.entity.CategoryEntity
import com.hisabkitab.core.database.entity.TransactionEntity
import com.hisabkitab.core.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionDaoTest {

    private lateinit var database: HisabKitabDatabase

    private val food = CategoryEntity(id = 1, name = "Food", iconKey = "icon_55", color = 0, type = TransactionType.EXPENSE)
    private val rent = CategoryEntity(id = 2, name = "Rent", iconKey = "icon_80", color = 0, type = TransactionType.EXPENSE)
    private val salary = CategoryEntity(id = 3, name = "Salary", iconKey = "icon_132", color = 0, type = TransactionType.INCOME)

    @Before
    fun setUp() = runTest {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            HisabKitabDatabase::class.java,
        ).allowMainThreadQueries().build()
        database.categoryDao().insertAll(listOf(food, rent, salary))
        database.transactionDao().insertAll(
            listOf(
                transaction(1, 200_00, food.id, at = 1_000),
                transaction(2, 300_00, food.id, at = 2_000),
                transaction(3, 10_000_00, rent.id, at = 3_000, note = "September rent"),
                transaction(4, 50_000_00, salary.id, at = 4_000),
                transaction(5, 999_00, food.id, at = 10_000), // outside the queried range
            ),
        )
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun totalsAreGroupedByTypeWithinRange() = runTest {
        val totals = database.transactionDao().observeTotalsByType(start = 0, end = 5_000).first()
            .associate { it.type to it.total }

        assertEquals(10_500_00L, totals[TransactionType.EXPENSE])
        assertEquals(50_000_00L, totals[TransactionType.INCOME])
    }

    @Test
    fun categoryTotalsAreSortedDescending() = runTest {
        val rows = database.transactionDao().observeCategoryTotals(TransactionType.EXPENSE, 0, 5_000).first()

        assertEquals(listOf("Rent", "Food"), rows.map { it.category.name })
        assertEquals(500_00L, rows[1].total)
        assertEquals(2, rows[1].transactionCount)
    }

    @Test
    fun filterMatchesNotesAndCategoryNames() = runTest {
        val dao = database.transactionDao()

        assertEquals(listOf(3L), dao.observeFiltered(null, "rent").first().map { it.transaction.id })
        assertEquals(listOf(4L), dao.observeFiltered(TransactionType.INCOME, "").first().map { it.transaction.id })
        assertTrue(dao.observeFiltered(null, "100\\%").first().isEmpty())
    }

    @Test
    fun deletingCategoryCascadesToTransactions() = runTest {
        database.categoryDao().deleteById(food.id)

        assertEquals(0, database.transactionDao().countForCategory(food.id))
        assertEquals(2, database.transactionDao().count())
    }

    private fun transaction(id: Long, amount: Long, categoryId: Long, at: Long, note: String = "") =
        TransactionEntity(id = id, amountMinor = amount, categoryId = categoryId, occurredAt = at, note = note, createdAt = at)
}
