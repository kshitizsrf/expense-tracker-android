package com.hisabkitab.feature.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hisabkitab.core.designsystem.R as DesignR
import com.hisabkitab.core.designsystem.component.BackdropHeader
import com.hisabkitab.core.designsystem.component.GlassCard
import com.hisabkitab.core.designsystem.component.GlassChip
import com.hisabkitab.core.designsystem.component.GlassIconButton
import com.hisabkitab.core.designsystem.component.LiquidNavItem
import com.hisabkitab.core.designsystem.component.LiquidNavigationBar
import com.hisabkitab.core.designsystem.component.SlidingSegmentedControl
import com.hisabkitab.core.designsystem.component.TimePickerModal
import com.hisabkitab.core.designsystem.component.ThemeGallery
import com.hisabkitab.core.designsystem.theme.HisabKitabTheme
import com.hisabkitab.core.model.BudgetPeriod
import com.hisabkitab.core.model.ChartTransition
import com.hisabkitab.core.model.NavBarStyle
import com.hisabkitab.core.security.authenticate
import com.hisabkitab.core.security.canUseAppLock
import com.hisabkitab.core.security.findFragmentActivity
import com.hisabkitab.core.ui.AppLanguages
import com.hisabkitab.core.ui.LanguageDialog
import com.hisabkitab.core.model.ThemeMode
import com.hisabkitab.core.model.UserPreferences
import com.hisabkitab.core.ui.CurrencyDialog
import com.hisabkitab.core.ui.DateFormats
import com.hisabkitab.core.ui.LocalMoneyFormatter
import com.hisabkitab.core.ui.label
import java.time.LocalDate

@Composable
fun SettingsScreen(
    versionName: String,
    onBack: () -> Unit,
    onOpenBudget: () -> Unit,
    onOpenAbout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showCurrencyDialog by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    var showNameDialog by rememberSaveable { mutableStateOf(false) }
    val lockAvailable = remember { canUseAppLock(context) }
    val lockPromptTitle = stringResource(R.string.app_lock_confirm_title)
    val lockPromptSubtitle = stringResource(R.string.app_lock_confirm_subtitle)
    val systemBars = WindowInsets.systemBars.asPaddingValues()
    val colors = HisabKitabTheme.colors

    val exportTransactionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(CSV_MIME_TYPE),
    ) { uri -> if (uri != null) viewModel.export(ExportKind.TRANSACTIONS, uri) }
    val exportCategoriesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(CSV_MIME_TYPE),
    ) { uri -> if (uri != null) viewModel.export(ExportKind.CATEGORIES, uri) }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.importTransactions(uri)
    }
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
        is SettingsMessage.Imported -> buildString {
            append(pluralStringResource(R.plurals.imported_transactions, message.count, message.count))
            if (message.skipped > 0) {
                append(" · ")
                append(pluralStringResource(R.plurals.import_skipped, message.skipped, message.skipped))
            }
        }
        SettingsMessage.ImportFailed -> stringResource(R.string.import_failed)
        SettingsMessage.ImportUnsupported -> stringResource(R.string.import_unsupported)
        null -> null
    }
    LaunchedEffect(message) {
        if (messageText != null) {
            snackbarHostState.showSnackbar(messageText)
            viewModel.onMessageShown()
        }
    }

    Box(Modifier.fillMaxSize()) {
        val preferences = uiState.preferences
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = systemBars.calculateTopPadding(), bottom = systemBars.calculateBottomPadding() + 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                BackdropHeader(
                    title = stringResource(R.string.settings_title_big),
                    subtitle = stringResource(R.string.settings_subtitle),
                    navigationIcon = { GlassIconButton(Icons.AutoMirrored.Filled.ArrowBack, stringResource(DesignR.string.action_back), onBack) },
                )
            }
            if (preferences == null) return@LazyColumn

            item {
                ProfileCard(name = preferences.userName, onClick = { showNameDialog = true })
            }

            item {
                Text(
                    stringResource(R.string.settings_theme_gallery),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onBackdrop,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            item { ThemeGallery(selected = preferences.theme, onSelect = viewModel::setTheme) }
            item {
                SlidingSegmentedControl(
                    options = listOf(
                        ThemeMode.SYSTEM to stringResource(R.string.theme_system),
                        ThemeMode.LIGHT to stringResource(R.string.theme_light),
                        ThemeMode.DARK to stringResource(R.string.theme_dark),
                    ),
                    selected = preferences.themeMode,
                    onSelect = viewModel::setThemeMode,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            item {
                SettingsGroup(stringResource(R.string.settings_motion)) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(stringResource(R.string.nav_style), style = MaterialTheme.typography.bodyLarge)
                        Text(stringResource(R.string.nav_style_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        ChoiceChips(
                            options = NavBarStyle.entries.map { it to stringResource(it.labelRes()) },
                            selected = preferences.navBarStyle,
                            onSelect = viewModel::setNavBarStyle,
                        )
                        NavBarPreview(preferences.navBarStyle)
                    }
                    GroupDivider()
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(stringResource(R.string.chart_animation), style = MaterialTheme.typography.bodyLarge)
                        Text(stringResource(R.string.chart_animation_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        ChoiceChips(
                            options = ChartTransition.entries.map { it to stringResource(it.labelRes()) },
                            selected = preferences.chartTransition,
                            onSelect = viewModel::setChartTransition,
                        )
                    }
                }
            }

            item {
                SettingsGroup(stringResource(R.string.settings_privacy)) {
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Lock,
                        title = stringResource(R.string.app_lock),
                        summary = stringResource(if (lockAvailable) R.string.app_lock_summary else R.string.app_lock_unavailable),
                        checked = preferences.appLockEnabled,
                        enabled = lockAvailable,
                        onCheckedChange = { enable ->
                            if (!enable) {
                                viewModel.setAppLockEnabled(false)
                            } else {
                                // Confirm the user can unlock before turning the lock on.
                                context.findFragmentActivity()?.let { activity ->
                                    authenticate(activity, lockPromptTitle, lockPromptSubtitle, onSuccess = { viewModel.setAppLockEnabled(true) })
                                }
                            }
                        },
                    )
                }
            }

            item {
                SettingsGroup(stringResource(R.string.settings_money)) {
                    val language = remember { AppLanguages.current() }
                    SettingsRow(Icons.Outlined.Language, stringResource(R.string.language_setting), "${language.flag}  ${language.nativeName}") {
                        showLanguageDialog = true
                    }
                    GroupDivider()
                    val currency = LocalMoneyFormatter.current.currency
                    SettingsRow(Icons.Outlined.Payments, stringResource(R.string.currency_setting), currency.label()) {
                        showCurrencyDialog = true
                    }
                    GroupDivider()
                    SettingsRow(Icons.Outlined.Savings, stringResource(DesignR.string.budget), budgetSummary(preferences), onClick = onOpenBudget)
                }
            }

            item {
                SettingsGroup(stringResource(R.string.settings_reminders)) {
                    SettingsSwitchRow(
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
                    GroupDivider()
                    SettingsRow(
                        icon = Icons.Outlined.Schedule,
                        title = stringResource(R.string.reminder_time),
                        summary = DateFormats.time(preferences.reminder.time),
                        enabled = preferences.reminder.enabled,
                        onClick = { showTimePicker = true },
                    )
                    GroupDivider()
                    SettingsRow(
                        icon = Icons.Outlined.Notifications,
                        title = stringResource(R.string.notification_settings),
                        summary = stringResource(R.string.notification_settings_summary),
                        onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
                            )
                        },
                    )
                }
            }

            item {
                SettingsGroup(stringResource(R.string.settings_data)) {
                    SettingsRow(
                        icon = Icons.Outlined.TableChart,
                        title = stringResource(R.string.export_transactions),
                        summary = stringResource(R.string.export_summary),
                        enabled = !uiState.isExporting,
                        onClick = { exportTransactionsLauncher.launch(exportFileName("transactions")) },
                    )
                    GroupDivider()
                    SettingsRow(
                        icon = Icons.Outlined.Category,
                        title = stringResource(R.string.export_categories),
                        summary = stringResource(R.string.export_summary),
                        enabled = !uiState.isExporting,
                        onClick = { exportCategoriesLauncher.launch(exportFileName("categories")) },
                    )
                    GroupDivider()
                    SettingsRow(
                        icon = Icons.Outlined.FileUpload,
                        title = stringResource(R.string.import_transactions),
                        summary = stringResource(R.string.import_summary),
                        enabled = !uiState.isExporting,
                        // Some file managers label CSV as plain text or a generic file.
                        onClick = { importLauncher.launch(arrayOf(CSV_MIME_TYPE, "text/comma-separated-values", "text/plain", "application/octet-stream")) },
                    )
                }
            }

            item {
                SettingsGroup(stringResource(R.string.settings_about)) {
                    SettingsRow(
                        icon = Icons.Outlined.Info,
                        title = stringResource(DesignR.string.app_name),
                        summary = stringResource(R.string.version, versionName),
                        onClick = onOpenAbout,
                    )
                }
            }
            item {
                Text(
                    stringResource(R.string.made_with_love),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onBackdropMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(bottom = systemBars.calculateBottomPadding()))
    }

    if (showNameDialog) {
        NameDialog(
            initialName = uiState.preferences?.userName.orEmpty(),
            onSave = {
                viewModel.setUserName(it)
                showNameDialog = false
            },
            onDismiss = { showNameDialog = false },
        )
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
    if (showLanguageDialog) {
        LanguageDialog(
            selectedTag = AppLanguages.current().tag,
            onSelect = { language ->
                showLanguageDialog = false
                // Follow the new language's currency unless the user picked a different one themselves.
                val previous = AppLanguages.current()
                if (uiState.preferences?.currencyCode == previous.defaultCurrencyCode()) {
                    viewModel.setCurrency(language.defaultCurrencyCode())
                }
                AppLanguages.apply(language)
            },
            onDismiss = { showLanguageDialog = false },
        )
    }
    val reminderTime = uiState.preferences?.reminder?.time
    if (showTimePicker && reminderTime != null) {
        TimePickerModal(initialTime = reminderTime, onTimeSelected = viewModel::setReminderTime, onDismiss = { showTimePicker = false })
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
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            color = HisabKitabTheme.colors.onBackdrop,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        GlassCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 4.dp), content = content)
    }
}

@Composable
private fun GroupDivider() {
    HorizontalDivider(Modifier.padding(start = 72.dp, end = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun RowIcon(icon: ImageVector) {
    Box(
        Modifier.size(40.dp).clip(CircleShape).background(Brush.linearGradient(HisabKitabTheme.colors.accentGradient)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    summary: String,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier)
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RowIcon(icon)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (onClick != null) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, role = Role.Switch) { onCheckedChange(!checked) }
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RowIcon(icon)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}

private const val CSV_MIME_TYPE = "text/csv"
private const val DISABLED_ALPHA = 0.38f

private fun NavBarStyle.labelRes() = when (this) {
    NavBarStyle.GLOW -> R.string.nav_style_glow
    NavBarStyle.LIQUID -> R.string.nav_style_liquid
    NavBarStyle.BUBBLE -> R.string.nav_style_bubble
}

private fun ChartTransition.labelRes() = when (this) {
    ChartTransition.ZOOM -> R.string.chart_zoom
    ChartTransition.SLIDE -> R.string.chart_slide
    ChartTransition.FLIP -> R.string.chart_flip
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> ChoiceChips(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (value, label) ->
            GlassChip(label = label, selected = value == selected, onClick = { onSelect(value) }, onBackdrop = false)
        }
    }
}

/** A working miniature of the navigation bar so styles can be tried right here. */
@Composable
private fun NavBarPreview(style: NavBarStyle) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val items = listOf(
        LiquidNavItem(stringResource(R.string.preview_home), Icons.Filled.Home, Icons.Outlined.Home),
        LiquidNavItem(stringResource(DesignR.string.nav_transactions), Icons.AutoMirrored.Filled.ReceiptLong, Icons.AutoMirrored.Outlined.ReceiptLong),
        LiquidNavItem(stringResource(DesignR.string.nav_stats), Icons.Filled.Insights, Icons.Outlined.Insights),
        LiquidNavItem(stringResource(DesignR.string.nav_categories), Icons.Filled.Category, Icons.Outlined.Category),
    )
    Box(Modifier.fillMaxWidth().padding(top = 24.dp)) {
        LiquidNavigationBar(
            items = items,
            selectedIndex = selected,
            onSelect = { selected = it },
            centerIcon = Icons.Filled.Add,
            centerContentDescription = stringResource(DesignR.string.add_transaction),
            onCenterClick = {},
            style = style,
            applySystemInsets = false,
        )
    }
}

/** The user's name, shown as an avatar card; tapping edits it. */
@Composable
private fun ProfileCard(name: String, onClick: () -> Unit) {
    val colors = HisabKitabTheme.colors
    GlassCard(
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
        onClick = onClick,
        contentPadding = PaddingValues(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(52.dp).clip(CircleShape).background(Brush.linearGradient(colors.accentGradient)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = name.trim().firstOrNull()?.uppercase() ?: "🙂",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = name.ifBlank { stringResource(R.string.profile_add_name) },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    stringResource(R.string.profile_summary),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.profile_edit), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun NameDialog(initialName: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_name_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= MAX_NAME_LENGTH) name = it },
                label = { Text(stringResource(R.string.profile_name_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSave(name) }),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = { TextButton(onClick = { onSave(name) }) { Text(stringResource(DesignR.string.action_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(DesignR.string.action_cancel)) } },
    )
}

private const val MAX_NAME_LENGTH = 30
