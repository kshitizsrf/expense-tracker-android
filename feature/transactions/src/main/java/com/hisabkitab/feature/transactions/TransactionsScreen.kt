package com.hisabkitab.feature.transactions

import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.platform.LocalResources
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hisabkitab.core.designsystem.R as DesignR
import com.hisabkitab.core.designsystem.component.BackdropHeader
import com.hisabkitab.core.designsystem.component.EmptyState
import com.hisabkitab.core.designsystem.component.GlassCard
import com.hisabkitab.core.designsystem.component.GlassChip
import com.hisabkitab.core.designsystem.component.SpotlightItem
import com.hisabkitab.core.designsystem.component.spotlightOnClick
import com.hisabkitab.core.designsystem.component.glass
import com.hisabkitab.core.designsystem.theme.HisabKitabTheme
import com.hisabkitab.core.designsystem.theme.LocalNavBarClearance
import com.hisabkitab.core.model.Transaction
import com.hisabkitab.core.model.TransactionType
import com.hisabkitab.core.ui.LocalMoneyFormatter
import com.hisabkitab.core.ui.TransactionListItem
import com.hisabkitab.core.ui.relativeDayLabel
import java.time.LocalDate

@Composable
fun TransactionsScreen(
    onAddTransaction: () -> Unit,
    onOpenTransaction: (Long) -> Unit,
    viewModel: TransactionsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TransactionsScreen(
        uiState = uiState,
        onTypeFilterChange = viewModel::onTypeFilterChange,
        onQueryChange = viewModel::onQueryChange,
        onOpenTransaction = onOpenTransaction,
        onDelete = viewModel::delete,
        onUndo = viewModel::undoDelete,
        onUndoExpired = viewModel::onUndoExpired,
    )
}

@Composable
internal fun TransactionsScreen(
    uiState: TransactionsUiState,
    onTypeFilterChange: (TransactionType?) -> Unit,
    onQueryChange: (String) -> Unit,
    onOpenTransaction: (Long) -> Unit,
    onDelete: (Transaction) -> Unit,
    onUndo: () -> Unit,
    onUndoExpired: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val systemBars = WindowInsets.systemBars.asPaddingValues()
    val clearance = LocalNavBarClearance.current

    LaunchedEffect(uiState.recentlyDeleted) {
        if (uiState.recentlyDeleted == null) return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = resources.getString(R.string.deleted_transaction),
            actionLabel = resources.getString(R.string.undo),
            duration = SnackbarDuration.Short,
        )
        if (result == SnackbarResult.ActionPerformed) onUndo() else onUndoExpired()
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = systemBars.calculateTopPadding(),
                bottom = systemBars.calculateBottomPadding() + clearance + 8.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "header") {
                BackdropHeader(title = stringResource(R.string.activity_title), subtitle = stringResource(R.string.activity_subtitle))
            }
            item(key = "search") {
                SearchBar(query = uiState.query, onQueryChange = onQueryChange, modifier = Modifier.padding(horizontal = 16.dp))
            }
            item(key = "filters") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GlassChip(stringResource(R.string.filter_all), uiState.typeFilter == null, { onTypeFilterChange(null) })
                    GlassChip(stringResource(DesignR.string.expenses), uiState.typeFilter == TransactionType.EXPENSE, {
                        onTypeFilterChange(TransactionType.EXPENSE)
                    })
                    GlassChip(stringResource(DesignR.string.income), uiState.typeFilter == TransactionType.INCOME, {
                        onTypeFilterChange(TransactionType.INCOME)
                    })
                }
            }
            if (uiState.groups.isNotEmpty()) {
                item(key = "totals") { TotalsStrip(uiState) }
            }

            when {
                uiState.isLoading -> Unit
                uiState.groups.isEmpty() -> item(key = "empty") {
                    GlassCard(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                        EmptyState(
                            icon = if (uiState.isFiltering) Icons.Outlined.SearchOff else Icons.AutoMirrored.Outlined.ReceiptLong,
                            title = stringResource(if (uiState.isFiltering) R.string.empty_filtered_title else DesignR.string.empty_transactions_title),
                            message = stringResource(if (uiState.isFiltering) R.string.empty_filtered_message else DesignR.string.empty_transactions_message),
                        )
                    }
                }
                else -> items(uiState.groups, key = { "day-${it.date}" }) { group ->
                    DayCard(
                        group = group,
                        today = uiState.today,
                        onOpenTransaction = onOpenTransaction,
                        onDelete = onDelete,
                        modifier = Modifier.padding(horizontal = 16.dp).animateItem(),
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = systemBars.calculateBottomPadding() + clearance),
        )
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val focusManager = LocalFocusManager.current
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .glass(CircleShape)
            .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = scheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    stringResource(R.string.search_hint),
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = scheme.onSurface),
                cursorBrush = SolidColor(scheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.clear_search))
            }
        }
    }
}

@Composable
private fun TotalsStrip(uiState: TransactionsUiState) {
    val formatter = LocalMoneyFormatter.current
    val colors = HisabKitabTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        if (uiState.typeFilter != TransactionType.INCOME) {
            TotalFigure(stringResource(R.string.spent_label), formatter.format(uiState.totalSpentMinor), colors.expense, Modifier.weight(1f))
        }
        if (uiState.typeFilter != TransactionType.EXPENSE) {
            TotalFigure(stringResource(R.string.earned_label), formatter.format(uiState.totalEarnedMinor), colors.income, Modifier.weight(1f))
        }
    }
}

@Composable
private fun TotalFigure(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    val colors = HisabKitabTheme.colors
    Column(
        modifier
            .clip(MaterialTheme.shapes.small)
            .spotlightOnClick { SpotlightItem(label, value, valueColor = accent) }
            .padding(vertical = 4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = colors.onBackdropMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        BasicText(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(color = colors.onBackdrop, fontWeight = FontWeight.Bold),
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 14.sp, maxFontSize = MaterialTheme.typography.titleLarge.fontSize),
        )
    }
}

@Composable
private fun DayCard(
    group: TransactionDayGroup,
    today: LocalDate,
    onOpenTransaction: (Long) -> Unit,
    onDelete: (Transaction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val formatter = LocalMoneyFormatter.current
    val colors = HisabKitabTheme.colors
    GlassCard(modifier = modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                relativeDayLabel(group.date, today),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(end = 8.dp),
            )
            Text(
                formatter.formatNet(group.netMinor),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                color = if (group.netMinor >= 0) colors.income else colors.expense,
                modifier = Modifier
                    .clip(CircleShape)
                    .background((if (group.netMinor >= 0) colors.income else colors.expense).copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
        group.transactions.forEach { transaction ->
            SwipeToDelete(onDelete = { onDelete(transaction) }) {
                TransactionListItem(transaction = transaction, onClick = { onOpenTransaction(transaction.id) })
            }
        }
    }
}

@Composable
private fun SwipeToDelete(onDelete: () -> Unit, content: @Composable () -> Unit) {
    val state = rememberSwipeToDismissBoxState()
    LaunchedEffect(state.currentValue) {
        if (state.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
            state.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }
    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            if (state.dismissDirection != SwipeToDismissBoxValue.EndToStart) return@SwipeToDismissBox
            val active = state.targetValue == SwipeToDismissBoxValue.EndToStart
            val background by animateColorAsState(
                if (active) HisabKitabTheme.colors.expense else HisabKitabTheme.colors.expense.copy(alpha = 0.45f),
                label = "swipeBg",
            )
            Box(
                Modifier.fillMaxSize().background(background).padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = stringResource(DesignR.string.action_delete), tint = Color.White)
            }
        },
    ) {
        // Opaque only while swiping, so the red backdrop never shows through the row.
        val swiping = state.dismissDirection == SwipeToDismissBoxValue.EndToStart
        Box(Modifier.background(if (swiping) MaterialTheme.colorScheme.surfaceContainerLow else Color.Transparent)) { content() }
    }
}

