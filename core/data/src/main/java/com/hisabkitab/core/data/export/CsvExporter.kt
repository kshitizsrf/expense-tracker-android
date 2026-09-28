package com.hisabkitab.core.data.export

import android.content.Context
import android.net.Uri
import com.hisabkitab.core.common.di.IoDispatcher
import com.hisabkitab.core.common.money.toMajorUnits
import com.hisabkitab.core.data.repository.CategoryRepository
import com.hisabkitab.core.data.repository.TransactionRepository
import com.hisabkitab.core.model.TransactionType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.Writer
import java.time.Clock
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Writes data as CSV to a document the user picked with the Storage Access Framework,
 * so no storage permission is needed (replaces the old AsyncTask writing to Downloads).
 */
class CsvExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val clock: Clock,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    /** @return number of transactions written. */
    suspend fun exportTransactions(destination: Uri): Int = withContext(ioDispatcher) {
        val transactions = transactionRepository.getAll()
        write(destination) { writer ->
            writer.appendCsvRow(listOf("Date", "Time", "Type", "Category", "Amount", "Note"))
            transactions.forEach { transaction ->
                val dateTime = transaction.occurredAt.atZone(clock.zone)
                writer.appendCsvRow(
                    listOf(
                        dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        dateTime.format(TIME_FORMAT),
                        transaction.type.label(),
                        transaction.category.name,
                        transaction.amountMinor.toMajorUnits().toPlainString(),
                        transaction.note,
                    ),
                )
            }
        }
        transactions.size
    }

    /** @return number of categories written. */
    suspend fun exportCategories(destination: Uri): Int = withContext(ioDispatcher) {
        val categories = categoryRepository.getAll()
        write(destination) { writer ->
            writer.appendCsvRow(listOf("Name", "Type", "Icon", "Color"))
            categories.forEach { category ->
                writer.appendCsvRow(
                    listOf(
                        category.name,
                        category.type.label(),
                        category.iconKey,
                        "#%08X".format(category.color),
                    ),
                )
            }
        }
        categories.size
    }

    private inline fun write(destination: Uri, block: (Writer) -> Unit) {
        val stream = context.contentResolver.openOutputStream(destination, "wt")
            ?: throw IOException("Cannot open $destination")
        stream.bufferedWriter().use(block)
    }

    private fun TransactionType.label() = when (this) {
        TransactionType.EXPENSE -> "Expense"
        TransactionType.INCOME -> "Income"
    }

    private companion object {
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}

/** RFC 4180 CSV helpers. */
object Csv {
    fun escape(value: String): String =
        if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }

    fun row(values: List<String>): String = values.joinToString(",") { escape(it) }
}

private fun Writer.appendCsvRow(values: List<String>) {
    append(Csv.row(values)).append("\r\n")
}
