package com.hisabkitab.core.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.room.withTransaction
import com.hisabkitab.core.common.di.IoDispatcher
import com.hisabkitab.core.database.entity.CategoryEntity
import com.hisabkitab.core.database.entity.TransactionEntity
import com.hisabkitab.core.model.CategoryColors
import com.hisabkitab.core.model.TransactionType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToLong

/**
 * One-time import of data from version 1 of the app, which used a hand-written SQLite database
 * (`expense_tracker.db`) with dates stored as text such as "Jan 05, 2024" and "06:30 PM".
 * The old file is deleted after a successful import so this runs at most once.
 */
@Singleton
class LegacyDatabaseImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: HisabKitabDatabase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    /** @return true if legacy data was found and imported. */
    suspend fun importIfPresent(): Boolean = withContext(ioDispatcher) {
        val file = context.getDatabasePath(LEGACY_DATABASE_NAME)
        if (!file.exists()) return@withContext false

        val legacy = try {
            readLegacyData(file)
        } catch (e: Exception) {
            Log.w(TAG, "Could not read legacy database; leaving it in place", e)
            return@withContext false
        }

        val imported = if (legacy.categories.isNotEmpty() && database.transactionDao().count() == 0) {
            database.withTransaction {
                // Replace the freshly seeded defaults with the user's own categories.
                database.categoryDao().deleteAll()
                database.categoryDao().insertAll(legacy.categories)
                database.transactionDao().insertAll(legacy.transactions)
            }
            true
        } else {
            false
        }
        context.deleteDatabase(LEGACY_DATABASE_NAME)
        imported
    }

    private data class LegacyData(
        val categories: List<CategoryEntity>,
        val transactions: List<TransactionEntity>,
    )

    private fun readLegacyData(file: File): LegacyData {
        SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            val categories = mutableListOf<CategoryEntity>()
            db.rawQuery("SELECT category_id, category_name, category_icon, type FROM Category", null).use { c ->
                while (c.moveToNext()) {
                    categories += CategoryEntity(
                        id = c.getLong(0),
                        name = c.getString(1)?.trim().orEmpty().ifEmpty { "Untitled" },
                        iconKey = c.getString(2)?.takeIf { it.isNotBlank() } ?: "icon_97",
                        color = CategoryColors.forIndex(categories.size),
                        type = if (c.getString(3).equals("Income", ignoreCase = true)) {
                            TransactionType.INCOME
                        } else {
                            TransactionType.EXPENSE
                        },
                    )
                }
            }

            val categoryIds = categories.mapTo(HashSet()) { it.id }
            val now = System.currentTimeMillis()
            val zone = ZoneId.systemDefault()
            val transactions = mutableListOf<TransactionEntity>()
            db.rawQuery("SELECT transaction_id, amount, date, time, category_id, note FROM Transactions", null).use { c ->
                while (c.moveToNext()) {
                    val categoryId = c.getLong(4)
                    // Transactions whose category was deleted were already invisible in v1.
                    if (categoryId !in categoryIds) continue
                    val occurredAt = parseLegacyDateTime(c.getString(2), c.getString(3))
                        ?.atZone(zone)?.toInstant()?.toEpochMilli() ?: now
                    transactions += TransactionEntity(
                        id = c.getLong(0),
                        amountMinor = (c.getDouble(1) * 100).roundToLong(),
                        categoryId = categoryId,
                        occurredAt = occurredAt,
                        note = c.getString(5)?.trim().orEmpty(),
                        createdAt = occurredAt,
                    )
                }
            }
            return LegacyData(categories, transactions)
        }
    }

    private fun parseLegacyDateTime(date: String?, time: String?): LocalDateTime? {
        val localDate = date?.trim()?.let { text ->
            dateFormatters.firstNotNullOfOrNull { runCatching { LocalDate.parse(text, it) }.getOrNull() }
        } ?: return null
        val localTime = time?.trim()?.let { text ->
            timeFormatters.firstNotNullOfOrNull { runCatching { LocalTime.parse(text, it) }.getOrNull() }
        } ?: LocalTime.NOON
        return LocalDateTime.of(localDate, localTime)
    }

    private val legacyLocales = listOf(Locale.ENGLISH, Locale.getDefault()).distinct()

    private val dateFormatters: List<DateTimeFormatter> = legacyLocales.map {
        DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("MMM dd, yyyy").toFormatter(it)
    }

    private val timeFormatters: List<DateTimeFormatter> = legacyLocales.map {
        DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("hh:mm a").toFormatter(it)
    }

    private companion object {
        const val TAG = "LegacyImport"
        const val LEGACY_DATABASE_NAME = "expense_tracker.db"
    }
}
