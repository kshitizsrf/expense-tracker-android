package com.hisabkitab.core.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.hisabkitab.core.designsystem.theme.LocalReducedMotion

/**
 * Money that counts up/down to its new value, like a ticker. Shrinks to fit on one line.
 * [format] turns the current minor-unit value into text (e.g. signed or plain).
 */
@Composable
fun AnimatedMoneyText(
    amountMinor: Long,
    style: TextStyle,
    modifier: Modifier = Modifier,
    minFontSize: TextUnit = 14.sp,
    format: (Long) -> String = LocalMoneyFormatter.current::format,
) {
    var from by remember { mutableLongStateOf(amountMinor) }
    val progress = remember { Animatable(1f) }
    val reducedMotion = LocalReducedMotion.current

    LaunchedEffect(amountMinor) {
        if (reducedMotion) {
            from = amountMinor
            return@LaunchedEffect
        }
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = 700, easing = FastOutSlowInEasing))
        from = amountMinor
    }

    val shown = from + ((amountMinor - from) * progress.value).toLong()
    BasicText(
        text = format(shown),
        style = style,
        maxLines = 1,
        autoSize = TextAutoSize.StepBased(minFontSize = minFontSize, maxFontSize = style.fontSize),
        modifier = modifier,
    )
}
