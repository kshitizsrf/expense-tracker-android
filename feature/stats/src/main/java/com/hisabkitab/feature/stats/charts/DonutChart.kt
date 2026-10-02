package com.hisabkitab.feature.stats.charts

import androidx.compose.runtime.key
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.hypot

@Immutable
data class DonutSlice(val value: Float, val color: Color)

/**
 * Animated, tappable donut. Tapping a slice selects it (it grows while the others fade);
 * tapping it again or the hole clears the selection. [content] is centered in the ring.
 */
@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    selectedIndex: Int?,
    onSelect: (Int?) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    thickness: Dp = 30.dp,
    gapDegrees: Float = 2.5f,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val sweep = remember { Animatable(0f) }
    LaunchedEffect(slices) {
        sweep.snapTo(0f)
        sweep.animateTo(1f, tween(durationMillis = 1_000, easing = FastOutSlowInEasing))
    }
    val smooth = spring<Float>(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
    val emphasis by animateFloatAsState(if (selectedIndex != null) 1f else 0f, smooth, label = "emphasis")
    // One animation per slice: the old selection eases back while the new one grows.
    val grow = slices.indices.map { index ->
        key(index) { animateFloatAsState(if (index == selectedIndex) 1f else 0f, smooth, label = "slice$index") }
    }
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val total = slices.sumOf { it.value.toDouble() }.toFloat()

    Box(
        modifier = modifier
            .semantics { this.contentDescription = contentDescription }
            .pointerInput(slices, selectedIndex) {
                detectTapGestures { tap ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val outer = size.width.coerceAtMost(size.height) / 2f
                    val distance = hypot(tap.x - center.x, tap.y - center.y)
                    if (distance < outer - thickness.toPx() * 1.8f || distance > outer + 12.dp.toPx() || total <= 0f) {
                        onSelect(null)
                        return@detectTapGestures
                    }
                    val degrees = (Math.toDegrees(atan2((tap.y - center.y).toDouble(), (tap.x - center.x).toDouble())) + 450) % 360
                    var start = 0.0
                    val hit = slices.indexOfFirst { slice ->
                        val end = start + 360.0 * slice.value / total
                        (degrees in start..end).also { start = end }
                    }
                    onSelect(if (hit == selectedIndex || hit < 0) null else hit)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val baseStroke = thickness.toPx()
            val growth = 10.dp.toPx()
            val diameter = size.minDimension - baseStroke - growth
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)
            drawArc(track, 0f, 360f, false, topLeft, arcSize, style = Stroke(baseStroke))
            if (total <= 0f) return@Canvas

            val gap = if (slices.size > 1) gapDegrees else 0f
            var start = -90f
            slices.forEachIndexed { index, slice ->
                val full = 360f * slice.value / total * sweep.value
                val visible = (full - gap).coerceAtLeast(0.6f)
                val isSelected = index == selectedIndex
                val sliceGrowth = grow[index].value
                val alpha = if (isSelected) 1f else 1f - 0.6f * emphasis * (1f - sliceGrowth)
                drawArc(
                    color = slice.color.copy(alpha = alpha),
                    startAngle = start + gap / 2f,
                    sweepAngle = visible,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(
                        width = baseStroke + growth * sliceGrowth,
                        cap = if (slices.size == 1) StrokeCap.Butt else StrokeCap.Round,
                    ),
                )
                start += full
            }
        }
        content()
    }
}
