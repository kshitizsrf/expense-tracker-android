package com.hisabkitab.core.designsystem.component

import androidx.compose.ui.unit.IntOffset
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hisabkitab.core.designsystem.theme.HisabKitabTheme

/**
 * Frosted-glass look: a translucent tint of the theme's card color with a light-catching
 * top edge. Real backdrop blur is not available on every Android version, so the frosting
 * comes from high tint opacity over the soft aurora backdrop.
 */
@Composable
fun Modifier.glass(
    shape: Shape = MaterialTheme.shapes.large,
    strong: Boolean = false,
): Modifier {
    val colors = HisabKitabTheme.colors
    return this
        .clip(shape)
        .background(if (strong) colors.glassStrong else colors.glass, shape)
        .border(
            border = BorderStroke(
                width = 1.dp,
                brush = Brush.verticalGradient(listOf(colors.glassEdge, colors.glassEdge.copy(alpha = 0.05f))),
            ),
            shape = shape,
        )
}

/** A glass card. Content uses the regular on-surface colors. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    strong: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "pressScale",
    )
    Column(
        modifier = modifier
            .scale(scale)
            .glass(shape, strong)
            .then(
                if (onClick != null) {
                    Modifier.clickable(interactionSource = interaction, indication = ripple(), onClick = onClick)
                } else {
                    Modifier
                },
            )
            .padding(contentPadding),
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            content()
        }
    }
}

/** Round glass button for toolbar actions on the backdrop. */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = if (HisabKitabTheme.colors.isDark) 0.1f else 0.22f))
            .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = HisabKitabTheme.colors.onBackdrop)
    }
}

/** A pill-shaped chip for filters. [onBackdrop] chips are translucent white on the gradient. */
@Composable
fun GlassChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onBackdrop: Boolean = true,
) {
    val colors = HisabKitabTheme.colors
    val scheme = MaterialTheme.colorScheme
    val container by animateColorAsState(
        when {
            selected -> if (onBackdrop) Color.White.copy(alpha = if (colors.isDark) 0.9f else 0.95f) else scheme.primary
            onBackdrop -> Color.White.copy(alpha = if (colors.isDark) 0.1f else 0.2f)
            else -> scheme.surfaceContainerHighest
        },
        label = "chipContainer",
    )
    val content by animateColorAsState(
        when {
            selected -> if (onBackdrop) colors.backdrop.first() else scheme.onPrimary
            onBackdrop -> colors.onBackdrop
            else -> scheme.onSurfaceVariant
        },
        label = "chipContent",
    )
    Row(
        modifier = modifier
            .height(38.dp)
            .clip(CircleShape)
            .background(container)
            .then(if (onBackdrop && !selected) Modifier.border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape) else Modifier)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
        Text(label, color = content, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

/**
 * Segmented switch with a sliding thumb (e.g. Expense / Income). Works on the backdrop or,
 * with [onBackdrop] = false, inside a glass card.
 */
@Composable
fun <T> SlidingSegmentedControl(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    onBackdrop: Boolean = true,
) {
    val colors = HisabKitabTheme.colors
    val scheme = MaterialTheme.colorScheme
    val selectedIndex = options.indexOfFirst { it.first == selected }.coerceAtLeast(0)
    val track = if (onBackdrop) Color.White.copy(alpha = if (colors.isDark) 0.1f else 0.2f) else scheme.surfaceContainerHighest
    val thumb = if (onBackdrop) Color.White else scheme.primary
    val selectedText = if (onBackdrop) colors.backdrop.first() else scheme.onPrimary
    val unselectedText = if (onBackdrop) colors.onBackdrop else scheme.onSurfaceVariant

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(CircleShape)
            .background(track)
            .padding(4.dp),
    ) {
        val segmentWidth = maxWidth / options.size
        val offset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
            label = "thumb",
        )
        Box(
            Modifier
                .offset { IntOffset(offset.roundToPx(), 0) }
                .width(segmentWidth)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(thumb),
        )
        Row(Modifier.fillMaxWidth().fillMaxHeight()) {
            options.forEachIndexed { index, (value, label) ->
                val isSelected = index == selectedIndex
                val textColor by animateColorAsState(if (isSelected) selectedText else unselectedText, label = "segText")
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .selectable(selected = isSelected, role = Role.Tab) { onSelect(value) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        color = textColor,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** Primary call-to-action with the theme's accent gradient. */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    val colors = HisabKitabTheme.colors
    val onAccent = MaterialTheme.colorScheme.onPrimary
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(CircleShape)
            .background(
                if (enabled) Brush.horizontalGradient(colors.accentGradient)
                else Brush.horizontalGradient(listOf(Color.Gray.copy(alpha = 0.35f), Color.Gray.copy(alpha = 0.35f))),
            )
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val content = if (enabled) onAccent else onAccent.copy(alpha = 0.6f)
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = content)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, color = content, style = MaterialTheme.typography.titleMedium)
    }
}

/** Title + optional action, for sections inside glass cards. */
@Composable
fun CardHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        action?.invoke(this)
    }
}

/** Large screen title drawn on the backdrop, with optional trailing actions. */
@Composable
fun BackdropHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = HisabKitabTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        navigationIcon?.invoke()
        Column(Modifier.weight(1f)) {
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.labelLarge, color = colors.onBackdropMuted)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = colors.onBackdrop,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = actions)
    }
}
