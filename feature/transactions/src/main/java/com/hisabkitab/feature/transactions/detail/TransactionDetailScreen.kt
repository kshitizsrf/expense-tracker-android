package com.hisabkitab.feature.transactions.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hisabkitab.core.designsystem.R as DesignR
import com.hisabkitab.core.designsystem.component.BackdropHeader
import com.hisabkitab.core.designsystem.component.CategoryIconBadge
import com.hisabkitab.core.designsystem.component.EmptyState
import com.hisabkitab.core.designsystem.component.GlassCard
import com.hisabkitab.core.designsystem.component.GlassIconButton
import com.hisabkitab.core.designsystem.component.GradientButton
import com.hisabkitab.core.designsystem.component.LoadingState
import com.hisabkitab.core.designsystem.theme.HisabKitabTheme
import com.hisabkitab.core.model.Transaction
import com.hisabkitab.core.model.TransactionType
import com.hisabkitab.core.ui.DateFormats
import com.hisabkitab.core.ui.displayName
import com.hisabkitab.core.ui.LocalMoneyFormatter
import com.hisabkitab.core.ui.amountColor
import com.hisabkitab.feature.transactions.R
import java.time.ZoneId

@Composable
fun TransactionDetailScreen(
    viewModel: TransactionDetailViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val systemBars = WindowInsets.systemBars.asPaddingValues()

    LaunchedEffect(uiState) {
        if (uiState is TransactionDetailUiState.Deleted) onBack()
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = systemBars.calculateTopPadding(), bottom = systemBars.calculateBottomPadding()),
    ) {
        BackdropHeader(
            title = stringResource(R.string.transaction_details),
            navigationIcon = {
                GlassIconButton(Icons.AutoMirrored.Filled.ArrowBack, stringResource(DesignR.string.action_back), onBack)
            },
            actions = {
                if (uiState is TransactionDetailUiState.Success) {
                    GlassIconButton(Icons.Outlined.Delete, stringResource(DesignR.string.action_delete), { showDeleteDialog = true })
                }
            },
        )
        when (val state = uiState) {
            TransactionDetailUiState.Loading, TransactionDetailUiState.Deleted -> LoadingState()
            TransactionDetailUiState.NotFound -> GlassCard(Modifier.padding(16.dp).fillMaxWidth()) {
                EmptyState(
                    icon = Icons.Outlined.SearchOff,
                    title = stringResource(R.string.transaction_not_found_title),
                    message = stringResource(R.string.transaction_not_found_message),
                )
            }
            is TransactionDetailUiState.Success -> DetailContent(state.transaction, onEdit)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
            title = { Text(stringResource(R.string.delete_transaction_title)) },
            text = { Text(stringResource(R.string.delete_transaction_message)) },
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

@Composable
private fun DetailContent(transaction: Transaction, onEdit: () -> Unit) {
    val formatter = LocalMoneyFormatter.current
    val colors = HisabKitabTheme.colors
    val dateTime = transaction.occurredAt.atZone(ZoneId.systemDefault())
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(12.dp))
        CategoryIconBadge(transaction.category.iconKey, transaction.category.color, size = 96.dp, filled = true)
        Spacer(Modifier.height(16.dp))
        Text(transaction.category.displayName(), style = MaterialTheme.typography.titleLarge, color = colors.onBackdrop)
        Text(
            text = formatter.formatSigned(transaction.amountMinor, transaction.type),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onBackdrop,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(if (transaction.type == TransactionType.EXPENSE) DesignR.string.expense else DesignR.string.income),
            style = MaterialTheme.typography.labelLarge,
            color = transaction.type.amountColor(),
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                .padding(horizontal = 14.dp, vertical = 6.dp),
        )
        Spacer(Modifier.height(28.dp))
        GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)) {
            DetailRow(Icons.Outlined.CalendarToday, stringResource(R.string.date), DateFormats.full(dateTime.toLocalDate()))
            HorizontalDivider(Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant)
            DetailRow(Icons.Outlined.Schedule, stringResource(R.string.time), DateFormats.time(dateTime.toLocalTime()))
            HorizontalDivider(Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant)
            DetailRow(Icons.AutoMirrored.Outlined.Notes, stringResource(R.string.note), transaction.note.ifBlank { stringResource(R.string.no_note) })
        }
        Spacer(Modifier.height(24.dp))
        GradientButton(
            text = stringResource(DesignR.string.action_edit),
            onClick = onEdit,
            icon = Icons.Outlined.Edit,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
