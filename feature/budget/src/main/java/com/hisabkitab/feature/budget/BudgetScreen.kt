package com.hisabkitab.feature.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hisabkitab.core.designsystem.R as DesignR
import com.hisabkitab.core.designsystem.component.BackdropHeader
import com.hisabkitab.core.designsystem.component.DateRangePickerModal
import com.hisabkitab.core.designsystem.component.GlassCard
import com.hisabkitab.core.designsystem.component.GlassChip
import com.hisabkitab.core.designsystem.component.GlassIconButton
import com.hisabkitab.core.designsystem.component.GradientButton
import com.hisabkitab.core.designsystem.component.LoadingState
import com.hisabkitab.core.designsystem.theme.HisabKitabTheme
import com.hisabkitab.core.model.BudgetPeriod
import com.hisabkitab.core.ui.DateFormats
import com.hisabkitab.core.ui.LocalMoneyFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BudgetScreen(
    onBack: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showRangePicker by rememberSaveable { mutableStateOf(false) }
    val colors = HisabKitabTheme.colors
    val systemBars = WindowInsets.systemBars.asPaddingValues()

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) onBack()
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = systemBars.calculateTopPadding())
            .imePadding(),
    ) {
        BackdropHeader(
            title = stringResource(DesignR.string.budget),
            navigationIcon = { GlassIconButton(Icons.AutoMirrored.Filled.ArrowBack, stringResource(DesignR.string.action_back), onBack) },
        )
        if (uiState.isLoading) {
            LoadingState()
            return@Column
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = systemBars.calculateBottomPadding() + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.budget_intro),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onBackdropMuted,
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            AmountInput(
                text = uiState.amountText,
                onTextChange = viewModel::onAmountChange,
                isError = uiState.error == DesignR.string.error_amount_required,
            )

            Text(stringResource(R.string.budget_period), style = MaterialTheme.typography.titleSmall, color = colors.onBackdrop)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    BudgetPeriod.DAILY to DesignR.string.period_daily,
                    BudgetPeriod.WEEKLY to DesignR.string.period_weekly,
                    BudgetPeriod.MONTHLY to DesignR.string.period_monthly,
                    BudgetPeriod.CUSTOM to DesignR.string.period_custom,
                ).forEach { (period, label) ->
                    GlassChip(
                        label = stringResource(label),
                        selected = uiState.period == period,
                        onClick = {
                            viewModel.onPeriodChange(period)
                            if (period == BudgetPeriod.CUSTOM && uiState.customRange == null) showRangePicker = true
                        },
                    )
                }
            }

            GlassCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(
                                when (uiState.period) {
                                    BudgetPeriod.DAILY -> R.string.budget_explain_daily
                                    BudgetPeriod.WEEKLY -> R.string.budget_explain_weekly
                                    BudgetPeriod.MONTHLY -> R.string.budget_explain_monthly
                                    BudgetPeriod.CUSTOM -> R.string.budget_explain_custom
                                },
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        uiState.previewRange?.let { range ->
                            Text(
                                stringResource(R.string.budget_current_period, DateFormats.range(range)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                if (uiState.period == BudgetPeriod.CUSTOM) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .clickable { showRangePicker = true }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Outlined.DateRange, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            uiState.customRange?.let(DateFormats::range) ?: stringResource(R.string.choose_dates),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        Text(stringResource(R.string.action_change), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            uiState.error?.let { error ->
                Text(
                    stringResource(error),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }

            Spacer(Modifier.height(8.dp))
            GradientButton(
                text = stringResource(R.string.save_budget),
                onClick = viewModel::save,
                icon = Icons.Filled.Check,
                modifier = Modifier.fillMaxWidth(),
            )
            if (uiState.hasExistingBudget) {
                TextButton(onClick = viewModel::remove, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.remove_budget), color = colors.onBackdrop)
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

/** Big centered amount entry, the focal point of the screen. */
@Composable
private fun AmountInput(text: String, onTextChange: (String) -> Unit, isError: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val formatter = LocalMoneyFormatter.current
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isError) Modifier.border(2.dp, scheme.error, MaterialTheme.shapes.large) else Modifier),
    ) {
        Text(stringResource(R.string.budget_amount), style = MaterialTheme.typography.labelLarge, color = scheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(scheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(formatter.symbol, color = scheme.primary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Box(Modifier.weight(1f)) {
                val style = MaterialTheme.typography.displaySmall.copy(fontSize = 40.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Start)
                if (text.isEmpty()) Text("0", style = style, color = scheme.outline)
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    singleLine = true,
                    textStyle = style.copy(color = scheme.onSurface),
                    cursorBrush = SolidColor(scheme.primary),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
