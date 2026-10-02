package com.hisabkitab.feature.categories

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
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
    CategoriesScreen(
        uiState = uiState,
        onAddCategory = onAddCategory,
        onOpenCategory = onOpenCategory,
        onRequestDelete = viewModel::requestDelete,
        onConfirmDelete = viewModel::confirmDelete,
        onCancelDelete = viewModel::cancelDelete,
    )
}

/**
 * Category tiles per type. Long-press a tile (or use the checklist button) to start selecting;
 * selected categories can then be deleted together without opening each one.
 */
@Composable
internal fun CategoriesScreen(
    uiState: CategoriesUiState,
    onAddCategory: (TransactionType) -> Unit,
    onOpenCategory: (Category) -> Unit,
    onRequestDelete: (Set<Long>) -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    onCancelDelete: () -> Unit = {},
) {
    val pagerState = rememberPagerState { Tabs.size }
    val scope = rememberCoroutineScope()
    val currentType = Tabs[pagerState.currentPage]
    val systemBars = WindowInsets.systemBars.asPaddingValues()
    val haptics = LocalHapticFeedback.current

    var selecting by rememberSaveable { mutableStateOf(false) }
    // A LongArray is saveable as-is, so the selection survives rotation.
    var selectedIds by rememberSaveable { mutableStateOf(longArrayOf()) }
    val selected = selectedIds.toSet()
    fun clearSelection() {
        selecting = false
        selectedIds = longArrayOf()
    }
    fun toggle(id: Long) {
        val next = if (id in selected) selected - id else selected + id
        selectedIds = next.toLongArray()
        if (next.isEmpty()) selecting = false
    }

    // Drop ids that no longer exist (e.g. after the deletion went through).
    LaunchedEffect(uiState.expense, uiState.income) {
        if (uiState.isLoading) return@LaunchedEffect
        val existing = (uiState.expense + uiState.income).mapTo(HashSet()) { it.id }
        if (selectedIds.any { it !in existing }) {
            selectedIds = selectedIds.filter { it in existing }.toLongArray()
            if (selectedIds.isEmpty()) selecting = false
        }
    }

    BackHandler(enabled = selecting) { clearSelection() }

    Column(Modifier.fillMaxSize().padding(top = systemBars.calculateTopPadding())) {
        AnimatedContent(
            targetState = selecting,
            transitionSpec = { fadeIn().togetherWith(fadeOut()) },
            label = "header",
        ) { isSelecting ->
            if (isSelecting) {
                BackdropHeader(
                    title = pluralStringResource(R.plurals.categories_selected, selected.size, selected.size),
                    navigationIcon = { GlassIconButton(Icons.Filled.Close, stringResource(R.string.clear_selection), ::clearSelection) },
                    actions = {
                        GlassIconButton(Icons.Outlined.SelectAll, stringResource(R.string.select_all), {
                            selectedIds = (selected + uiState.categoriesFor(currentType).map { it.id }).toLongArray()
                        })
                        GlassIconButton(
                            Icons.Outlined.Delete,
                            stringResource(R.string.delete_selected),
                            { onRequestDelete(selected) },
                            enabled = selected.isNotEmpty(),
                        )
                    },
                )
            } else {
                BackdropHeader(
                    title = stringResource(R.string.categories_title),
                    subtitle = stringResource(R.string.categories_subtitle),
                    actions = {
                        GlassIconButton(Icons.Outlined.Checklist, stringResource(R.string.select_categories), { selecting = true })
                        GlassIconButton(Icons.Filled.Add, stringResource(R.string.new_category), { onAddCategory(currentType) })
                    },
                )
            }
        }
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
                            selecting = selecting,
                            selected = category.id in selected,
                            onClick = { if (selecting) toggle(category.id) else onOpenCategory(category) },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                selecting = true
                                toggle(category.id)
                            },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }

    uiState.pendingDelete?.let { pending ->
        AlertDialog(
            onDismissRequest = onCancelDelete,
            title = { Text(pluralStringResource(R.plurals.delete_categories_title, pending.ids.size, pending.ids.size)) },
            text = {
                Text(
                    if (pending.transactionCount > 0) {
                        pluralStringResource(R.plurals.delete_categories_message, pending.transactionCount, pending.transactionCount)
                    } else {
                        stringResource(R.string.delete_categories_message_empty)
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text(stringResource(DesignR.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = onCancelDelete) { Text(stringResource(DesignR.string.action_cancel)) } },
        )
    }
}

@Composable
private fun CategoryTile(
    category: Category,
    total: CategoryTotal?,
    selecting: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val formatter = LocalMoneyFormatter.current
    val scheme = MaterialTheme.colorScheme
    val scale by animateFloatAsState(if (selected) 0.95f else 1f, label = "tileScale")
    Box(modifier.scale(scale)) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(138.dp)
                .then(if (selected) Modifier.border(2.dp, scheme.primary, MaterialTheme.shapes.large) else Modifier),
            onClick = onClick,
            onLongClick = onLongClick,
            contentPadding = PaddingValues(16.dp),
        ) {
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
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        AnimatedVisibility(
            visible = selecting,
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
        ) {
            SelectionMark(selected)
        }
    }
}

@Composable
private fun SelectionMark(selected: Boolean) {
    val scheme = MaterialTheme.colorScheme
    if (selected) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = scheme.primary,
            modifier = Modifier.size(26.dp).background(Color.White, CircleShape),
        )
    } else {
        Box(
            Modifier
                .size(26.dp)
                .border(2.dp, scheme.outline, CircleShape),
        )
    }
}
