package com.hisabkitab.feature.transactions.editor

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalResources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hisabkitab.core.common.calculator.AmountInput
import com.hisabkitab.core.designsystem.R as DesignR
import com.hisabkitab.core.designsystem.component.BackdropHeader
import com.hisabkitab.core.designsystem.component.CategoryIconBadge
import com.hisabkitab.core.designsystem.component.DatePickerModal
import com.hisabkitab.core.designsystem.component.GlassIconButton
import com.hisabkitab.core.designsystem.component.GradientButton
import com.hisabkitab.core.designsystem.component.SlidingSegmentedControl
import com.hisabkitab.core.designsystem.component.TimePickerModal
import com.hisabkitab.core.designsystem.component.glass
import com.hisabkitab.core.designsystem.theme.HisabKitabTheme
import com.hisabkitab.core.model.Category
import com.hisabkitab.core.model.TransactionType
import com.hisabkitab.core.ui.DateFormats
import com.hisabkitab.core.ui.displayName
import com.hisabkitab.core.ui.LocalMoneyFormatter
import com.hisabkitab.core.ui.relativeDayLabel
import com.hisabkitab.feature.transactions.R
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun TransactionEditorScreen(
    viewModel: TransactionEditorViewModel,
    onBack: () -> Unit,
    onAddCategory: (TransactionType) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onBack()
    }

    TransactionEditorScreen(
        uiState = uiState,
        onBack = onBack,
        onTypeChange = viewModel::onTypeChange,
        onDigit = viewModel::onDigit,
        onDecimalPoint = viewModel::onDecimalPoint,
        onOperator = viewModel::onOperator,
        onBackspace = viewModel::onBackspace,
        onClearAmount = viewModel::onClearAmount,
        onCategorySelected = viewModel::onCategorySelected,
        onAddCategory = { onAddCategory(uiState.type) },
        onDateSelected = viewModel::onDateSelected,
        onTimeSelected = viewModel::onTimeSelected,
        onNoteChange = viewModel::onNoteChange,
        onErrorShown = viewModel::onErrorShown,
        onSave = viewModel::save,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TransactionEditorScreen(
    uiState: TransactionEditorUiState,
    onBack: () -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onDigit: (Char) -> Unit,
    onDecimalPoint: () -> Unit,
    onOperator: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClearAmount: () -> Unit,
    onCategorySelected: (Long) -> Unit,
    onAddCategory: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    onNoteChange: (String) -> Unit,
    onErrorShown: () -> Unit,
    onSave: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(resources.getString(message))
            onErrorShown()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .imePadding(),
        ) {
            BackdropHeader(
                title = stringResource(if (uiState.isEditing) R.string.edit_transaction else R.string.new_transaction),
                navigationIcon = {
                    GlassIconButton(Icons.Filled.Close, stringResource(DesignR.string.action_close), onBack)
                },
            )
            // The amount stays put; categories, date and note scroll beneath it.
            AmountDisplay(uiState.expression, uiState.amountMinor, uiState.showsCalculation, uiState.type)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                CategoryGrid(uiState.categories, uiState.selectedCategoryId, onCategorySelected, onAddCategory)
                Spacer(Modifier.height(4.dp))
                NoteField(uiState.note, onNoteChange, Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                Spacer(Modifier.height(8.dp))
            }

            AnimatedVisibility(visible = !WindowInsets.isImeVisible) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glass(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp), strong = true)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = if (HisabKitabTheme.colors.isDark) 0.12f else 0.08f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.02f),
                                ),
                            ),
                        )
                        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                        .padding(top = 8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        WhenChip(
                            label = relativeDayLabel(uiState.date, LocalDate.now()),
                            icon = Icons.Outlined.CalendarToday,
                            onClick = { showDatePicker = true },
                            modifier = Modifier.weight(1.4f),
                        )
                        WhenChip(
                            label = DateFormats.time(uiState.time),
                            icon = Icons.Outlined.Schedule,
                            onClick = { showTimePicker = true },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    CalculatorKeypad(
                        onDigit = onDigit,
                        onDecimalPoint = onDecimalPoint,
                        onOperator = onOperator,
                        onBackspace = onBackspace,
                        onClear = onClearAmount,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    GradientButton(
                        text = stringResource(if (uiState.isEditing) R.string.save_changes else R.string.save_transaction),
                        onClick = onSave,
                        enabled = uiState.canSave,
                        icon = Icons.Filled.Check,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                    // Expense/income switch sits last, within thumb reach.
                    SlidingSegmentedControl(
                        options = listOf(
                            TransactionType.EXPENSE to stringResource(DesignR.string.expense),
                            TransactionType.INCOME to stringResource(DesignR.string.income),
                        ),
                        selected = uiState.type,
                        onSelect = onTypeChange,
                        modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 10.dp),
                    )
                }
            }
        }
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.TopCenter).padding(top = 96.dp))
    }

    if (showDatePicker) {
        DatePickerModal(initialDate = uiState.date, onDateSelected = onDateSelected, onDismiss = { showDatePicker = false })
    }
    if (showTimePicker) {
        TimePickerModal(initialTime = uiState.time, onTimeSelected = onTimeSelected, onDismiss = { showTimePicker = false })
    }
}

@Composable
private fun AmountDisplay(expression: String, amountMinor: Long?, showsCalculation: Boolean, type: TransactionType) {
    val formatter = LocalMoneyFormatter.current
    val colors = HisabKitabTheme.colors
    val signColor by animateColorAsState(if (type == TransactionType.EXPENSE) colors.expense else colors.income, label = "sign")
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(34.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center,
            ) {
                OperatorGlyph(if (type == TransactionType.EXPENSE) AmountInput.MINUS else AmountInput.PLUS, signColor, size = 14.dp)
            }
            Spacer(Modifier.width(12.dp))
            BasicText(
                text = formatter.symbol + expression.ifEmpty { "0" },
                style = MaterialTheme.typography.displayLarge.copy(
                    color = if (expression.isEmpty()) colors.onBackdropMuted else colors.onBackdrop,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 26.sp, maxFontSize = 52.sp),
            )
        }
        if (showsCalculation) {
            Text(
                text = stringResource(R.string.calculated_as, amountMinor?.let(formatter::format) ?: "—"),
                style = MaterialTheme.typography.titleMedium,
                color = colors.onBackdropMuted,
            )
        }
    }
}

/** Every category at a glance, three per row with large icons. */
@Composable
private fun CategoryGrid(
    categories: List<Category>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    onAddCategory: () -> Unit,
) {
    Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
        val columns = CATEGORY_COLUMNS
        val cells: List<Category?> = categories + null // null is the "new category" cell
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            cells.chunked(columns).forEach { row ->
                Row(Modifier.fillMaxWidth()) {
                    row.forEach { category ->
                        Box(Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                            if (category != null) {
                                CategoryCell(category, category.id == selectedId) { onSelect(category.id) }
                            } else {
                                NewCategoryCell(onAddCategory)
                            }
                        }
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun CategoryCell(category: Category, selected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (selected) 1.08f else 1f, spring(dampingRatio = 0.5f), label = "catScale")
    CategoryChoice(label = category.displayName(), selected = selected, onClick = onClick) {
        Box(
            Modifier
                .scale(scale)
                .then(if (selected) Modifier.border(2.5.dp, HisabKitabTheme.colors.onBackdrop, CircleShape) else Modifier)
                .padding(3.dp),
        ) {
            CategoryIconBadge(category.iconKey, category.color, size = CATEGORY_ICON_SIZE, filled = true)
        }
    }
}

@Composable
private fun NewCategoryCell(onClick: () -> Unit) {
    CategoryChoice(label = stringResource(R.string.new_category_short), selected = false, onClick = onClick) {
        Box(
            modifier = Modifier
                .padding(3.dp)
                .size(CATEGORY_ICON_SIZE)
                .clip(CircleShape)
                .background(HisabKitabTheme.colors.onBackdrop.copy(alpha = 0.14f))
                .border(1.5.dp, HisabKitabTheme.colors.onBackdrop.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = HisabKitabTheme.colors.onBackdrop, modifier = Modifier.size(30.dp))
        }
    }
}

private const val CATEGORY_COLUMNS = 3
private val CATEGORY_ICON_SIZE = 60.dp

@Composable
private fun CategoryChoice(label: String, selected: Boolean, onClick: () -> Unit, icon: @Composable () -> Unit) {
    val colors = HisabKitabTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        icon()
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) colors.onBackdrop else colors.onBackdropMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

/** Date or time button inside the keypad panel. */
@Composable
private fun WhenChip(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .height(42.dp)
            .clip(CircleShape)
            .background(lerp(scheme.surface, scheme.primary, 0.1f))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = scheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun NoteField(note: String, onNoteChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val focusManager = LocalFocusManager.current
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .glass(CircleShape)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.AutoMirrored.Outlined.Notes, contentDescription = null, tint = scheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Box(Modifier.weight(1f)) {
            if (note.isEmpty()) {
                Text(stringResource(R.string.note_optional), style = MaterialTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
            }
            BasicTextField(
                value = note,
                onValueChange = onNoteChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = scheme.onSurface),
                cursorBrush = SolidColor(scheme.primary),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
