package com.hisabkitab.feature.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hisabkitab.feature.settings.R
import com.hisabkitab.core.designsystem.R as DesignR
import com.hisabkitab.core.common.money.MoneyFormatter
import com.hisabkitab.core.designsystem.component.TimePickerModal
import com.hisabkitab.core.designsystem.theme.supportsDynamicColor
import com.hisabkitab.core.designsystem.theme.swatch
import com.hisabkitab.core.model.BudgetPeriod
import com.hisabkitab.core.model.ColorPalette
import com.hisabkitab.core.model.ThemeMode
import com.hisabkitab.core.model.UserPreferences
import com.hisabkitab.core.ui.DateFormats
import com.hisabkitab.core.ui.LocalMoneyFormatter
import java.time.LocalDate
import java.util.Currency
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    versionName: String,
    onBack: () -> Unit,
    onOpenBudget: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showCurrencyDialog by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    val exportTransactionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(CSV_MIME_TYPE),
    ) { uri -> if (uri != null) viewModel.export(ExportKind.TRANSACTIONS, uri) }
    val exportCategoriesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(CSV_MIME_TYPE),
    ) { uri -> if (uri != null) viewModel.export(ExportKind.CATEGORIES, uri) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> viewModel.setReminderEnabled(granted) }

    val message = uiState.message
    val messageText = when (message) {
        is SettingsMessage.Exported -> when (message.kind) {
            ExportKind.TRANSACTIONS -> pluralStringResource(R.plurals.exported_transactions, message.count, message.count)
            ExportKind.CATEGORIES -> pluralStringResource(R.plurals.exported_categories, message.count, message.count)
        }
        SettingsMessage.ExportFailed -> stringResource(R.string.export_failed)
        null -> null
    }
    LaunchedEffect(message) {
        if (messageText != null) {
            snackbarHostState.showSnackbar(messageText)
            viewModel.onMessageShown()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(DesignR.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(DesignR.string.action_back))
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val preferences = uiState.preferences ?: return@Scaffold
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 16.dp),
        ) {
            item { SettingsSection(stringResource(R.string.settings_appearance)) }
            item {
                ThemeModeSelector(
                    selected = preferences.themeMode,
                    onSelect = viewModel::setThemeMode,
                )
            }
            if (supportsDynamicColor()) {
                item {
                    SettingsSwitchItem(
                        icon = Icons.Outlined.Wallpaper,
                        title = stringResource(R.string.dynamic_color),
                        summary = stringResource(R.string.dynamic_color_summary),
                        checked = preferences.useDynamicColor,
                        onCheckedChange = viewModel::setUseDynamicColor,
                    )
                }
            }
            item {
                PaletteSelector(
                    selected = preferences.palette,
                    enabled = !(preferences.useDynamicColor && supportsDynamicColor()),
                    onSelect = viewModel::setPalette,
                )
            }

            item { SettingsSection(stringResource(R.string.settings_money)) }
            item {
                val formatter = LocalMoneyFormatter.current
                SettingsItem(
                    icon = Icons.Outlined.Payments,
                    title = stringResource(R.string.currency),
                    summary = "${formatter.currency.currencyCode} · ${formatter.currency.getDisplayName(Locale.getDefault())} (${formatter.symbol})",
                    onClick = { showCurrencyDialog = true },
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Savings,
                    title = stringResource(DesignR.string.budget),
                    summary = budgetSummary(preferences),
                    onClick = onOpenBudget,
                )
            }

            item { SettingsSection(stringResource(R.string.settings_reminders)) }
            item {
                SettingsSwitchItem(
                    icon = Icons.Outlined.NotificationsActive,
                    title = stringResource(R.string.daily_reminder),
                    summary = stringResource(R.string.daily_reminder_summary),
                    checked = preferences.reminder.enabled,
                    onCheckedChange = { enabled ->
                        val needsPermission = enabled &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                            PackageManager.PERMISSION_GRANTED
                        if (needsPermission) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.setReminderEnabled(enabled)
                        }
                    },
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Schedule,
                    title = stringResource(R.string.reminder_time),
                    summary = DateFormats.time(preferences.reminder.time),
                    enabled = preferences.reminder.enabled,
                    onClick = { showTimePicker = true },
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Notifications,
                    title = stringResource(R.string.notification_settings),
                    summary = stringResource(R.string.notification_settings_summary),
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
                        )
                    },
                )
            }

            item { SettingsSection(stringResource(R.string.settings_data)) }
            item {
                SettingsItem(
                    icon = Icons.Outlined.TableChart,
                    title = stringResource(R.string.export_transactions),
                    summary = stringResource(R.string.export_summary),
                    enabled = !uiState.isExporting,
                    onClick = { exportTransactionsLauncher.launch(exportFileName("transactions")) },
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Category,
                    title = stringResource(R.string.export_categories),
                    summary = stringResource(R.string.export_summary),
                    enabled = !uiState.isExporting,
                    onClick = { exportCategoriesLauncher.launch(exportFileName("categories")) },
                )
            }

            item { SettingsSection(stringResource(R.string.settings_about)) }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    title = stringResource(DesignR.string.app_name),
                    summary = stringResource(R.string.version, versionName),
                )
            }
        }
    }

    if (showCurrencyDialog) {
        CurrencyDialog(
            selectedCode = uiState.preferences?.currencyCode,
            onSelect = {
                viewModel.setCurrency(it)
                showCurrencyDialog = false
            },
            onDismiss = { showCurrencyDialog = false },
        )
    }
    val reminderTime = uiState.preferences?.reminder?.time
    if (showTimePicker && reminderTime != null) {
        TimePickerModal(
            initialTime = reminderTime,
            onTimeSelected = viewModel::setReminderTime,
            onDismiss = { showTimePicker = false },
        )
    }
}

@Composable
private fun budgetSummary(preferences: UserPreferences): String {
    val budget = preferences.budget ?: return stringResource(R.string.budget_not_set)
    val amount = LocalMoneyFormatter.current.format(budget.amountMinor)
    val period = stringResource(
        when (budget.period) {
            BudgetPeriod.DAILY -> DesignR.string.period_daily
            BudgetPeriod.WEEKLY -> DesignR.string.period_weekly
            BudgetPeriod.MONTHLY -> DesignR.string.period_monthly
            BudgetPeriod.CUSTOM -> DesignR.string.period_custom
        },
    )
    return "$amount · $period"
}

private fun exportFileName(kind: String) = "hisab-kitab-$kind-${LocalDate.now()}.csv"

@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    summary: String,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    ListItem(
        modifier = Modifier
            .then(if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier)
            .alpha(if (enabled) 1f else DISABLED_ALPHA),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = { Icon(icon, contentDescription = null) },
        headlineContent = { Text(title) },
        supportingContent = { Text(summary) },
    )
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(role = Role.Switch) { onCheckedChange(!checked) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = { Icon(icon, contentDescription = null) },
        headlineContent = { Text(title) },
        supportingContent = { Text(summary) },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
    )
}

@Composable
private fun ThemeModeSelector(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    val options = listOf(
        ThemeMode.SYSTEM to R.string.theme_system,
        ThemeMode.LIGHT to R.string.theme_light,
        ThemeMode.DARK to R.string.theme_dark,
    )
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(stringResource(R.string.theme), style = MaterialTheme.typography.bodyLarge)
        SingleChoiceSegmentedButtonRow(
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            options.forEachIndexed { index, (mode, label) ->
                SegmentedButton(
                    selected = mode == selected,
                    onClick = { onSelect(mode) },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                ) { Text(stringResource(label)) }
            }
        }
    }
}

@Composable
private fun PaletteSelector(selected: ColorPalette, enabled: Boolean, onSelect: (ColorPalette) -> Unit) {
    Column(
        Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .alpha(if (enabled) 1f else DISABLED_ALPHA),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = stringResource(R.string.color_theme),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 16.dp),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            ColorPalette.entries.forEach { palette ->
                val isSelected = palette == selected
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(palette.swatch, CircleShape)
                        .then(
                            if (isSelected) {
                                Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                            } else {
                                Modifier
                            },
                        )
                        .selectable(
                            selected = isSelected,
                            enabled = enabled,
                            role = Role.RadioButton,
                            onClick = { onSelect(palette) },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun CurrencyDialog(selectedCode: String?, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    val locale = Locale.getDefault()
    val currencies = remember {
        (listOf(MoneyFormatter.defaultCurrencyCode()) + POPULAR_CURRENCIES)
            .distinct()
            .mapNotNull { code -> runCatching { Currency.getInstance(code) }.getOrNull() }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.currency)) },
        text = {
            LazyColumn {
                items(currencies, key = { it.currencyCode }) { currency ->
                    val isSelected = currency.currencyCode == selectedCode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected = isSelected, role = Role.RadioButton) { onSelect(currency.currencyCode) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = isSelected, onClick = null)
                        Column(Modifier.padding(start = 16.dp)) {
                            Text("${currency.currencyCode} · ${currency.getSymbol(locale)}", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                currency.getDisplayName(locale),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(DesignR.string.action_close)) }
        },
    )
}

private const val CSV_MIME_TYPE = "text/csv"
private const val DISABLED_ALPHA = 0.38f
private val POPULAR_CURRENCIES = listOf(
    "INR", "USD", "EUR", "GBP", "JPY", "CNY", "AED", "SAR", "AUD", "CAD",
    "SGD", "CHF", "NPR", "BDT", "PKR", "LKR", "ZAR", "BRL", "KRW", "RUB",
)
