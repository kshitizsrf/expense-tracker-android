package com.hisabkitab.feature.transactions.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hisabkitab.feature.transactions.R
import com.hisabkitab.core.designsystem.R as DesignR
import com.hisabkitab.core.designsystem.component.CategoryIconBadge
import com.hisabkitab.core.designsystem.component.DatePickerModal
import com.hisabkitab.core.designsystem.component.TimePickerModal
import com.hisabkitab.core.model.Category
import com.hisabkitab.core.model.TransactionType
import com.hisabkitab.core.ui.DateFormats
import com.hisabkitab.core.ui.LocalMoneyFormatter
import com.hisabkitab.core.ui.amountColor
import com.hisabkitab.core.ui.relativeDayLabel
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(context.getString(message))
            onErrorShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(if (uiState.isEditing) R.string.edit_transaction else R.string.new_transaction))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(DesignR.string.action_close))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding(),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                TypeSelector(
                    selected = uiState.type,
                    onSelect = onTypeChange,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                AmountDisplay(
                    expression = uiState.expression,
                    amountMinor = uiState.amountMinor,
                    showsCalculation = uiState.showsCalculation,
                    type = uiState.type,
                )
                CategorySelector(
                    categories = uiState.categories,
                    selectedId = uiState.selectedCategoryId,
                    onSelect = onCategorySelected,
                    onAddCategory = onAddCategory,
                )
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AssistChip(
                        onClick = { showDatePicker = true },
                        label = { Text(relativeDayLabel(uiState.date, LocalDate.now())) },
                        leadingIcon = { Icon(Icons.Outlined.CalendarToday, contentDescription = null, Modifier.size(18.dp)) },
                    )
                    AssistChip(
                        onClick = { showTimePicker = true },
                        label = { Text(DateFormats.time(uiState.time)) },
                        leadingIcon = { Icon(Icons.Outlined.Schedule, contentDescription = null, Modifier.size(18.dp)) },
                    )
                }
                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = onNoteChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    label = { Text(stringResource(R.string.note_optional)) },
                    leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Notes, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                )
            }

            // The keypad makes way for the system keyboard while the note is being typed.
            AnimatedVisibility(visible = !WindowInsets.isImeVisible) {
                Column {
                    CalculatorKeypad(
                        onDigit = onDigit,
                        onDecimalPoint = onDecimalPoint,
                        onOperator = onOperator,
                        onBackspace = onBackspace,
                        onClear = onClearAmount,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = onSave,
                        enabled = uiState.canSave,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Text(
                            text = stringResource(if (uiState.isEditing) R.string.save_changes else R.string.save_transaction),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerModal(
            initialDate = uiState.date,
            onDateSelected = onDateSelected,
            onDismiss = { showDatePicker = false },
        )
    }
    if (showTimePicker) {
        TimePickerModal(
            initialTime = uiState.time,
            onTimeSelected = onTimeSelected,
            onDismiss = { showTimePicker = false },
        )
    }
}

@Composable
private fun TypeSelector(
    selected: TransactionType,
    onSelect: (TransactionType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(TransactionType.EXPENSE to DesignR.string.expense, TransactionType.INCOME to DesignR.string.income)
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (type, label) ->
            SegmentedButton(
                selected = selected == type,
                onClick = { onSelect(type) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
            ) { Text(stringResource(label)) }
        }
    }
}

@Composable
private fun AmountDisplay(
    expression: String,
    amountMinor: Long?,
    showsCalculation: Boolean,
    type: TransactionType,
) {
    val formatter = LocalMoneyFormatter.current
    val amountColor by animateColorAsState(type.amountColor(), label = "amountColor")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.End,
    ) {
        BasicText(
            text = formatter.symbol + " " + expression.ifEmpty { "0" },
            style = MaterialTheme.typography.displayMedium.copy(
                color = if (expression.isEmpty()) MaterialTheme.colorScheme.outline else amountColor,
                textAlign = TextAlign.End,
            ),
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 24.sp, maxFontSize = 48.sp),
            modifier = Modifier.fillMaxWidth(),
        )
        if (showsCalculation) {
            Text(
                text = "= " + (amountMinor?.let(formatter::format) ?: "—"),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CategorySelector(
    categories: List<Category>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    onAddCategory: () -> Unit,
) {
    Text(
        text = stringResource(R.string.category),
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(categories, key = { it.id }) { category ->
            CategoryChoice(
                category = category,
                selected = category.id == selectedId,
                onClick = { onSelect(category.id) },
            )
        }
        item(key = "add") {
            CategoryChoiceFrame(label = stringResource(R.string.new_category_short), onClick = onAddCategory) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.size(52.dp),
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChoice(category: Category, selected: Boolean, onClick: () -> Unit) {
    CategoryChoiceFrame(label = category.name, selected = selected, onClick = onClick) {
        CategoryIconBadge(
            iconKey = category.iconKey,
            color = category.color,
            size = 52.dp,
            filled = selected,
        )
    }
}

@Composable
private fun CategoryChoiceFrame(
    label: String,
    onClick: () -> Unit,
    selected: Boolean = false,
    icon: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(76.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        icon()
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}
