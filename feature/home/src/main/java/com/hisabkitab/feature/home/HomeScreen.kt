package com.hisabkitab.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hisabkitab.feature.home.R
import com.hisabkitab.core.designsystem.R as DesignR
import com.hisabkitab.core.designsystem.component.EmptyState
import com.hisabkitab.core.designsystem.component.SectionHeader
import com.hisabkitab.core.designsystem.theme.HisabKitabTheme
import com.hisabkitab.core.model.BudgetPeriod
import com.hisabkitab.core.model.BudgetStatus
import com.hisabkitab.core.model.PeriodTotals
import com.hisabkitab.core.ui.DateFormats
import com.hisabkitab.core.ui.LocalMoneyFormatter
import com.hisabkitab.core.ui.TransactionListItem
import java.time.YearMonth

@Composable
fun HomeScreen(
    onAddTransaction: () -> Unit,
    onOpenTransaction: (Long) -> Unit,
    onSeeAllTransactions: () -> Unit,
    onOpenBudget: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onPreviousMonth = viewModel::showPreviousMonth,
        onNextMonth = viewModel::showNextMonth,
        onCurrentMonth = viewModel::showCurrentMonth,
        onAddTransaction = onAddTransaction,
        onOpenTransaction = onOpenTransaction,
        onSeeAllTransactions = onSeeAllTransactions,
        onOpenBudget = onOpenBudget,
        onOpenSettings = onOpenSettings,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onCurrentMonth: () -> Unit,
    onAddTransaction: () -> Unit,
    onOpenTransaction: (Long) -> Unit,
    onSeeAllTransactions: () -> Unit,
    onOpenBudget: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()
    val fabExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(DesignR.string.app_name)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = stringResource(DesignR.string.settings_title))
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTransaction,
                expanded = fabExpanded,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(DesignR.string.add_transaction)) },
            )
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "month") {
                MonthSelector(
                    month = uiState.month,
                    isCurrentMonth = uiState.isCurrentMonth,
                    onPrevious = onPreviousMonth,
                    onNext = onNextMonth,
                    onCurrent = onCurrentMonth,
                )
            }
            item(key = "balance") {
                BalanceCard(totals = uiState.totals)
            }
            item(key = "budget") {
                BudgetCard(status = uiState.budgetStatus, onClick = onOpenBudget)
            }
            item(key = "recent-header") {
                SectionHeader(
                    title = stringResource(R.string.recent_transactions),
                    action = if (uiState.recentTransactions.isNotEmpty()) {
                        { TextButton(onClick = onSeeAllTransactions) { Text(stringResource(R.string.see_all)) } }
                    } else {
                        null
                    },
                )
            }
            if (uiState.recentTransactions.isEmpty() && !uiState.isLoading) {
                item(key = "empty") {
                    EmptyState(
                        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                        title = stringResource(DesignR.string.empty_transactions_title),
                        message = stringResource(DesignR.string.empty_transactions_message),
                    )
                }
            } else {
                item(key = "recent") {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                        Column(Modifier.padding(vertical = 4.dp)) {
                            uiState.recentTransactions.forEach { transaction ->
                                TransactionListItem(
                                    transaction = transaction,
                                    onClick = { onOpenTransaction(transaction.id) },
                                    showDate = true,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthSelector(
    month: YearMonth,
    isCurrentMonth: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onCurrent: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.previous_month))
        }
        AnimatedContent(
            targetState = month,
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (slideInHorizontally { it / 3 * direction } + fadeIn())
                    .togetherWith(slideOutHorizontally { -it / 3 * direction } + fadeOut())
                    .using(SizeTransform(clip = false))
            },
            modifier = Modifier.weight(1f),
            label = "month",
        ) { target ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .clickable(enabled = !isCurrentMonth, onClick = onCurrent)
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(DateFormats.monthYear(target), style = MaterialTheme.typography.titleMedium)
                if (!isCurrentMonth) {
                    Text(
                        text = stringResource(R.string.back_to_this_month),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.next_month))
        }
    }
}

@Composable
internal fun BalanceCard(totals: PeriodTotals, modifier: Modifier = Modifier) {
    val formatter = LocalMoneyFormatter.current
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colors.primaryContainer, contentColor = colors.onPrimaryContainer),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(stringResource(R.string.balance), style = MaterialTheme.typography.labelLarge)
            BasicText(
                text = formatter.format(totals.balanceMinor),
                style = MaterialTheme.typography.displaySmall.copy(color = colors.onPrimaryContainer),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 20.sp, maxFontSize = 36.sp),
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TotalPill(
                    label = stringResource(DesignR.string.income),
                    amount = formatter.format(totals.incomeMinor),
                    icon = Icons.Filled.ArrowDownward,
                    iconTint = HisabKitabTheme.financeColors.income,
                    modifier = Modifier.weight(1f),
                )
                TotalPill(
                    label = stringResource(DesignR.string.expenses),
                    amount = formatter.format(totals.expenseMinor),
                    icon = Icons.Filled.ArrowUpward,
                    iconTint = HisabKitabTheme.financeColors.expense,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TotalPill(
    label: String,
    amount: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = iconTint.copy(alpha = 0.16f), modifier = Modifier.size(32.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                BasicText(
                    text = amount,
                    style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(minFontSize = 11.sp, maxFontSize = 16.sp),
                )
            }
        }
    }
}

@Composable
private fun BudgetCard(status: BudgetStatus?, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        if (status == null) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Savings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.budget_set_title), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.budget_set_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        } else {
            BudgetProgress(status)
        }
    }
}

@Composable
private fun BudgetProgress(status: BudgetStatus) {
    val formatter = LocalMoneyFormatter.current
    val finance = HisabKitabTheme.financeColors
    val progressColor = when {
        status.isOverBudget -> finance.expense
        status.progress >= 0.8f -> finance.warning
        else -> MaterialTheme.colorScheme.primary
    }
    val title = when (status.budget.period) {
        BudgetPeriod.DAILY -> R.string.budget_title_daily
        BudgetPeriod.WEEKLY -> R.string.budget_title_weekly
        BudgetPeriod.MONTHLY -> R.string.budget_title_monthly
        BudgetPeriod.CUSTOM -> R.string.budget_title_custom
    }

    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text(
                DateFormats.range(status.range),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LinearProgressIndicator(
            progress = { status.progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape),
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {},
        )
        Text(
            text = stringResource(
                R.string.budget_spent_of,
                formatter.format(status.spentMinor),
                formatter.format(status.budget.amountMinor),
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        val message = when {
            status.hasEnded -> stringResource(R.string.budget_ended)
            !status.isActive -> stringResource(R.string.budget_not_started, DateFormats.medium(status.range.start))
            status.isOverBudget -> stringResource(R.string.budget_over_by, formatter.format(-status.remainingMinor))
            status.daysLeft > 1 -> pluralStringResource(
                R.plurals.budget_left_per_day,
                status.daysLeft.toInt(),
                formatter.format(status.remainingMinor),
                formatter.format(status.dailyAllowanceMinor),
                status.daysLeft.toInt(),
            )
            else -> stringResource(R.string.budget_left_today, formatter.format(status.remainingMinor))
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = if (status.isOverBudget) finance.expense else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview
@Composable
private fun BalanceCardPreview() {
    HisabKitabTheme {
        BalanceCard(totals = PeriodTotals(incomeMinor = 8_500_000, expenseMinor = 3_245_050))
    }
}
