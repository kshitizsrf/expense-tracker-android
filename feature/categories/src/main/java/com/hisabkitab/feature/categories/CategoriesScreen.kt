package com.hisabkitab.feature.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hisabkitab.feature.categories.R
import com.hisabkitab.core.designsystem.R as DesignR
import com.hisabkitab.core.designsystem.component.CategoryIconBadge
import com.hisabkitab.core.designsystem.component.EmptyState
import com.hisabkitab.core.model.Category
import com.hisabkitab.core.model.TransactionType
import kotlinx.coroutines.launch

private val Tabs = listOf(TransactionType.EXPENSE to DesignR.string.expenses, TransactionType.INCOME to DesignR.string.income)

@Composable
fun CategoriesScreen(
    onAddCategory: (TransactionType) -> Unit,
    onOpenCategory: (Category) -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CategoriesScreen(uiState = uiState, onAddCategory = onAddCategory, onOpenCategory = onOpenCategory)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CategoriesScreen(
    uiState: CategoriesUiState,
    onAddCategory: (TransactionType) -> Unit,
    onOpenCategory: (Category) -> Unit,
) {
    val pagerState = rememberPagerState { Tabs.size }
    val scope = rememberCoroutineScope()
    val currentType = Tabs[pagerState.currentPage].first

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(DesignR.string.nav_categories)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddCategory(currentType) }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.new_category))
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding()),
        ) {
            PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                Tabs.forEachIndexed { index, (_, label) ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(stringResource(label)) },
                    )
                }
            }
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                val categories = uiState.categoriesFor(Tabs[page].first)
                if (categories.isEmpty() && !uiState.isLoading) {
                    EmptyState(
                        icon = Icons.Outlined.Category,
                        title = stringResource(R.string.categories_empty_title),
                        message = stringResource(R.string.categories_empty_message),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = padding.calculateBottomPadding() + 88.dp),
                    ) {
                        items(categories, key = { it.id }) { category ->
                            ListItem(
                                modifier = Modifier
                                    .clickable { onOpenCategory(category) }
                                    .animateItem(),
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                leadingContent = { CategoryIconBadge(category.iconKey, category.color) },
                                headlineContent = { Text(category.name) },
                                trailingContent = {
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
