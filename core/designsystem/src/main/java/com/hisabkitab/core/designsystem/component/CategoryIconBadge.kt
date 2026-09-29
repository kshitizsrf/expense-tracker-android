package com.hisabkitab.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hisabkitab.core.icons.CategoryIcons

/**
 * Round badge showing a category icon in the category's color.
 * When [filled] the badge is solid and the icon white (used for selection states).
 */
@Composable
fun CategoryIconBadge(
    iconKey: String,
    color: Int,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    filled: Boolean = false,
    contentDescription: String? = null,
) {
    val tint = Color(color)
    Box(
        modifier = modifier
            .size(size)
            .background(
                brush = if (filled) {
                    Brush.linearGradient(listOf(lerp(tint, Color.White, 0.18f), lerp(tint, Color.Black, 0.18f)))
                } else {
                    Brush.linearGradient(listOf(tint.copy(alpha = 0.22f), tint.copy(alpha = 0.10f)))
                },
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(CategoryIcons.resolve(iconKey)),
            contentDescription = contentDescription,
            tint = if (filled) Color.White else tint,
            modifier = Modifier.size(size * 0.5f),
        )
    }
}
