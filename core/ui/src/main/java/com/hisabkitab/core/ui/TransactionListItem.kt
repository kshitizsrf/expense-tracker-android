package com.hisabkitab.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hisabkitab.core.designsystem.component.CategoryIconBadge
import com.hisabkitab.core.designsystem.theme.HisabKitabTheme
import com.hisabkitab.core.model.Transaction
import com.hisabkitab.core.model.TransactionType
import java.time.ZoneId

/** One transaction row, designed to sit inside a glass card. */
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

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CategoryIconBadge(iconKey = transaction.category.iconKey, color = transaction.category.color, size = 46.dp)
        Column(Modifier.weight(1f)) {
            Text(
                text = transaction.category.displayName(),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (transaction.note.isBlank()) whenText else "${transaction.note} · $whenText",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = formatter.formatSigned(transaction.amountMinor, transaction.type),
            style = MaterialTheme.typography.titleMedium,
            color = transaction.type.amountColor(),
            maxLines = 1,
        )
    }
}

@Composable
fun TransactionType.amountColor(): Color = when (this) {
    TransactionType.EXPENSE -> HisabKitabTheme.colors.expense
    TransactionType.INCOME -> HisabKitabTheme.colors.income
}
