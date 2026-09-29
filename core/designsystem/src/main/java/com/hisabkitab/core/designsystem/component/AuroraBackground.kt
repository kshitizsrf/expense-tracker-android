package com.hisabkitab.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.hisabkitab.core.designsystem.theme.HisabKitabTheme
import com.hisabkitab.core.designsystem.theme.LocalReducedMotion
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The theme's living backdrop: a diagonal gradient with soft "aurora" light blobs drifting
 * slowly across it. Animation runs in the draw phase only (no recomposition) and stops when
 * the user has disabled animations.
 */
@Composable
fun AuroraBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val colors = HisabKitabTheme.colors
    val phase: State<Float> = if (LocalReducedMotion.current) {
        remember { mutableFloatStateOf(0.15f) }
    } else {
        rememberInfiniteTransition(label = "aurora").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(durationMillis = 24_000, easing = LinearEasing), RepeatMode.Restart),
            label = "auroraPhase",
        )
    }

    Box(
        modifier = modifier.drawBehind {
            drawRect(
                brush = Brush.linearGradient(
                    colors = colors.backdrop,
                    start = Offset(size.width * 0.8f, 0f),
                    end = Offset(size.width * 0.2f, size.height),
                ),
            )
            drawAurora(colors.aurora, phase.value, alpha = if (colors.isDark) 0.35f else 0.45f)
        },
        content = content,
    )
}

private fun DrawScope.drawAurora(blobs: List<Color>, phase: Float, alpha: Float) {
    val base = size.minDimension
    blobs.forEachIndexed { index, color ->
        val angle = (phase + index / blobs.size.toFloat()) * 2f * PI.toFloat()
        // Each blob follows its own slow Lissajous path.
        val center = Offset(
            x = size.width * (0.5f + 0.38f * cos(angle + index)),
            y = size.height * (0.28f + 0.22f * sin(angle * (1 + index % 2) + index * 1.7f)),
        )
        val radius = base * (0.55f + 0.12f * sin(angle * 2 + index))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = alpha), Color.Transparent),
                center = center,
                radius = radius,
            ),
            radius = radius,
            center = center,
        )
    }
}
