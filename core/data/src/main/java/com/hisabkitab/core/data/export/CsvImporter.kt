package com.hisabkitab.core.data.export

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.hisabkitab.core.common.di.IoDispatcher
import com.hisabkitab.core.common.money.parseAmountToMinorUnits
import com.hisabkitab.core.data.repository.CategoryRepository
import com.hisabkitab.core.data.repository.TransactionRepository
import com.hisabkitab.core.database.HisabKitabDatabase
import com.hisabkitab.core.model.Category
import com.hisabkitab.core.model.CategoryColors
import com.hisabkitab.core.model.TransactionDraft
import com.hisabkitab.core.model.TransactionType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/** Outcome of an import; [skipped] rows were duplicates, [invalid] rows could not be read. */
data class ImportResult(val imported: Int, val skipped: Int, val invalid: Int, val newCategories: Int)

/** The file is not a Hisab Kitab transactions export (missing required columns). */
class UnsupportedCsvException : IOException("Not a transactions CSV")

/** A transaction row read from CSV, before categories are resolved. */
internal data class CsvTransaction(
    val occurredAt: Instant,
    val type: TransactionType,
    val categoryName: String,
    val amountMinor: Long,
    val note: String,
)

/**
 * Reads a CSV in the format [CsvExporter.exportTransactions] writes
 * (Date, Time, Type, Category, Amount, Note), so exports can be moved between phones.
 * Unknown categories are created; rows already present are skipped, so importing the
 * same file twice does not duplicate anything.
 */
class CsvImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: HisabKitabDatabase,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val clock: Clock,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    suspend fun importTransactions(source: Uri): ImportResult = withContext(ioDispatcher) {
        val text = context.contentResolver.openInputStream(source)?.bufferedReader()?.use { it.readText() }
            ?: throw IOException("Cannot open $source")
        val parsed = parseTransactions(text, clock.zone)

        database.withTransaction {
            val categories = categoryRepository.getAll().toMutableList()
            val existing = transactionRepository.getAll()
                .mapTo(HashSet()) { fingerprint(it.occurredAt, it.amountMinor, it.category.id, it.note) }
            var imported = 0
            var skipped = 0
            var created = 0
            parsed.rows.forEach { row ->
                val category = categories.firstOrNull { it.type == row.type && it.name.equals(row.categoryName, ignoreCase = true) }
                    ?: run {
                        val new = Category(
                            id = 0,
                            name = row.categoryName,
                            iconKey = DEFAULT_ICON_KEY,
                            color = CategoryColors.forIndex(categories.size),
                            type = row.type,
                        )
                        created++
                        new.copy(id = categoryRepository.save(new)).also(categories::add)
                    }
                if (!existing.add(fingerprint(row.occurredAt, row.amountMinor, category.id, row.note))) {
                    skipped++
                } else {
                    transactionRepository.save(TransactionDraft(null, row.amountMinor, category.id, row.occurredAt, row.note))
                    imported++
                }
            }
            ImportResult(imported = imported, skipped = skipped, invalid = parsed.invalid, newCategories = created)
        }
    }

    private data class Fingerprint(val at: Instant, val amount: Long, val categoryId: Long, val note: String)

    /** Exports keep times to the minute, so duplicates are matched to the minute too. */
    private fun fingerprint(at: Instant, amount: Long, categoryId: Long, note: String) =
        Fingerprint(at.truncatedTo(ChronoUnit.MINUTES), amount, categoryId, note.trim())

    internal data class Parsed(val rows: List<CsvTransaction>, val invalid: Int)

    internal companion object {
        /** The generic "other" icon from CategoryIcons. */
        const val DEFAULT_ICON_KEY = "icon_97"
        private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("H:mm")
        private val DEFAULT_TIME: LocalTime = LocalTime.NOON

        /** Pure parsing step, separated from storage so it can be unit tested. */
        fun parseTransactions(text: String, zone: ZoneId): Parsed {
            val rows = Csv.parse(text)
            val header = rows.firstOrNull()?.map { it.trim().lowercase() } ?: throw UnsupportedCsvException()
            fun column(name: String) = header.indexOf(name)
            val date = column("date")
            val time = column("time")
            val type = column("type")
            val category = column("category")
            val amount = column("amount")
            val note = column("note")
            if (date < 0 || type < 0 || category < 0 || amount < 0) throw UnsupportedCsvException()

            var invalid = 0
            val parsed = rows.drop(1).mapNotNull { cells ->
                fun cell(index: Int) = if (index >= 0) cells.getOrNull(index)?.trim().orEmpty() else ""
                val row = runCatching {
                    val day = LocalDate.parse(cell(date))
                    val clockTime = cell(time).takeIf { it.isNotEmpty() }?.let { LocalTime.parse(it, TIME_FORMAT) } ?: DEFAULT_TIME
                    val kind = when (cell(type).lowercase()) {
                        "expense" -> TransactionType.EXPENSE
                        "income" -> TransactionType.INCOME
                        else -> null
                    }
                    val minor = parseAmountToMinorUnits(cell(amount))?.takeIf { it > 0 }
                    val name = cell(category)
                    if (kind == null || minor == null || name.isEmpty()) {
                        null
                    } else {
                        CsvTransaction(day.atTime(clockTime).atZone(zone).toInstant(), kind, name, minor, cell(note))
                    }
                }.getOrNull()
                if (row == null) invalid++
                row
            }
            return Parsed(parsed, invalid)
        }
    }
}
