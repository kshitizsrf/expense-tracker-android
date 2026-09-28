package com.hisabkitab.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.hisabkitab.core.designsystem.component.CategoryIconBadge
import com.hisabkitab.core.designsystem.theme.HisabKitabTheme
import com.hisabkitab.core.model.Transaction
import com.hisabkitab.core.model.TransactionType
import java.time.ZoneId

@Composable
fun TransactionListItem(
    transaction: Transaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDate: Boolean = false,
    zone: ZoneId = ZoneId.systemDefault(),
) {
    val formatter = LocalMoneyFormatter.current
    val dateTime = transaction.occurredAt.atZone(zone)
    val whenText = if (showDate) {
        "${DateFormats.dayMonth(dateTime.toLocalDate())} · ${DateFormats.time(dateTime.toLocalTime())}"
    } else {
        DateFormats.time(dateTime.toLocalTime())
    }

    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = {
            CategoryIconBadge(iconKey = transaction.category.iconKey, color = transaction.category.color)
        },
        headlineContent = {
            Text(
                text = transaction.category.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Text(
                text = transaction.note.ifBlank { whenText },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatter.formatSigned(transaction.amountMinor, transaction.type),
                    style = MaterialTheme.typography.titleMedium,
                    color = transaction.type.amountColor(),
                )
                if (transaction.note.isNotBlank()) {
                    Text(
                        text = whenText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
    )
}

@Composable
fun TransactionType.amountColor(): Color = when (this) {
    TransactionType.EXPENSE -> HisabKitabTheme.financeColors.expense
    TransactionType.INCOME -> HisabKitabTheme.financeColors.income
}
