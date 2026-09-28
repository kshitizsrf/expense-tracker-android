package com.hisabkitab.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hisabkitab.core.database.dao.CategoryDao
import com.hisabkitab.core.database.dao.TransactionDao
import com.hisabkitab.core.database.entity.CategoryEntity
import com.hisabkitab.core.database.entity.TransactionEntity

@Database(
    entities = [CategoryEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class HisabKitabDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val NAME = "hisab_kitab.db"
    }
}
