package com.hisabkitab.feature.stats

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hisabkitab.feature.stats.R
import com.hisabkitab.core.designsystem.R as DesignR
import com.hisabkitab.core.designsystem.component.CategoryIconBadge
import com.hisabkitab.core.designsystem.component.DateRangePickerModal
import com.hisabkitab.core.designsystem.component.EmptyState
import com.hisabkitab.core.model.CategoryTotal
import com.hisabkitab.core.model.DateRange
import com.hisabkitab.core.model.TransactionType
import com.hisabkitab.core.ui.DateFormats
import com.hisabkitab.core.ui.LocalMoneyFormatter
import kotlin.math.roundToInt

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    StatsScreen(
        uiState = uiState,
        onTypeChange = viewModel::onTypeChange,
        onPeriodChange = viewModel::onPeriodChange,
        onCustomRangeSelected = viewModel::onCustomRangeSelected,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StatsScreen(
    uiState: StatsUiState,
    onTypeChange: (TransactionType) -> Unit,
    onPeriodChange: (StatsPeriod) -> Unit,
    onCustomRangeSelected: (DateRange) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showRangePicker by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(title = { Text(stringResource(DesignR.string.nav_stats)) }, scrollBehavior = scrollBehavior)
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
        ) {
            item(key = "type") {
                TypeToggle(
                    selected = uiState.type,
                    onSelect = onTypeChange,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
            item(key = "periods") {
                PeriodChips(
                    selected = uiState.period,
                    customRange = uiState.customRange,
                    onSelect = onPeriodChange,
                    onCustom = { showRangePicker = true },
                )
            }
            item(key = "chart") {
                ChartCard(uiState)
            }
            if (uiState.totals.isNotEmpty()) {
                items(uiState.totals, key = { it.category.id }) { total ->
                    CategoryBreakdownRow(total = total, grandTotalMinor = uiState.grandTotalMinor)
                }
            }
        }
    }

    if (showRangePicker) {
        DateRangePickerModal(
            initialRange = uiState.customRange ?: uiState.range,
            onRangeSelected = onCustomRangeSelected,
            onDismiss = { showRangePicker = false },
        )
    }
}

@Composable
private fun TypeToggle(selected: TransactionType, onSelect: (TransactionType) -> Unit, modifier: Modifier = Modifier) {
    val options = listOf(TransactionType.EXPENSE to DesignR.string.expenses, TransactionType.INCOME to DesignR.string.income)
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (type, label) ->
            SegmentedButton(
                selected = type == selected,
                onClick = { onSelect(type) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
            ) { Text(stringResource(label)) }
        }
    }
}

@Composable
private fun PeriodChips(
    selected: StatsPeriod,
    customRange: DateRange?,
    onSelect: (StatsPeriod) -> Unit,
    onCustom: () -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(StatsPeriod.entries) { period ->
            val label = when (period) {
                StatsPeriod.THIS_MONTH -> stringResource(R.string.period_this_month)
                StatsPeriod.LAST_MONTH -> stringResource(R.string.period_last_month)
                StatsPeriod.LAST_3_MONTHS -> stringResource(R.string.period_last_3_months)
                StatsPeriod.THIS_YEAR -> stringResource(R.string.period_this_year)
                StatsPeriod.CUSTOM -> customRange?.let(DateFormats::range) ?: stringResource(DesignR.string.period_custom)
            }
            FilterChip(
                selected = period == selected,
                onClick = { if (period == StatsPeriod.CUSTOM) onCustom() else onSelect(period) },
                label = { Text(label) },
                leadingIcon = if (period == StatsPeriod.CUSTOM) {
                    { Icon(Icons.Outlined.DateRange, contentDescription = null, Modifier.size(18.dp)) }
                } else {
                    null
                },
            )
        }
    }
}

@Composable
private fun ChartCard(uiState: StatsUiState) {
    val formatter = LocalMoneyFormatter.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = DateFormats.range(uiState.range),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            if (uiState.totals.isEmpty() && !uiState.isLoading) {
                EmptyState(
                    icon = Icons.Outlined.PieChart,
                    title = stringResource(R.string.stats_empty_title),
                    message = stringResource(R.string.stats_empty_message),
                )
            } else {
                val totalLabel = stringResource(
                    if (uiState.type == TransactionType.EXPENSE) R.string.total_spent else R.string.total_earned,
                )
                val totalText = formatter.format(uiState.grandTotalMinor)
                val slices = remember(uiState.totals) {
                    uiState.totals.map { DonutSlice(it.totalMinor.toFloat(), Color(it.category.color)) }
                }
                DonutChart(
                    slices = slices,
                    contentDescription = "$totalLabel $totalText",
                    modifier = Modifier.size(220.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(totalLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = totalText,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 40.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBreakdownRow(total: CategoryTotal, grandTotalMinor: Long) {
    val formatter = LocalMoneyFormatter.current
    val share = if (grandTotalMinor > 0) total.totalMinor.toFloat() / grandTotalMinor else 0f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryIconBadge(iconKey = total.category.iconKey, color = total.category.color)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = total.category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(formatter.format(total.totalMinor), style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { share },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = Color(total.category.color),
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {},
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.percent_of_total, (share * 100).roundToInt()) + " · " +
                    pluralStringResource(R.plurals.transaction_count, total.transactionCount, total.transactionCount),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
