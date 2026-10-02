package com.hisabkitab.feature.transactions.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hisabkitab.core.common.calculator.AmountInput

/**
 * Draws +, −, × and ÷ geometrically. Font glyphs for these sit at different heights and
 * side bearings, so they never look centered in a button; drawn strokes are exactly centered.
 */
@Composable
internal fun OperatorGlyph(operator: Char, color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier.size(size)) {
        val stroke = this.size.minDimension * 0.12f
        val c = center
        val half = this.size.minDimension / 2f
        fun horizontal(length: Float = half) =
            drawLine(color, Offset(c.x - length, c.y), Offset(c.x + length, c.y), stroke, StrokeCap.Round)
        fun vertical() = drawLine(color, Offset(c.x, c.y - half), Offset(c.x, c.y + half), stroke, StrokeCap.Round)

        when (operator) {
            AmountInput.PLUS -> {
                horizontal()
                vertical()
            }
            AmountInput.MINUS -> horizontal()
            AmountInput.TIMES -> rotate(45f) {
                val arm = half * 0.92f
                drawLine(color, Offset(c.x - arm, c.y), Offset(c.x + arm, c.y), stroke, StrokeCap.Round)
                drawLine(color, Offset(c.x, c.y - arm), Offset(c.x, c.y + arm), stroke, StrokeCap.Round)
            }
            AmountInput.DIVIDE -> {
                horizontal()
                val dot = stroke * 0.85f
                drawCircle(color, dot, Offset(c.x, c.y - half * 0.62f))
                drawCircle(color, dot, Offset(c.x, c.y + half * 0.62f))
            }
        }
    }
}
