package com.hisabkitab.feature.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hisabkitab.feature.budget.R
import com.hisabkitab.core.designsystem.R as DesignR
import com.hisabkitab.core.designsystem.component.DateRangePickerModal
import com.hisabkitab.core.designsystem.component.LoadingState
import com.hisabkitab.core.model.BudgetPeriod
import com.hisabkitab.core.ui.DateFormats
import com.hisabkitab.core.ui.LocalMoneyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    onBack: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showRangePicker by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(DesignR.string.budget)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(DesignR.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.budget_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = uiState.amountText,
                onValueChange = viewModel::onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.budget_amount)) },
                prefix = { Text(LocalMoneyFormatter.current.symbol + " ") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = uiState.error == DesignR.string.error_amount_required,
            )

            Text(stringResource(R.string.budget_period), style = MaterialTheme.typography.titleSmall)
            val periods = listOf(
                BudgetPeriod.DAILY to DesignR.string.period_daily,
                BudgetPeriod.WEEKLY to DesignR.string.period_weekly,
                BudgetPeriod.MONTHLY to DesignR.string.period_monthly,
                BudgetPeriod.CUSTOM to DesignR.string.period_custom,
            )
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                periods.forEachIndexed { index, (period, label) ->
                    SegmentedButton(
                        selected = uiState.period == period,
                        onClick = {
                            viewModel.onPeriodChange(period)
                            if (period == BudgetPeriod.CUSTOM && uiState.customRange == null) showRangePicker = true
                        },
                        shape = SegmentedButtonDefaults.itemShape(index, periods.size),
                        icon = {},
                    ) { Text(stringResource(label), maxLines = 1) }
                }
            }

            if (uiState.period == BudgetPeriod.CUSTOM) {
                OutlinedCard(onClick = { showRangePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.DateRange, contentDescription = null)
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = uiState.customRange?.let(DateFormats::range) ?: stringResource(R.string.choose_dates),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = { showRangePicker = true }) { Text(stringResource(R.string.action_change)) }
                    }
                }
            }

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(
                            when (uiState.period) {
                                BudgetPeriod.DAILY -> R.string.budget_explain_daily
                                BudgetPeriod.WEEKLY -> R.string.budget_explain_weekly
                                BudgetPeriod.MONTHLY -> R.string.budget_explain_monthly
                                BudgetPeriod.CUSTOM -> R.string.budget_explain_custom
                            },
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    uiState.previewRange?.let { range ->
                        Text(
                            text = stringResource(R.string.budget_current_period, DateFormats.range(range)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            uiState.error?.let { error ->
                Text(stringResource(error), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(8.dp))
            Button(onClick = viewModel::save, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(stringResource(R.string.save_budget))
            }
            if (uiState.hasExistingBudget) {
                OutlinedButton(
                    onClick = viewModel::remove,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(stringResource(R.string.remove_budget))
                }
            }
        }
    }

    if (showRangePicker) {
        DateRangePickerModal(
            initialRange = uiState.customRange,
            onRangeSelected = viewModel::onCustomRangeSelected,
            onDismiss = { showRangePicker = false },
        )
    }
}
