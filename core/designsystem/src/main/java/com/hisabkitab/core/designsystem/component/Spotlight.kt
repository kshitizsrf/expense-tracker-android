package com.hisabkitab.core.designsystem.component

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisabkitab.core.designsystem.R
import com.hisabkitab.core.designsystem.theme.HisabKitabTheme

/** What a spotlight shows: one big figure plus a handful of supporting details. */
@Immutable
data class SpotlightItem(
    val title: String,
    val value: String,
    val caption: String? = null,
    val icon: ImageVector? = null,
    val valueColor: Color? = null,
    val details: List<Pair<String, String>> = emptyList(),
)

@Stable
class SpotlightController {
    var current: SpotlightItem? by mutableStateOf(null)
        private set

    fun show(item: SpotlightItem) {
        current = item
    }

    fun dismiss() {
        current = null
    }
}

val LocalSpotlight = staticCompositionLocalOf { SpotlightController() }

/**
 * Hosts spotlights for everything inside it. While one is open, the content behind is blurred
 * (Android 12+) and dimmed so only the focused figure competes for attention.
 */
@Composable
fun SpotlightHost(content: @Composable () -> Unit) {
    val controller = remember { SpotlightController() }
    val item = controller.current
    val blur by animateDpAsState(if (item != null) 16.dp else 0.dp, label = "spotlightBlur")

    CompositionLocalProvider(LocalSpotlight provides controller) {
        Box(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().blur(blur)) { content() }

            AnimatedVisibility(visible = item != null, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                            controller.dismiss()
                        },
                )
            }
            AnimatedVisibility(
                visible = item != null,
                modifier = Modifier.align(Alignment.Center),
                enter = scaleIn(spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow), initialScale = 0.8f) + fadeIn(),
                exit = scaleOut(targetScale = 0.9f) + fadeOut(),
            ) {
                // Keep showing the last item while the exit animation runs.
                val shown = remember { mutableStateOf(item) }
                if (item != null) shown.value = item
                shown.value?.let { SpotlightCard(it, onDismiss = controller::dismiss) }
            }
        }
        BackHandler(enabled = item != null) { controller.dismiss() }
    }
}

@Composable
private fun SpotlightCard(item: SpotlightItem, onDismiss: () -> Unit) {
    val colors = HisabKitabTheme.colors
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
            .glass(RoundedCornerShape(36.dp), strong = true)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (item.icon != null) {
            Box(
                Modifier.size(64.dp).background(Brush.linearGradient(colors.accentGradient), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(item.icon, contentDescription = null, tint = scheme.onPrimary, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.height(16.dp))
        }
        Text(item.title, style = MaterialTheme.typography.titleMedium, color = scheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        BasicText(
            text = item.value,
            style = MaterialTheme.typography.displayLarge.copy(
                color = item.valueColor ?: scheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 28.sp, maxFontSize = 64.sp),
            modifier = Modifier.fillMaxWidth(),
        )
        if (item.caption != null) {
            Spacer(Modifier.height(4.dp))
            Text(item.caption, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
        if (item.details.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            item.details.forEachIndexed { index, (label, value) ->
                if (index > 0) HorizontalDivider(color = scheme.outlineVariant)
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(label, style = MaterialTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
                    Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.spotlight_dismiss), style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
    }
}

/** Makes this element open [item] in the spotlight when tapped. */
@Composable
fun Modifier.spotlightOnClick(item: () -> SpotlightItem): Modifier {
    val controller = LocalSpotlight.current
    val view = LocalView.current
    return this.clickable {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        controller.show(item())
    }
}
