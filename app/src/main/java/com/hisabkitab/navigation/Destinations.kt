package com.hisabkitab.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.hisabkitab.R
import com.hisabkitab.core.designsystem.R as DesignR
import com.hisabkitab.core.model.TransactionType
import kotlinx.serialization.Serializable

// Navigation 3 keys. Each is @Serializable so the back stack survives process death.

@Serializable
data object HomeKey : NavKey

@Serializable
data object TransactionsKey : NavKey

@Serializable
data object StatsKey : NavKey

@Serializable
data object CategoriesKey : NavKey

@Serializable
data object SettingsKey : NavKey

@Serializable
data object BudgetKey : NavKey

@Serializable
data class TransactionDetailKey(val transactionId: Long) : NavKey

/** Add a transaction when [transactionId] is null, otherwise edit it. */
@Serializable
data class TransactionEditorKey(
    val transactionId: Long? = null,
    val initialType: TransactionType = TransactionType.EXPENSE,
) : NavKey

/** Create a category when [categoryId] is null, otherwise edit it. */
@Serializable
data class CategoryEditorKey(
    val categoryId: Long? = null,
    val initialType: TransactionType = TransactionType.EXPENSE,
) : NavKey

/** Tabs in the bottom navigation bar. */
enum class TopLevelDestination(
    val key: NavKey,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val label: Int,
) {
    HOME(HomeKey, Icons.Filled.Home, Icons.Outlined.Home, R.string.nav_home),
    TRANSACTIONS(
        TransactionsKey,
        Icons.AutoMirrored.Filled.ReceiptLong,
        Icons.AutoMirrored.Outlined.ReceiptLong,
        DesignR.string.nav_transactions,
    ),
    STATS(StatsKey, Icons.Filled.PieChart, Icons.Outlined.PieChart, DesignR.string.nav_stats),
    CATEGORIES(CategoriesKey, Icons.Filled.Category, Icons.Outlined.Category, DesignR.string.nav_categories),
}
