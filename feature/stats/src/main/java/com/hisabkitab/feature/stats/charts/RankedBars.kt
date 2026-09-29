package com.hisabkitab.feature.stats.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hisabkitab.core.designsystem.component.CategoryIconBadge
import com.hisabkitab.core.model.CategoryTotal
import com.hisabkitab.core.ui.LocalMoneyFormatter
import com.hisabkitab.core.ui.displayName
import kotlin.math.roundToInt

/** Leaderboard of categories: bars grow in one after another, longest first. */
@Composable
fun RankedBars(totals: List<CategoryTotal>, modifier: Modifier = Modifier) {
    val formatter = LocalMoneyFormatter.current
    val max = totals.maxOfOrNull { it.totalMinor }?.coerceAtLeast(1) ?: 1
    val sum = totals.sumOf { it.totalMinor }.coerceAtLeast(1)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        totals.forEachIndexed { index, total ->
            val growth = remember(total.category.id) { Animatable(0f) }
            LaunchedEffect(total.totalMinor, max) {
                growth.animateTo(
                    total.totalMinor.toFloat() / max,
                    tween(durationMillis = 700, delayMillis = index.coerceAtMost(8) * 70, easing = FastOutSlowInEasing),
                )
            }
            val color = Color(total.category.color)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(22.dp),
                )
                CategoryIconBadge(total.category.iconKey, total.category.color, size = 38.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            total.category.displayName(),
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            formatter.format(total.totalMinor),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .weight(1f)
                                .height(10.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth(growth.value.coerceIn(0.02f, 1f))
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(Brush.horizontalGradient(listOf(lerp(color, Color.White, 0.35f), color))),
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "${(total.totalMinor * 100f / sum).roundToInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(36.dp),
                        )
                    }
                }
            }
        }
    }
}
