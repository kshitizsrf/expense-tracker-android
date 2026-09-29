package com.hisabkitab.core.database

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hisabkitab.core.model.CategoryColors
import com.hisabkitab.core.model.TransactionType

/** Categories every new install starts with (same set as the original app). */
internal object DefaultCategories {

    private data class Seed(val name: String, val iconKey: String, val type: TransactionType) {
        /** Stable key used to show the name in the user's language. */
        val key: String get() = name.lowercase()
    }

    private val seeds = listOf(
        Seed("Food", "icon_55", TransactionType.EXPENSE),
        Seed("Shopping", "icon_5", TransactionType.EXPENSE),
        Seed("Transportation", "icon_24", TransactionType.EXPENSE),
        Seed("Bills", "icon_119", TransactionType.EXPENSE),
        Seed("Home", "icon_80", TransactionType.EXPENSE),
        Seed("Health", "icon_90", TransactionType.EXPENSE),
        Seed("Entertainment", "icon_67", TransactionType.EXPENSE),
        Seed("Education", "icon_111", TransactionType.EXPENSE),
        Seed("Travel", "icon_2", TransactionType.EXPENSE),
        Seed("Clothing", "icon_114", TransactionType.EXPENSE),
        Seed("Fruits", "icon_138", TransactionType.EXPENSE),
        Seed("Vegetables", "icon_139", TransactionType.EXPENSE),
        Seed("Car", "icon_33", TransactionType.EXPENSE),
        Seed("Insurance", "icon_112", TransactionType.EXPENSE),
        Seed("Gift", "icon_70", TransactionType.EXPENSE),
        Seed("Sport", "icon_11", TransactionType.EXPENSE),
        Seed("Book", "icon_87", TransactionType.EXPENSE),
        Seed("Pet", "icon_94", TransactionType.EXPENSE),
        Seed("Wine", "icon_137", TransactionType.EXPENSE),
        Seed("Salary", "icon_132", TransactionType.INCOME),
        Seed("Rental", "icon_25", TransactionType.INCOME),
        Seed("Sale", "icon_124", TransactionType.INCOME),
        Seed("Awards", "icon_106", TransactionType.INCOME),
        Seed("Investment", "icon_37", TransactionType.INCOME),
        Seed("Other", "icon_97", TransactionType.INCOME),
    )

    /** Inserts the defaults the first time the database file is created. */
    val callback = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            seeds.forEachIndexed { index, seed ->
                db.execSQL(
                    "INSERT INTO categories (name, icon_key, color, type, default_key) VALUES (?, ?, ?, ?, ?)",
                    arrayOf<Any?>(seed.name, seed.iconKey, CategoryColors.forIndex(index), seed.type.name, seed.key),
                )
            }
        }
    }
}
