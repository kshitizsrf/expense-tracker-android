package com.hisabkitab.core.data.export

import com.hisabkitab.core.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

class CsvImportTest {

    private val zone = ZoneOffset.UTC

    @Test
    fun parseHandlesQuotesCommasAndNewlines() {
        val rows = Csv.parse("a,\"b,c\",\"say \"\"hi\"\"\"\r\n\"multi\nline\",x,y\n")
        assertEquals(listOf(listOf("a", "b,c", "say \"hi\""), listOf("multi\nline", "x", "y")), rows)
    }

    @Test
    fun parseRoundTripsExporterRows() {
        val values = listOf("2026-09-30", "08:15", "Expense", "Food", "120.50", "Tea, snacks")
        assertEquals(listOf(values), Csv.parse(Csv.row(values) + "\r\n"))
    }

    @Test
    fun readsExportedTransactions() {
        val csv = "\uFEFFDate,Time,Type,Category,Amount,Note\r\n" +
            "2026-09-30,08:15,Expense,Food,120.50,\"Tea, snacks\"\r\n" +
            "2026-09-01,9:00,income,Salary,50000,\r\n"
        val parsed = CsvImporter.parseTransactions(csv, zone)

        assertEquals(0, parsed.invalid)
        val (tea, salary) = parsed.rows
        assertEquals(LocalDateTime.of(2026, 9, 30, 8, 15).toInstant(zone), tea.occurredAt)
        assertEquals(TransactionType.EXPENSE, tea.type)
        assertEquals(12_050L, tea.amountMinor)
        assertEquals("Tea, snacks", tea.note)
        assertEquals(TransactionType.INCOME, salary.type)
        assertEquals(5_000_000L, salary.amountMinor)
    }

    @Test
    fun countsUnreadableRowsAndDefaultsMissingTime() {
        val csv = "Date,Type,Category,Amount\n" +
            "2026-09-30,Expense,Food,10\n" +
            "not a date,Expense,Food,10\n" +
            "2026-09-30,Transfer,Food,10\n" +
            "2026-09-30,Expense,Food,-5\n"
        val parsed = CsvImporter.parseTransactions(csv, zone)

        assertEquals(1, parsed.rows.size)
        assertEquals(3, parsed.invalid)
        assertEquals(LocalDateTime.of(2026, 9, 30, 12, 0).toInstant(zone), parsed.rows.single().occurredAt)
    }

    @Test(expected = UnsupportedCsvException::class)
    fun rejectsFilesWithoutTransactionColumns() {
        CsvImporter.parseTransactions("Name,Type,Icon,Color\nFood,Expense,icon_55,#FFE53935\n", zone)
    }
}
