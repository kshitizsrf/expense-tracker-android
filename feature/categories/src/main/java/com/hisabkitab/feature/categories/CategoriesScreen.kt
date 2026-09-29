package com.hisabkitab.feature.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hisabkitab.core.designsystem.R as DesignR
import com.hisabkitab.core.designsystem.component.BackdropHeader
import com.hisabkitab.core.designsystem.component.CategoryIconBadge
import com.hisabkitab.core.designsystem.component.EmptyState
import com.hisabkitab.core.designsystem.component.GlassCard
import com.hisabkitab.core.designsystem.component.GlassIconButton
import com.hisabkitab.core.designsystem.component.SlidingSegmentedControl
import com.hisabkitab.core.designsystem.theme.LocalNavBarClearance
import com.hisabkitab.core.model.Category
import com.hisabkitab.core.model.CategoryTotal
import com.hisabkitab.core.model.TransactionType
import com.hisabkitab.core.ui.LocalMoneyFormatter
import com.hisabkitab.core.ui.displayName
import kotlinx.coroutines.launch

private val Tabs = listOf(TransactionType.EXPENSE, TransactionType.INCOME)

@Composable
fun CategoriesScreen(
    onAddCategory: (TransactionType) -> Unit,
    onOpenCategory: (Category) -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CategoriesScreen(uiState = uiState, onAddCategory = onAddCategory, onOpenCategory = onOpenCategory)
}

@Composable
internal fun CategoriesScreen(
    uiState: CategoriesUiState,
    onAddCategory: (TransactionType) -> Unit,
    onOpenCategory: (Category) -> Unit,
) {
    val pagerState = rememberPagerState { Tabs.size }
    val scope = rememberCoroutineScope()
    val currentType = Tabs[pagerState.currentPage]
    val systemBars = WindowInsets.systemBars.asPaddingValues()

    Column(Modifier.fillMaxSize().padding(top = systemBars.calculateTopPadding())) {
        BackdropHeader(
            title = stringResource(R.string.categories_title),
            subtitle = stringResource(R.string.categories_subtitle),
            actions = {
                GlassIconButton(Icons.Filled.Add, stringResource(R.string.new_category), { onAddCategory(currentType) })
            },
        )
        SlidingSegmentedControl(
            options = listOf(
                TransactionType.EXPENSE to stringResource(DesignR.string.expenses),
                TransactionType.INCOME to stringResource(DesignR.string.income),
            ),
            selected = currentType,
            onSelect = { type -> scope.launch { pagerState.animateScrollToPage(Tabs.indexOf(type)) } },
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(12.dp))
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val categories = uiState.categoriesFor(Tabs[page])
            if (categories.isEmpty() && !uiState.isLoading) {
                GlassCard(Modifier.padding(16.dp).fillMaxWidth()) {
                    EmptyState(
                        icon = Icons.Outlined.Category,
                        title = stringResource(R.string.categories_empty_title),
                        message = stringResource(R.string.categories_empty_message),
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 4.dp,
                        bottom = systemBars.calculateBottomPadding() + LocalNavBarClearance.current + 8.dp,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(categories, key = { it.id }) { category ->
                        CategoryTile(
                            category = category,
                            total = uiState.monthTotals[category.id],
                            onClick = { onOpenCategory(category) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryTile(category: Category, total: CategoryTotal?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val formatter = LocalMoneyFormatter.current
    GlassCard(modifier = modifier.fillMaxWidth().height(138.dp), onClick = onClick, contentPadding = PaddingValues(16.dp)) {
        CategoryIconBadge(category.iconKey, category.color, size = 48.dp, filled = true)
        Spacer(Modifier.weight(1f))
        Text(
            category.displayName(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = total?.let { stringResource(R.string.this_month_total, formatter.format(it.totalMinor)) }
                ?: stringResource(R.string.nothing_this_month),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
