package com.hisabkitab.feature.stats.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.hisabkitab.core.designsystem.component.smoothPath
import kotlin.math.round

/**
 * Smooth area chart of amounts over time. Touch and drag to scrub: a guide line and a tooltip
 * follow the finger and show the exact value for that day (or month).
 */
@Composable
fun TrendChart(
    points: List<Pair<String, Long>>,
    color: Color,
    formatValue: (Long) -> String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(points) {
        reveal.snapTo(0f)
        reveal.animateTo(1f, tween(durationMillis = 1_000, easing = FastOutSlowInEasing))
    }
    var selected by remember(points) { mutableStateOf<Int?>(null) }
    val measurer = rememberTextMeasurer()
    val scheme = MaterialTheme.colorScheme
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = scheme.onSurfaceVariant)
    val tooltipStyle = MaterialTheme.typography.labelLarge.copy(color = scheme.inverseOnSurface, fontWeight = FontWeight.Bold)
    val tooltipCaption = MaterialTheme.typography.labelSmall.copy(color = scheme.inverseOnSurface.copy(alpha = 0.8f))
    val grid = scheme.outlineVariant

    fun indexAt(x: Float, width: Float): Int {
        if (points.size < 2) return 0
        val step = width / (points.size - 1)
        return round(x / step).toInt().coerceIn(0, points.lastIndex)
    }

    Canvas(
        modifier = modifier
            .semantics { this.contentDescription = contentDescription }
            .pointerInput(points) {
                detectTapGestures { selected = indexAt(it.x, size.width.toFloat()) }
            }
            .pointerInput(points) {
                detectDragGestures(
                    onDragStart = { selected = indexAt(it.x, size.width.toFloat()) },
                    onDrag = { change, _ -> selected = indexAt(change.position.x, size.width.toFloat()) },
                )
            },
    ) {
        if (points.isEmpty()) return@Canvas
        val axisHeight = 22.dp.toPx()
        val top = 34.dp.toPx() // room for the tooltip
        val chartHeight = size.height - axisHeight - top
        val max = points.maxOf { it.second }.coerceAtLeast(1L).toFloat()

        // Dashed guide lines at 0%, 50% and 100% of the maximum.
        val dash = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 6.dp.toPx()))
        listOf(0f, 0.5f, 1f).forEach { f ->
            val y = top + chartHeight * (1 - f)
            drawLine(grid, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx(), pathEffect = dash)
        }
        val maxLabel = measurer.measure(formatValue(max.toLong()), labelStyle)
        drawText(maxLabel, topLeft = Offset(0f, top - maxLabel.size.height - 2.dp.toPx()))

        val step = if (points.size > 1) size.width / (points.size - 1) else 0f
        val offsets = points.mapIndexed { i, (_, v) ->
            Offset(if (points.size > 1) i * step else size.width / 2, top + chartHeight * (1 - v / max))
        }
        val line = smoothPath(offsets)
        val area = Path().apply {
            addPath(line)
            lineTo(offsets.last().x, top + chartHeight)
            lineTo(offsets.first().x, top + chartHeight)
            close()
        }
        clipRect(right = size.width * reveal.value) {
            drawPath(area, Brush.verticalGradient(listOf(color.copy(alpha = 0.35f), color.copy(alpha = 0.02f)), startY = top, endY = top + chartHeight))
            drawPath(line, color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        }

        // X-axis labels: first, middle and last.
        listOf(0, points.lastIndex / 2, points.lastIndex).distinct().forEach { i ->
            val text = measurer.measure(points[i].first, labelStyle)
            val x = (offsets[i].x - text.size.width / 2f).coerceIn(0f, size.width - text.size.width)
            drawText(text, topLeft = Offset(x, size.height - text.size.height))
        }

        selected?.let { i ->
            val point = offsets[i]
            drawLine(color.copy(alpha = 0.5f), Offset(point.x, top), Offset(point.x, top + chartHeight), strokeWidth = 1.5.dp.toPx())
            drawCircle(Color.White, radius = 7.dp.toPx(), center = point)
            drawCircle(color, radius = 4.5.dp.toPx(), center = point)

            val value = measurer.measure(formatValue(points[i].second), tooltipStyle)
            val caption = measurer.measure(points[i].first, tooltipCaption)
            val padding = 8.dp.toPx()
            val boxWidth = maxOf(value.size.width, caption.size.width) + padding * 2
            val boxHeight = value.size.height + caption.size.height + padding * 1.5f
            val boxX = (point.x - boxWidth / 2).coerceIn(0f, size.width - boxWidth)
            val boxY = (point.y - boxHeight - 12.dp.toPx()).coerceAtLeast(0f)
            drawRoundRect(scheme.inverseSurface, Offset(boxX, boxY), Size(boxWidth, boxHeight), CornerRadius(10.dp.toPx()))
            drawText(value, topLeft = Offset(boxX + padding, boxY + padding * 0.6f))
            drawText(caption, topLeft = Offset(boxX + padding, boxY + padding * 0.6f + value.size.height))
        }
    }
}
