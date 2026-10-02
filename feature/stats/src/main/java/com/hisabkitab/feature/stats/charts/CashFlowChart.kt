package com.hisabkitab.feature.stats.charts

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp

data class CashFlowBar(val label: String, val incomeMinor: Long, val expenseMinor: Long)

/**
 * Income vs expense side by side for each month. Tap a month to highlight it; the caller shows
 * the details for the [selectedIndex]. When the months do not fit, the chart scrolls sideways
 * and opens on the latest month.
 */
@Composable
fun CashFlowChart(
    bars: List<CashFlowBar>,
    incomeColor: Color,
    expenseColor: Color,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val growth = remember { Animatable(0f) }
    LaunchedEffect(bars) {
        growth.snapTo(0f)
        growth.animateTo(1f, tween(durationMillis = 900, easing = FastOutSlowInEasing))
    }
    val measurer = rememberTextMeasurer()
    val scheme = MaterialTheme.colorScheme
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = scheme.onSurfaceVariant)
    val selectedLabelStyle = labelStyle.copy(color = scheme.onSurface, fontWeight = FontWeight.Bold)
    val highlight = scheme.primary.copy(alpha = 0.10f)
    val scroll = rememberScrollState()
    LaunchedEffect(bars.size, scroll.maxValue) { scroll.scrollTo(scroll.maxValue) }

    BoxWithConstraints(modifier.semantics { this.contentDescription = contentDescription }) {
    val width = maxOf(maxWidth, MIN_GROUP_WIDTH * bars.size)
    Canvas(
        modifier = Modifier
            .horizontalScroll(scroll)
            .width(width)
            .fillMaxHeight()
            .pointerInput(bars) {
                detectTapGestures { tap ->
                    if (bars.isNotEmpty()) onSelect((tap.x / (size.width / bars.size)).toInt().coerceIn(0, bars.lastIndex))
                }
            },
    ) {
        if (bars.isEmpty()) return@Canvas
        val axis = 22.dp.toPx()
        val chartHeight = size.height - axis - 8.dp.toPx()
        val groupWidth = size.width / bars.size
        val barWidth = (groupWidth * 0.26f).coerceAtMost(18.dp.toPx())
        val max = bars.maxOf { maxOf(it.incomeMinor, it.expenseMinor) }.coerceAtLeast(1L).toFloat()
        val radius = CornerRadius(barWidth / 2, barWidth / 2)

        bars.forEachIndexed { index, bar ->
            val left = index * groupWidth
            if (index == selectedIndex) {
                drawRoundRect(highlight, Offset(left + 4.dp.toPx(), 0f), Size(groupWidth - 8.dp.toPx(), size.height), CornerRadius(14.dp.toPx()))
            }
            val center = left + groupWidth / 2
            listOf(
                Triple(bar.incomeMinor, incomeColor, center - barWidth - 2.dp.toPx()),
                Triple(bar.expenseMinor, expenseColor, center + 2.dp.toPx()),
            ).forEach { (value, color, x) ->
                val height = (chartHeight * (value / max) * growth.value).coerceAtLeast(if (value > 0) barWidth else 3.dp.toPx())
                val top = 8.dp.toPx() + chartHeight - height
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(color, lerp(color, Color.White, 0.35f)), startY = top, endY = top + height),
                    topLeft = Offset(x, top),
                    size = Size(barWidth, height),
                    cornerRadius = radius,
                    alpha = if (value > 0) 1f else 0.3f,
                )
            }
            val text = measurer.measure(bar.label, if (index == selectedIndex) selectedLabelStyle else labelStyle)
            drawText(text, topLeft = Offset(center - text.size.width / 2f, size.height - text.size.height))
        }
    }
    }
}

/** Narrowest a month may get before the chart starts scrolling. */
private val MIN_GROUP_WIDTH = 52.dp
