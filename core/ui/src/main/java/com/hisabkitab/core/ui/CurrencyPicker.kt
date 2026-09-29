package com.hisabkitab.core.ui

import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.hisabkitab.core.common.money.MoneyFormatter
import com.hisabkitab.core.designsystem.R as DesignR
import java.util.Currency
import java.util.Locale

private val POPULAR_CURRENCIES = listOf(
    "INR", "USD", "EUR", "GBP", "JPY", "CNY", "AED", "SAR", "AUD", "CAD",
    "SGD", "CHF", "NPR", "BDT", "PKR", "LKR", "ZAR", "BRL", "KRW", "RUB",
)

/** Device currency first, then common ones. */
fun currencyChoices(): List<Currency> =
    (listOf(MoneyFormatter.defaultCurrencyCode()) + POPULAR_CURRENCIES)
        .distinct()
        .mapNotNull { code -> runCatching { Currency.getInstance(code) }.getOrNull() }

fun Currency.label(locale: Locale = Locale.getDefault()): String =
    "$currencyCode · ${getDisplayName(locale)} (${getSymbol(locale)})"

@Composable
fun CurrencyDialog(selectedCode: String?, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    val currencies = remember { currencyChoices() }
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
