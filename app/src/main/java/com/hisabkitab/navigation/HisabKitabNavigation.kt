package com.hisabkitab.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.hisabkitab.BuildConfig
import com.hisabkitab.feature.budget.BudgetScreen
import com.hisabkitab.feature.categories.CategoriesScreen
import com.hisabkitab.feature.categories.editor.CategoryEditorScreen
import com.hisabkitab.feature.categories.editor.CategoryEditorViewModel
import com.hisabkitab.feature.home.HomeScreen
import com.hisabkitab.feature.settings.SettingsScreen
import com.hisabkitab.feature.stats.StatsScreen
import com.hisabkitab.feature.transactions.TransactionsScreen
import com.hisabkitab.feature.transactions.detail.TransactionDetailScreen
import com.hisabkitab.feature.transactions.detail.TransactionDetailViewModel
import com.hisabkitab.feature.transactions.editor.TransactionEditorScreen
import com.hisabkitab.feature.transactions.editor.TransactionEditorViewModel

@Composable
fun HisabKitabNavDisplay(
    backStack: MutableList<NavKey>,
    modifier: Modifier = Modifier,
) {
    val navigate: (NavKey) -> Unit = { backStack.add(it) }
    val goBack: () -> Unit = { backStack.removeLastOrNull() }
    val openTopLevel: (TopLevelDestination) -> Unit = { backStack.navigateToTopLevel(it) }

    NavDisplay(
        backStack = backStack,
        onBack = { goBack() },
        modifier = modifier,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        transitionSpec = {
            (slideInHorizontally(tween(DURATION)) { it / 5 } + fadeIn(tween(DURATION)))
                .togetherWith(scaleOut(tween(DURATION), targetScale = 0.94f) + fadeOut(tween(DURATION)))
        },
        popTransitionSpec = {
            (scaleIn(tween(DURATION), initialScale = 0.94f) + fadeIn(tween(DURATION)))
                .togetherWith(slideOutHorizontally(tween(DURATION)) { it / 5 } + fadeOut(tween(DURATION)))
        },
        predictivePopTransitionSpec = {
            (scaleIn(tween(DURATION), initialScale = 0.94f) + fadeIn(tween(DURATION)))
                .togetherWith(slideOutHorizontally(tween(DURATION)) { it / 5 } + fadeOut(tween(DURATION)))
        },
        entryProvider = entryProvider {
            entry<HomeKey> {
                HomeScreen(
                    onAddTransaction = { navigate(TransactionEditorKey()) },
                    onOpenTransaction = { navigate(TransactionDetailKey(it)) },
                    onSeeAllTransactions = { openTopLevel(TopLevelDestination.TRANSACTIONS) },
                    onOpenBudget = { navigate(BudgetKey) },
                    onOpenSettings = { navigate(SettingsKey) },
                )
            }
            entry<TransactionsKey> {
                TransactionsScreen(
                    onAddTransaction = { navigate(TransactionEditorKey()) },
                    onOpenTransaction = { navigate(TransactionDetailKey(it)) },
                )
            }
            entry<StatsKey> {
                StatsScreen()
            }
            entry<CategoriesKey> {
                CategoriesScreen(
                    onAddCategory = { type -> navigate(CategoryEditorKey(initialType = type)) },
                    onOpenCategory = { category -> navigate(CategoryEditorKey(category.id, category.type)) },
                )
            }
            entry<SettingsKey> {
                SettingsScreen(
                    versionName = BuildConfig.VERSION_NAME,
                    onBack = goBack,
                    onOpenBudget = { navigate(BudgetKey) },
                )
            }
            entry<BudgetKey> {
                BudgetScreen(onBack = goBack)
            }
            entry<TransactionDetailKey> { key ->
                TransactionDetailScreen(
                    viewModel = hiltViewModel<TransactionDetailViewModel, TransactionDetailViewModel.Factory>(
                        creationCallback = { factory -> factory.create(key.transactionId) },
                    ),
                    onBack = goBack,
                    onEdit = { navigate(TransactionEditorKey(transactionId = key.transactionId)) },
                )
            }
            entry<TransactionEditorKey> { key ->
                TransactionEditorScreen(
                    viewModel = hiltViewModel<TransactionEditorViewModel, TransactionEditorViewModel.Factory>(
                        creationCallback = { factory -> factory.create(key.transactionId, key.initialType) },
                    ),
                    onBack = goBack,
                    onAddCategory = { type -> navigate(CategoryEditorKey(initialType = type)) },
                )
            }
            entry<CategoryEditorKey> { key ->
                CategoryEditorScreen(
                    viewModel = hiltViewModel<CategoryEditorViewModel, CategoryEditorViewModel.Factory>(
                        creationCallback = { factory -> factory.create(key.categoryId, key.initialType) },
                    ),
                    onBack = goBack,
                )
            }
        },
    )
}

/**
 * Switches tabs. Home is always the root, so Back from any other tab returns to Home
 * before leaving the app (Material navigation guidance).
 */
fun MutableList<NavKey>.navigateToTopLevel(destination: TopLevelDestination) {
    if (lastOrNull() == destination.key) return
    while (size > 1) removeAt(lastIndex)
    if (destination.key != HomeKey) add(destination.key)
}

private const val DURATION = 320
