package com.hisabkitab.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.roundToInt
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp

/**
 * Minimal smooth line chart used for at-a-glance trends. Draws in from left to right.
 *
 * When [onSelect] is set the chart can be scrubbed like a stock chart: touching and dragging
 * reports the nearest point's index (null on release), and [selectedIndex] is drawn with a
 * guide line and halo while the rest of the line beyond it dims.
 */
@Composable
fun Sparkline(
    values: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
    fillAlpha: Float = 0.28f,
    selectedIndex: Int? = null,
    onSelect: ((Int?) -> Unit)? = null,
) {
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(values) {
        reveal.snapTo(0f)
        reveal.animateTo(1f, tween(durationMillis = 900, easing = FastOutSlowInEasing))
    }
    val currentOnSelect by rememberUpdatedState(onSelect)
    val scrub = if (onSelect == null || values.size < 2) {
        Modifier
    } else {
        Modifier.pointerInput(values.size) {
            fun indexAt(x: Float) = (x / size.width * (values.size - 1)).roundToInt().coerceIn(0, values.size - 1)
            awaitEachGesture {
                // Consuming every change keeps the list from scrolling and the hero from swiping months.
                val down = awaitFirstDown()
                down.consume()
                currentOnSelect?.invoke(indexAt(down.position.x))
                while (true) {
                    val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) break
                    change.consume()
                    currentOnSelect?.invoke(indexAt(change.position.x))
                }
                currentOnSelect?.invoke(null)
            }
        }
    }
    Canvas(modifier.then(scrub)) {
        if (values.size < 2) return@Canvas
        val max = values.max().coerceAtLeast(1f)
        val stepX = size.width / (values.size - 1)
        val points = values.mapIndexed { i, v ->
            Offset(i * stepX, size.height - (v / max) * size.height * 0.9f - size.height * 0.05f)
        }
        val line = smoothPath(points)
        val fill = Path().apply {
            addPath(line)
            lineTo(points.last().x, size.height)
            lineTo(points.first().x, size.height)
            close()
        }
        val stroke = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val selected = selectedIndex?.takeIf { it in points.indices }?.let(points::get)
        if (selected == null) {
            clipRect(right = size.width * reveal.value) {
                drawPath(fill, Brush.verticalGradient(listOf(color.copy(alpha = fillAlpha), Color.Transparent)))
                drawPath(line, color, style = stroke)
                drawCircle(color, radius = 3.5.dp.toPx(), center = points.last())
            }
        } else {
            clipRect(right = selected.x) {
                drawPath(fill, Brush.verticalGradient(listOf(color.copy(alpha = fillAlpha), Color.Transparent)))
                drawPath(line, color, style = stroke)
            }
            clipRect(left = selected.x) {
                drawPath(line, color.copy(alpha = 0.3f), style = stroke)
            }
            drawLine(
                color = color.copy(alpha = 0.6f),
                start = Offset(selected.x, 0f),
                end = Offset(selected.x, size.height),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())),
            )
            drawCircle(color.copy(alpha = 0.25f), radius = 10.dp.toPx(), center = selected)
            drawCircle(color, radius = 5.dp.toPx(), center = selected)
        }
    }
}

/** Catmull-Rom style smoothing via cubic Béziers through every point. */
fun smoothPath(points: List<Offset>): Path = Path().apply {
    if (points.isEmpty()) return@apply
    moveTo(points[0].x, points[0].y)
    for (i in 1 until points.size) {
        val p0 = points.getOrElse(i - 2) { points[i - 1] }
        val p1 = points[i - 1]
        val p2 = points[i]
        val p3 = points.getOrElse(i + 1) { p2 }
        val c1 = Offset(p1.x + (p2.x - p0.x) / 6f, p1.y + (p2.y - p0.y) / 6f)
        val c2 = Offset(p2.x - (p3.x - p1.x) / 6f, p2.y - (p3.y - p1.y) / 6f)
        cubicTo(c1.x, c1.y, c2.x, c2.y, p2.x, p2.y)
    }
}
