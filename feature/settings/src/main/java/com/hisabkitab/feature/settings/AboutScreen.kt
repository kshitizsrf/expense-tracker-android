package com.hisabkitab.feature.settings

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hisabkitab.core.designsystem.R as DesignR
import com.hisabkitab.core.designsystem.component.BackdropHeader
import com.hisabkitab.core.designsystem.component.GlassCard
import com.hisabkitab.core.designsystem.component.GlassIconButton
import com.hisabkitab.core.designsystem.theme.HisabKitabTheme
import com.hisabkitab.core.designsystem.theme.LocalReducedMotion

/** App identity, highlights, privacy promise and open-source credits. */
@Composable
fun AboutScreen(versionName: String, onBack: () -> Unit) {
    val systemBars = WindowInsets.systemBars.asPaddingValues()
    val colors = HisabKitabTheme.colors
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = systemBars.calculateTopPadding(), bottom = systemBars.calculateBottomPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            BackdropHeader(
                title = stringResource(R.string.about_title),
                navigationIcon = { GlassIconButton(Icons.AutoMirrored.Filled.ArrowBack, stringResource(DesignR.string.action_back), onBack) },
            )
        }
        item { AppIdentity(versionName) }
        item {
            GlassCard(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), contentPadding = PaddingValues(vertical = 8.dp)) {
                Highlight(Icons.Outlined.CloudOff, stringResource(R.string.about_offline_title), stringResource(R.string.about_offline_body))
                Highlight(Icons.Outlined.Insights, stringResource(R.string.about_insights_title), stringResource(R.string.about_insights_body))
                Highlight(Icons.Outlined.Translate, stringResource(R.string.about_languages_title), stringResource(R.string.about_languages_body))
                Highlight(Icons.Outlined.Fingerprint, stringResource(R.string.about_lock_title), stringResource(R.string.about_lock_body))
            }
        }
        item {
            GlassCard(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), contentPadding = PaddingValues(vertical = 8.dp)) {
                Highlight(Icons.Outlined.VerifiedUser, stringResource(R.string.about_privacy_title), stringResource(R.string.about_privacy_body))
            }
        }
        item {
            GlassCard(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                Text(stringResource(R.string.about_credits), style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.about_credits_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            Text(
                stringResource(R.string.made_with_love),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onBackdropMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun AppIdentity(versionName: String) {
    val colors = HisabKitabTheme.colors
    val reducedMotion = LocalReducedMotion.current
    val float by rememberInfiniteTransition(label = "logo").animateFloat(
        initialValue = 0f,
        targetValue = if (reducedMotion) 0f else 1f,
        animationSpec = infiniteRepeatable(tween(2400), RepeatMode.Reverse),
        label = "logoFloat",
    )
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .graphicsLayer { translationY = -8.dp.toPx() * float }
                .size(96.dp)
                .shadow(20.dp, CircleShape, spotColor = colors.accentGradient.first())
                .clip(CircleShape)
                .background(Brush.linearGradient(colors.accentGradient))
                .border(3.dp, Color.White.copy(alpha = if (colors.isDark) 0.2f else 0.85f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.AccountBalanceWallet,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(44.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(DesignR.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onBackdrop,
        )
        Text(
            stringResource(R.string.version, versionName),
            style = MaterialTheme.typography.labelLarge,
            color = colors.onBackdropMuted,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.about_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onBackdrop,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        )
    }
}

@Composable
private fun Highlight(icon: ImageVector, title: String, body: String) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.Top) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(Brush.linearGradient(HisabKitabTheme.colors.accentGradient)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
