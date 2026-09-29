package com.hisabkitab.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.hisabkitab.core.model.Category
import com.hisabkitab.core.model.CategoryTotal
import com.hisabkitab.core.model.Transaction
import com.hisabkitab.core.model.TransactionType
import java.time.Instant

@Entity(
    tableName = "categories",
    indices = [Index(value = ["type"])],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "icon_key") val iconKey: String,
    val color: Int,
    val type: TransactionType,
    @ColumnInfo(name = "default_key") val defaultKey: String? = null,
)

/**
 * Money is stored in minor units and time as epoch millis, so range queries and sums are exact
 * and sort correctly (the old schema stored dates as locale-formatted text).
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["category_id"]), Index(value = ["occurred_at"])],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "amount_minor") val amountMinor: Long,
    @ColumnInfo(name = "category_id") val categoryId: Long,
    @ColumnInfo(name = "occurred_at") val occurredAt: Long,
    val note: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

data class TransactionWithCategory(
    @Embedded val transaction: TransactionEntity,
    @Relation(parentColumn = "category_id", entityColumn = "id")
    val category: CategoryEntity,
)

data class TypeTotalRow(
    val type: TransactionType,
    val total: Long,
)

data class CategoryTotalRow(
    @Embedded val category: CategoryEntity,
    val total: Long,
    @ColumnInfo(name = "transaction_count") val transactionCount: Int,
)

fun CategoryEntity.asModel() = Category(id = id, name = name, iconKey = iconKey, color = color, type = type, defaultKey = defaultKey)

fun Category.asEntity() = CategoryEntity(id = id, name = name, iconKey = iconKey, color = color, type = type, defaultKey = defaultKey)

fun TransactionWithCategory.asModel() = Transaction(
    id = transaction.id,
    amountMinor = transaction.amountMinor,
    category = category.asModel(),
    occurredAt = Instant.ofEpochMilli(transaction.occurredAt),
    note = transaction.note,
)

fun CategoryTotalRow.asModel() = CategoryTotal(
    category = category.asModel(),
    totalMinor = total,
    transactionCount = transactionCount,
)
