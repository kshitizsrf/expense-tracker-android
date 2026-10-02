package com.hisabkitab.core.designsystem.component

import android.os.Build
import androidx.compose.ui.unit.IntOffset
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.draw.drawBehind
import com.hisabkitab.core.model.NavBarStyle
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hisabkitab.core.designsystem.theme.HisabKitabTheme

data class LiquidNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

/** Height of the bar itself plus its bottom margin (the system bar inset is added on top). */
val LiquidNavigationBarHeight: Dp = 72.dp + 16.dp

/**
 * Floating "liquid glass" navigation: a translucent capsule hovering above the content.
 * The selection pill stretches toward its destination before its tail catches up (the
 * leading edge uses a stiffer spring), and a raised accent button sits in the middle for the
 * app's primary action. Items are split evenly on either side of the center button.
 */
@Composable
fun LiquidNavigationBar(
    items: List<LiquidNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    centerIcon: ImageVector,
    centerContentDescription: String,
    onCenterClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: NavBarStyle = NavBarStyle.GLOW,
    applySystemInsets: Boolean = true,
) {
    val colors = HisabKitabTheme.colors
    val scheme = MaterialTheme.colorScheme
    val view = LocalView.current
    val slots = items.size + 1 // one extra slot for the center button
    val centerSlot = items.size / 2
    fun slotOf(itemIndex: Int) = if (itemIndex >= centerSlot) itemIndex + 1 else itemIndex

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(if (applySystemInsets) Modifier.windowInsetsPadding(WindowInsets.navigationBars) else Modifier)
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .selectableGroup(),
        ) {
            // The glass is a separate layer so styles like Bubble can rise above the capsule.
            Box(
                Modifier
                    .matchParentSize()
                    .shadow(elevation = 18.dp, shape = CircleShape, ambientColor = colors.backdrop.first(), spotColor = colors.backdrop.first())
                    .glass(CircleShape, strong = true),
            )
            val slotWidth = maxWidth / slots
            val inset = 6.dp

            // Liquid indicator: edges animate separately so the pill stretches while moving.
            var previousSlot by remember { mutableIntStateOf(slotOf(selectedIndex.coerceAtLeast(0))) }
            val targetSlot = slotOf(selectedIndex.coerceAtLeast(0))
            val movingRight = targetSlot >= previousSlot
            val fast = spring<Dp>(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium)
            val slow = spring<Dp>(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)
            val left by animateDpAsState(slotWidth * targetSlot + inset, if (movingRight) slow else fast, label = "pillLeft")
            val right by animateDpAsState(slotWidth * (targetSlot + 1) - inset, if (movingRight) fast else slow, label = "pillRight")
            SideEffect { previousSlot = targetSlot }

            if (selectedIndex >= 0 && style == NavBarStyle.LIQUID) {
                Box(
                    Modifier
                        .offset { IntOffset(left.roundToPx(), 0) }
                        .padding(vertical = 8.dp)
                        .width((right - left).coerceAtLeast(0.dp))
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(scheme.primary.copy(alpha = if (colors.isDark) 0.22f else 0.14f)),
                )
            }

            Row(Modifier.fillMaxWidth().fillMaxHeight()) {
                for (slot in 0 until slots) {
                    if (slot == centerSlot) {
                        Spacer(Modifier.weight(1f))
                        continue
                    }
                    val itemIndex = if (slot > centerSlot) slot - 1 else slot
                    val item = items[itemIndex]
                    NavBarSlot(
                        item = item,
                        selected = itemIndex == selectedIndex,
                        style = style,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                            onSelect(itemIndex)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // Raised center action, overlapping the capsule's top edge.
        CenterActionButton(
            icon = centerIcon,
            contentDescription = centerContentDescription,
            onClick = {
                view.performHapticFeedback(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM
                    else HapticFeedbackConstants.VIRTUAL_KEY,
                )
                onCenterClick()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-26).dp),
        )
    }
}

@Composable
private fun NavBarSlot(
    item: LiquidNavItem,
    selected: Boolean,
    style: NavBarStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val colors = HisabKitabTheme.colors
    val tint by animateColorAsState(if (selected) scheme.primary else scheme.onSurfaceVariant, label = "navTint")
    val bounce = spring<Float>(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium)
    val iconScale by animateFloatAsState(if (selected) 1.12f else 1f, bounce, label = "navIconScale")

    // No clip here: the Bubble style's orb rises above the slot and must not be cut off.
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .semantics {
                this.selected = selected
                role = Role.Tab
                contentDescription = item.label
            },
        contentAlignment = Alignment.Center,
    ) {
        when (style) {
            NavBarStyle.LIQUID -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                NavIcon(item, selected, tint, Modifier.scale(iconScale))
                NavLabel(item.label, selected, tint)
            }

            NavBarStyle.GLOW -> {
                val glow by animateFloatAsState(if (selected) 1f else 0f, spring(), label = "glow")
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .drawBehind {
                                drawCircle(
                                    Brush.radialGradient(listOf(scheme.primary.copy(alpha = 0.35f * glow), Color.Transparent)),
                                    radius = size.minDimension * 0.75f,
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        NavIcon(item, selected, tint, Modifier.scale(iconScale))
                    }
                    Box(
                        Modifier
                            .size(width = (16 * glow).dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(scheme.primary.copy(alpha = glow)),
                    )
                }
            }

            NavBarStyle.BUBBLE -> {
                val lift by animateDpAsState(
                    targetValue = if (selected) (-22).dp else 0.dp,
                    animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
                    label = "lift",
                )
                val bubble by animateFloatAsState(if (selected) 1f else 0f, spring(dampingRatio = 0.6f), label = "bubble")
                Box(Modifier.fillMaxSize()) {
                    Box(
                        Modifier
                            .align(Alignment.Center)
                            .offset { IntOffset(0, lift.roundToPx()) }
                            .size(50.dp)
                            .scale(0.6f + 0.4f * bubble)
                            .shadow((8 * bubble).dp, CircleShape)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(colors.accentGradient.map { it.copy(alpha = bubble) }))
                            .border((3 * bubble).dp, Color.White.copy(alpha = bubble * if (colors.isDark) 0.2f else 0.9f), CircleShape),
                    )
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = null,
                        tint = if (selected) scheme.onPrimary else scheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center).offset { IntOffset(0, lift.roundToPx()) }.size(24.dp),
                    )
                    AnimatedVisibility(
                        visible = selected,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp),
                        enter = fadeIn() + slideInVertically { it },
                        exit = fadeOut(),
                    ) {
                        NavLabel(item.label, true, scheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun NavIcon(item: LiquidNavItem, selected: Boolean, tint: Color, modifier: Modifier) {
    Icon(
        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(24.dp),
    )
}

@Composable
private fun NavLabel(label: String, selected: Boolean, tint: Color) {
    Text(
        text = label,
        color = tint,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        maxLines = 1,
    )
}

@Composable
private fun CenterActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HisabKitabTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.9f else 1f, spring(dampingRatio = 0.5f), label = "centerScale")
    Box(
        modifier = modifier
            .scale(scale)
            .size(64.dp)
            .shadow(elevation = 14.dp, shape = CircleShape, spotColor = colors.accentGradient.first())
            .clip(CircleShape)
            .background(Brush.linearGradient(colors.accentGradient))
            .border(3.dp, Color.White.copy(alpha = if (colors.isDark) 0.18f else 0.85f), CircleShape)
            .clickable(interactionSource = interaction, indication = ripple(), role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(30.dp))
    }
}
