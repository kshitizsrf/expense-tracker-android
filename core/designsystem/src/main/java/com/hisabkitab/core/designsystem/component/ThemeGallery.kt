package com.hisabkitab.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hisabkitab.core.designsystem.theme.HisabKitabTheme
import com.hisabkitab.core.designsystem.theme.ThemeSpec
import com.hisabkitab.core.designsystem.theme.displayName
import com.hisabkitab.core.designsystem.theme.supportsDynamicColor
import com.hisabkitab.core.designsystem.theme.tagline
import com.hisabkitab.core.designsystem.theme.toThemeSpec
import com.hisabkitab.core.designsystem.theme.variants
import com.hisabkitab.core.model.AppTheme

/** Themes available on this device (Wallpaper needs Android 12+). */
fun availableThemes(): List<AppTheme> =
    AppTheme.entries.filter { it != AppTheme.WALLPAPER || supportsDynamicColor() }

/** Horizontally scrolling gallery of theme preview cards. */
@Composable
fun ThemeGallery(
    selected: AppTheme,
    onSelect: (AppTheme) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
) {
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(availableThemes()) { theme ->
            ThemePreviewCard(theme = theme, selected = theme == selected, onClick = { onSelect(theme) })
        }
    }
}

/** A miniature of the app in [theme]: its gradient, aurora glow and a glass card. */
@Composable
fun ThemePreviewCard(theme: AppTheme, selected: Boolean, onClick: () -> Unit) {
    val dark = HisabKitabTheme.colors.isDark
    val spec = rememberThemeSpec(theme, dark)
    val scale by animateFloatAsState(if (selected) 1f else 0.94f, spring(dampingRatio = 0.6f), label = "themeScale")
    val shape = RoundedCornerShape(28.dp)

    Column(
        modifier = Modifier
            .width(128.dp)
            .scale(scale)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp)
                .clip(shape)
                .background(Brush.linearGradient(listOf(spec.deep, lerp(spec.deep, spec.glow, 0.55f), spec.glow), start = Offset(200f, 0f), end = Offset(0f, 500f)))
                .background(Brush.radialGradient(listOf(spec.aurora.first().copy(alpha = 0.55f), Color.Transparent), center = Offset(60f, 120f), radius = 260f))
                .border(if (selected) 3.dp else 1.dp, HisabKitabTheme.colors.onBackdrop.copy(alpha = if (selected) 1f else 0.3f), shape),
        ) {
            // Fake balance + glass card, so the theme is judged in context.
            Column(Modifier.padding(14.dp)) {
                Box(Modifier.size(width = 36.dp, height = 6.dp).clip(CircleShape).background(spec.onBackdrop.copy(alpha = 0.6f)))
                Spacer(Modifier.height(8.dp))
                Box(Modifier.size(width = 72.dp, height = 12.dp).clip(CircleShape).background(spec.onBackdrop))
                Spacer(Modifier.weight(1f))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(lerp(spec.card, Color.White, if (dark) 0f else 0.5f).copy(alpha = if (dark) 0.7f else 0.85f))
                        .padding(10.dp),
                ) {
                    Box(Modifier.size(width = 48.dp, height = 6.dp).clip(CircleShape).background(spec.ink.copy(alpha = 0.35f)))
                    Spacer(Modifier.height(6.dp))
                    Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(spec.accent))
                    Spacer(Modifier.height(6.dp))
                    Box(Modifier.size(width = 64.dp, height = 6.dp).clip(CircleShape).background(spec.accentSoft))
                }
            }
            if (selected) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = spec.deep, modifier = Modifier.size(16.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(theme.displayName),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = HisabKitabTheme.colors.onBackdrop,
        )
        Text(
            stringResource(theme.tagline),
            style = MaterialTheme.typography.labelSmall,
            color = HisabKitabTheme.colors.onBackdropMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun rememberThemeSpec(theme: AppTheme, dark: Boolean): ThemeSpec {
    val context = LocalContext.current
    return remember(theme, dark) {
        if (theme == AppTheme.WALLPAPER && supportsDynamicColor()) {
            (if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)).toThemeSpec(dark)
        } else {
            theme.variants().let { if (dark) it.dark else it.light }
        }
    }
}
