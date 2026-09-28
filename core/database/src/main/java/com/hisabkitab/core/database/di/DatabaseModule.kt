package com.hisabkitab.core.database.di

import android.content.Context
import androidx.room.Room
import com.hisabkitab.core.database.DefaultCategories
import com.hisabkitab.core.database.HisabKitabDatabase
import com.hisabkitab.core.database.dao.CategoryDao
import com.hisabkitab.core.database.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HisabKitabDatabase =
        Room.databaseBuilder(context, HisabKitabDatabase::class.java, HisabKitabDatabase.NAME)
            .addCallback(DefaultCategories.callback)
            .build()

    @Provides
    fun provideCategoryDao(database: HisabKitabDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideTransactionDao(database: HisabKitabDatabase): TransactionDao = database.transactionDao()
}
