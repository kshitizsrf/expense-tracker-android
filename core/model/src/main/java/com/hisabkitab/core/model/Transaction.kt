package com.hisabkitab.core.model

import java.time.Instant

/**
 * A single income or expense entry.
 *
 * @property amountMinor amount in minor units (1/100 of the currency unit, e.g. paise).
 *   Money is stored as an integer to avoid floating-point rounding errors.
 */
data class Transaction(
    val id: Long,
    val amountMinor: Long,
    val category: Category,
    val occurredAt: Instant,
    val note: String,
) {
    val type: TransactionType get() = category.type
}

/** What the editor submits; [id] is null for a new transaction. */
data class TransactionDraft(
    val id: Long?,
    val amountMinor: Long,
    val categoryId: Long,
    val occurredAt: Instant,
    val note: String,
)

/** Filters for the transaction history screen. */
data class TransactionFilter(
    val type: TransactionType? = null,
    val query: String = "",
)

/** Income and expense totals for a period. */
data class PeriodTotals(
    val incomeMinor: Long = 0,
    val expenseMinor: Long = 0,
) {
    val balanceMinor: Long get() = incomeMinor - expenseMinor
}

/** Sum of transactions in one category over a period, used by the statistics screen. */
data class CategoryTotal(
    val category: Category,
    val totalMinor: Long,
    val transactionCount: Int,
)
