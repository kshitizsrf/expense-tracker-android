package com.hisabkitab.feature.transactions.editor

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hisabkitab.feature.transactions.R
import com.hisabkitab.core.common.calculator.AmountInput
import com.hisabkitab.core.designsystem.theme.HisabKitabTheme

/**
 * Number pad with basic arithmetic, so amounts like "120 + 45" can be totalled while entering.
 * Long-press backspace clears everything.
 */
@Composable
fun CalculatorKeypad(
    onDigit: (Char) -> Unit,
    onDecimalPoint: () -> Unit,
    onOperator: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows = listOf(
        listOf(Key.Digit('7'), Key.Digit('8'), Key.Digit('9'), Key.Operator(AmountInput.DIVIDE)),
        listOf(Key.Digit('4'), Key.Digit('5'), Key.Digit('6'), Key.Operator(AmountInput.TIMES)),
        listOf(Key.Digit('1'), Key.Digit('2'), Key.Digit('3'), Key.Operator(AmountInput.MINUS)),
        listOf(Key.DecimalPoint, Key.Digit('0'), Key.Backspace, Key.Operator(AmountInput.PLUS)),
    )
    Column(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { key ->
                    KeypadButton(
                        key = key,
                        onClick = {
                            when (key) {
                                is Key.Digit -> onDigit(key.char)
                                is Key.Operator -> onOperator(key.char)
                                Key.DecimalPoint -> onDecimalPoint()
                                Key.Backspace -> onBackspace()
                            }
                        },
                        onLongClick = if (key == Key.Backspace) onClear else null,
                    )
                }
            }
        }
    }
}

private sealed interface Key {
    data class Digit(val char: Char) : Key
    data class Operator(val char: Char) : Key
    data object DecimalPoint : Key
    data object Backspace : Key
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RowScope.KeypadButton(
    key: Key,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
) {
    val view = LocalView.current
    val colors = MaterialTheme.colorScheme
    val (container, content) = when (key) {
        is Key.Operator -> colors.secondaryContainer to colors.onSecondaryContainer
        Key.Backspace -> colors.surfaceContainerHighest to colors.onSurface
        else -> colors.surfaceContainerHigh to colors.onSurface
    }
    val description = when (key) {
        Key.Backspace -> stringResource(R.string.keypad_backspace)
        Key.DecimalPoint -> stringResource(R.string.keypad_decimal)
        is Key.Operator -> key.char.toString()
        is Key.Digit -> key.char.toString()
    }

    Surface(
        color = container,
        contentColor = content,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .weight(1f)
            .height(56.dp)
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                role = Role.Button,
                onClickLabel = description,
                // combinedClickable already performs long-press haptics.
                onLongClick = onLongClick,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onClick()
                },
            ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            when (key) {
                Key.Backspace -> Icon(Icons.AutoMirrored.Outlined.Backspace, contentDescription = description)
                Key.DecimalPoint -> KeyLabel(".", Color.Unspecified)
                is Key.Digit -> KeyLabel(key.char.toString(), Color.Unspecified)
                is Key.Operator -> KeyLabel(key.char.toString(), Color.Unspecified)
            }
        }
    }
}

@Composable
private fun KeyLabel(text: String, color: Color) {
    Text(text = text, style = MaterialTheme.typography.headlineSmall, color = color)
}

@Preview
@Composable
private fun CalculatorKeypadPreview() {
    HisabKitabTheme {
        Surface {
            CalculatorKeypad(
                onDigit = {},
                onDecimalPoint = {},
                onOperator = {},
                onBackspace = {},
                onClear = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
