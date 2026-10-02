package com.hisabkitab.feature.categories.editor

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceIn
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hisabkitab.core.designsystem.R as DesignR
import com.hisabkitab.core.designsystem.component.BackdropHeader
import com.hisabkitab.core.designsystem.component.CategoryIconBadge
import com.hisabkitab.core.designsystem.component.GlassCard
import com.hisabkitab.core.designsystem.component.GlassIconButton
import com.hisabkitab.core.designsystem.component.GradientButton
import com.hisabkitab.core.designsystem.component.LoadingState
import com.hisabkitab.core.designsystem.component.SlidingSegmentedControl
import com.hisabkitab.core.designsystem.component.glass
import com.hisabkitab.core.designsystem.theme.HisabKitabTheme
import com.hisabkitab.core.icons.CategoryIcons
import com.hisabkitab.core.model.Category
import com.hisabkitab.core.model.CategoryColors
import com.hisabkitab.core.model.TransactionType
import com.hisabkitab.core.ui.displayName
import com.hisabkitab.feature.categories.R

@Composable
fun CategoryEditorScreen(
    viewModel: CategoryEditorViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) onBack()
    }

    val localized = if (!uiState.isLoading && uiState.defaultKey != null) {
        Category(0, uiState.name, uiState.iconKey, uiState.color, uiState.type, uiState.defaultKey).displayName()
    } else {
        null
    }
    LaunchedEffect(localized) {
        if (localized != null) viewModel.adoptLocalizedName(localized)
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            .imePadding(),
    ) {
        Column(Modifier.fillMaxSize()) {
            BackdropHeader(
                title = stringResource(if (uiState.isEditing) R.string.edit_category else R.string.new_category),
                navigationIcon = { GlassIconButton(Icons.Filled.Close, stringResource(DesignR.string.action_close), onBack) },
                actions = {
                    if (uiState.isEditing) {
                        GlassIconButton(
                            Icons.Outlined.Delete,
                            stringResource(DesignR.string.action_delete),
                            { showDeleteDialog = true },
                            enabled = !uiState.isSaving,
                        )
                    }
                },
            )
            if (uiState.isLoading) {
                LoadingState()
            } else {
                EditorContent(
                    uiState = uiState,
                    onNameChange = viewModel::onNameChange,
                    onTypeChange = viewModel::onTypeChange,
                    onColorSelected = viewModel::onColorSelected,
                    onIconSelected = viewModel::onIconSelected,
                    onSave = viewModel::save,
                )
            }
        }
        GradientButton(
            text = stringResource(DesignR.string.action_save),
            onClick = viewModel::save,
            enabled = uiState.canSave,
            icon = Icons.Filled.Check,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp),
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
            title = { Text(stringResource(R.string.delete_category_title, uiState.name)) },
            text = {
                Text(
                    if (uiState.transactionCount > 0) {
                        pluralStringResource(R.plurals.delete_category_message, uiState.transactionCount, uiState.transactionCount)
                    } else {
                        stringResource(R.string.delete_category_message_empty)
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; viewModel.delete() }) {
                    Text(stringResource(DesignR.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(DesignR.string.action_cancel)) }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditorContent(
    uiState: CategoryEditorUiState,
    onNameChange: (String) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onColorSelected: (Int) -> Unit,
    onIconSelected: (String) -> Unit,
    onSave: () -> Unit,
) {
    val colors = HisabKitabTheme.colors
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 58.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "preview", span = { GridItemSpan(maxLineSpan) }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 8.dp)) {
                CategoryIconBadge(uiState.iconKey, uiState.color, size = 96.dp, filled = true)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = uiState.name.ifBlank { stringResource(R.string.preview) },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (uiState.name.isBlank()) colors.onBackdropMuted else colors.onBackdrop,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        item(key = "name", span = { GridItemSpan(maxLineSpan) }) {
            Column {
                NameField(uiState.name, onNameChange, onSave, isError = uiState.nameError != null)
                if (uiState.nameError != null) {
                    Text(
                        stringResource(uiState.nameError),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(start = 20.dp, top = 6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
                if (!uiState.isEditing) {
                    Spacer(Modifier.height(12.dp))
                    SlidingSegmentedControl(
                        options = listOf(
                            TransactionType.EXPENSE to stringResource(DesignR.string.expense),
                            TransactionType.INCOME to stringResource(DesignR.string.income),
                        ),
                        selected = uiState.type,
                        onSelect = onTypeChange,
                    )
                }
            }
        }
        item(key = "colors", span = { GridItemSpan(maxLineSpan) }) {
            GlassCard(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.color), style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(12.dp))
                ColorPalette(selected = uiState.color, onSelect = onColorSelected)
            }
        }
        item(key = "icon-label", span = { GridItemSpan(maxLineSpan) }) {
            Text(
                stringResource(R.string.icon),
                style = MaterialTheme.typography.titleSmall,
                color = colors.onBackdrop,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp),
            )
        }
        items(CategoryIcons.all, key = { it.key }) { icon ->
            IconCell(resId = icon.resId, selected = icon.key == uiState.iconKey, color = uiState.color) { onIconSelected(icon.key) }
        }
    }
}

@Composable
private fun NameField(name: String, onNameChange: (String) -> Unit, onDone: () -> Unit, isError: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .glass(CircleShape)
            .then(if (isError) Modifier.border(2.dp, scheme.error, CircleShape) else Modifier)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f)) {
            if (name.isEmpty()) {
                Text(stringResource(R.string.category_name), style = MaterialTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
            }
            BasicTextField(
                value = name,
                onValueChange = onNameChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = scheme.onSurface),
                cursorBrush = SolidColor(scheme.primary),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onDone() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun IconCell(resId: Int, selected: Boolean, color: Int, onClick: () -> Unit) {
    val tint = Color(color)
    val background by animateColorAsState(if (selected) tint else Color.Transparent, label = "iconBg")
    val scale by animateFloatAsState(if (selected) 1.08f else 1f, spring(dampingRatio = 0.5f), label = "iconScale")
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .scale(scale)
            .glass(CircleShape)
            .background(background, CircleShape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics { this.selected = selected },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(resId),
            contentDescription = null,
            tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(26.dp),
        )
    }
}

/**
 * Every color in an even grid: 5 per row on phones (15 colors make 3 full rows), all 15 in a
 * row on wide screens, so no row ever ends in a gap.
 */
@Composable
private fun ColorPalette(selected: Int, onSelect: (Int) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val perRow = if (maxWidth >= 52.dp * CategoryColors.all.size) CategoryColors.all.size else 5
        val swatch = (maxWidth / perRow - 10.dp).coerceIn(32.dp, 48.dp)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CategoryColors.all.chunked(perRow).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    row.forEach { color ->
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            ColorSwatch(color = color, selected = color == selected, size = swatch, onClick = { onSelect(color) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: Int, selected: Boolean, size: Dp, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (selected) 1.12f else 1f, spring(dampingRatio = 0.5f), label = "swatchScale")
    Box(
        modifier = Modifier
            .scale(scale)
            .size(size)
            .clip(CircleShape)
            .background(Color(color))
            .then(if (selected) Modifier.border(3.dp, Color.White, CircleShape) else Modifier)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics { this.selected = selected },
        contentAlignment = Alignment.Center,
    ) {
        if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
    }
}
