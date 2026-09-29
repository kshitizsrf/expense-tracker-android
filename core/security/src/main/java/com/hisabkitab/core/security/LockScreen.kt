package com.hisabkitab.core.security

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hisabkitab.core.designsystem.component.AuroraBackground
import com.hisabkitab.core.designsystem.component.GradientButton
import com.hisabkitab.core.designsystem.component.glass
import com.hisabkitab.core.designsystem.theme.HisabKitabTheme
import com.hisabkitab.core.designsystem.theme.LocalReducedMotion

/** Covers the whole app until the user authenticates. Prompts automatically when shown. */
@Composable
fun LockScreen(onUnlocked: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val colors = HisabKitabTheme.colors
    val title = stringResource(R.string.lock_prompt_title)
    val subtitle = stringResource(R.string.lock_prompt_subtitle)
    val prompt: () -> Unit = {
        context.findFragmentActivity()?.let { activity ->
            authenticate(activity, title, subtitle, onSuccess = onUnlocked)
        }
    }
    LaunchedEffect(Unit) { prompt() }

    val pulse = if (LocalReducedMotion.current) {
        1f
    } else {
        val transition = rememberInfiniteTransition(label = "lockPulse")
        val value by transition.animateFloat(1f, 1.08f, infiniteRepeatable(tween(1_200), RepeatMode.Reverse), label = "pulse")
        value
    }

    AuroraBackground(
        modifier
            .fillMaxSize()
            // Swallow touches so nothing underneath can be used while locked.
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
    ) {
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                Modifier.size(120.dp).scale(pulse).glass(CircleShape, strong = true),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(52.dp))
            }
            Spacer(Modifier.height(32.dp))
            Text(
                stringResource(R.string.lock_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onBackdrop,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.lock_message),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onBackdropMuted,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(40.dp))
            GradientButton(
                text = stringResource(R.string.lock_unlock),
                onClick = prompt,
                icon = Icons.Outlined.Fingerprint,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
