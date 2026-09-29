package com.hisabkitab.feature.onboarding

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hisabkitab.core.designsystem.component.CategoryIconBadge
import com.hisabkitab.core.designsystem.component.GlassCard
import com.hisabkitab.core.designsystem.component.GradientButton
import com.hisabkitab.core.designsystem.component.SlidingSegmentedControl
import com.hisabkitab.core.designsystem.component.ThemeGallery
import com.hisabkitab.core.designsystem.component.glass
import com.hisabkitab.core.designsystem.theme.HisabKitabTheme
import com.hisabkitab.core.designsystem.theme.LocalReducedMotion
import com.hisabkitab.core.model.AppTheme
import com.hisabkitab.core.model.CategoryColors
import com.hisabkitab.core.model.ThemeMode
import com.hisabkitab.core.security.authenticate
import com.hisabkitab.core.security.canUseAppLock
import com.hisabkitab.core.security.findFragmentActivity
import com.hisabkitab.core.ui.AppLanguage
import com.hisabkitab.core.ui.AppLanguages
import com.hisabkitab.core.ui.CurrencyDialog
import com.hisabkitab.core.ui.LanguageList
import com.hisabkitab.core.ui.LocalMoneyFormatter
import com.hisabkitab.core.ui.label
import kotlinx.coroutines.launch

private const val PAGE_COUNT = 4

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel = hiltViewModel()) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState { PAGE_COUNT }
    val scope = rememberCoroutineScope()
    val systemBars = WindowInsets.systemBars.asPaddingValues()
    val colors = HisabKitabTheme.colors
    val isLast = pagerState.currentPage == PAGE_COUNT - 1
    // Hindi is suggested first; an already chosen app language wins.
    var languageTag by rememberSaveable {
        mutableStateOf(AppLanguages.current().tag.ifEmpty { AppLanguages.PREFERRED_TAG })
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = systemBars.calculateTopPadding(), bottom = systemBars.calculateBottomPadding()),
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.End) {
            if (!isLast) {
                TextButton(onClick = viewModel::finish) {
                    Text(stringResource(R.string.onboarding_skip), color = colors.onBackdrop)
                }
            } else {
                Spacer(Modifier.height(48.dp))
            }
        }
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            when (page) {
                0 -> LanguagePage(selectedTag = languageTag, onSelect = { languageTag = it.tag })
                1 -> WelcomePage()
                2 -> ThemePage(
                    theme = preferences?.theme ?: AppTheme.SKYLINE,
                    mode = preferences?.themeMode ?: ThemeMode.SYSTEM,
                    onTheme = viewModel::setTheme,
                    onMode = viewModel::setThemeMode,
                )
                else -> SetupPage(
                    currencyCode = preferences?.currencyCode,
                    reminderEnabled = preferences?.reminder?.enabled ?: true,
                    appLockEnabled = preferences?.appLockEnabled ?: false,
                    onCurrency = viewModel::setCurrency,
                    onReminder = viewModel::setReminderEnabled,
                    onAppLock = viewModel::setAppLockEnabled,
                )
            }
        }
        PageDots(current = pagerState.currentPage, count = PAGE_COUNT)
        GradientButton(
            text = stringResource(if (isLast) R.string.onboarding_start else R.string.onboarding_next),
            onClick = {
                when {
                    isLast -> viewModel.finish()
                    pagerState.currentPage == 0 -> scope.launch {
                        pagerState.animateScrollToPage(1)
                        // Switching language recreates the activity; the pager position is saved.
                        val chosen = AppLanguages.all.firstOrNull { it.tag == languageTag } ?: AppLanguages.SYSTEM
                        if (chosen.tag != AppLanguages.current().tag) AppLanguages.apply(chosen)
                    }
                    else -> scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            },
            icon = Icons.AutoMirrored.Filled.ArrowForward,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )
    }
}

@Composable
private fun PageDots(current: Int, count: Int) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(count) { index ->
            Box(
                Modifier
                    .padding(horizontal = 4.dp)
                    .animateContentSize()
                    .height(8.dp)
                    .width(if (index == current) 28.dp else 8.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = if (index == current) 1f else 0.4f)),
            )
        }
    }
}

@Composable
private fun PageTitle(title: String, message: String) {
    val colors = HisabKitabTheme.colors
    Text(
        title,
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Bold,
        color = colors.onBackdrop,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(12.dp))
    Text(
        message,
        style = MaterialTheme.typography.bodyLarge,
        color = colors.onBackdropMuted,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
    )
}

@Composable
private fun WelcomePage() {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        FloatingBadges()
        Spacer(Modifier.height(32.dp))
        PageTitle(stringResource(R.string.onboarding_welcome_title), stringResource(R.string.onboarding_welcome_message))
        Spacer(Modifier.height(24.dp))
        FeatureLine(Icons.Outlined.Bolt, stringResource(R.string.onboarding_feature_fast))
        FeatureLine(Icons.AutoMirrored.Outlined.ShowChart, stringResource(R.string.onboarding_feature_insights))
        FeatureLine(Icons.Outlined.Lock, stringResource(R.string.onboarding_feature_private))
    }
}

/** A playful cluster of category badges gently bobbing around a glass "coin". */
@Composable
private fun FloatingBadges() {
    val reducedMotion = LocalReducedMotion.current
    val bob = if (reducedMotion) {
        0f
    } else {
        val transition = rememberInfiniteTransition(label = "bob")
        val value by transition.animateFloat(
            initialValue = -1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(2_400), RepeatMode.Reverse),
            label = "bobValue",
        )
        value
    }
    val formatter = LocalMoneyFormatter.current
    Box(Modifier.size(260.dp, 200.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(150.dp)
                .glass(CircleShape, strong = true),
            contentAlignment = Alignment.Center,
        ) {
            Text(formatter.symbol, fontSize = 64.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        val badges = listOf(
            Triple("icon_55", -96, -60),
            Triple("icon_24", 100, -54),
            Triple("icon_132", -100, 62),
            Triple("icon_5", 96, 64),
        )
        badges.forEachIndexed { index, (icon, x, y) ->
            val direction = if (index % 2 == 0) 1 else -1
            CategoryIconBadge(
                iconKey = icon,
                color = CategoryColors.forIndex(index * 3),
                size = 54.dp,
                filled = true,
                modifier = Modifier
                    .offset(x = x.dp, y = (y + bob * 8 * direction).dp)
                    .rotate(bob * 6 * direction),
            )
        }
    }
}

@Composable
private fun FeatureLine(icon: ImageVector, text: String) {
    val colors = HisabKitabTheme.colors
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = colors.onBackdrop, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = colors.onBackdrop)
    }
}

@Composable
private fun ThemePage(theme: AppTheme, mode: ThemeMode, onTheme: (AppTheme) -> Unit, onMode: (ThemeMode) -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(Modifier.padding(horizontal = 24.dp)) {
            PageTitle(stringResource(R.string.onboarding_theme_title), stringResource(R.string.onboarding_theme_message))
        }
        Spacer(Modifier.height(28.dp))
        ThemeGallery(selected = theme, onSelect = onTheme, contentPadding = PaddingValues(horizontal = 24.dp))
        Spacer(Modifier.height(24.dp))
        SlidingSegmentedControl(
            options = listOf(
                ThemeMode.SYSTEM to stringResource(R.string.mode_auto),
                ThemeMode.LIGHT to stringResource(R.string.mode_light),
                ThemeMode.DARK to stringResource(R.string.mode_dark),
            ),
            selected = mode,
            onSelect = onMode,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }
}

@Composable
private fun SetupPage(
    currencyCode: String?,
    reminderEnabled: Boolean,
    appLockEnabled: Boolean,
    onCurrency: (String) -> Unit,
    onReminder: (Boolean) -> Unit,
    onAppLock: (Boolean) -> Unit,
) {
    var showCurrencyDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val lockAvailable = remember { canUseAppLock(context) }
    val lockTitle = stringResource(R.string.onboarding_lock_prompt)
    val lockSubtitle = stringResource(R.string.onboarding_lock_prompt_subtitle)
    Column(
        Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        PageTitle(stringResource(R.string.onboarding_setup_title), stringResource(R.string.onboarding_setup_message))
        Spacer(Modifier.height(28.dp))
        GlassCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 4.dp)) {
            SetupRow(
                icon = Icons.Outlined.Payments,
                title = stringResource(R.string.onboarding_currency),
                summary = LocalMoneyFormatter.current.currency.label(),
                onClick = { showCurrencyDialog = true },
            )
            SetupRow(
                icon = Icons.Outlined.NotificationsActive,
                title = stringResource(R.string.onboarding_reminder),
                summary = stringResource(R.string.onboarding_reminder_summary),
                onClick = { onReminder(!reminderEnabled) },
                trailing = { Switch(checked = reminderEnabled, onCheckedChange = null) },
            )
            if (lockAvailable) {
                SetupRow(
                    icon = Icons.Outlined.Lock,
                    title = stringResource(R.string.onboarding_lock),
                    summary = stringResource(R.string.onboarding_lock_summary),
                    onClick = {
                        if (appLockEnabled) {
                            onAppLock(false)
                        } else {
                            context.findFragmentActivity()?.let { activity ->
                                authenticate(activity, lockTitle, lockSubtitle, onSuccess = { onAppLock(true) })
                            }
                        }
                    },
                    trailing = { Switch(checked = appLockEnabled, onCheckedChange = null) },
                )
            }
        }
    }
    if (showCurrencyDialog) {
        CurrencyDialog(
            selectedCode = currencyCode,
            onSelect = {
                onCurrency(it)
                showCurrencyDialog = false
            },
            onDismiss = { showCurrencyDialog = false },
        )
    }
}

@Composable
private fun SetupRow(
    icon: ImageVector,
    title: String,
    summary: String,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(Brush.linearGradient(HisabKitabTheme.colors.accentGradient)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing?.invoke()
    }
}

@Composable
private fun LanguagePage(selectedTag: String, onSelect: (AppLanguage) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 24.dp)) {
            PageTitle(stringResource(R.string.onboarding_language_title), stringResource(R.string.onboarding_language_message))
        }
        Spacer(Modifier.height(20.dp))
        GlassCard(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(top = 12.dp, start = 12.dp, end = 12.dp),
        ) {
            LanguageList(
                selectedTag = selectedTag,
                onSelect = onSelect,
                includeSystem = true,
                contentPadding = PaddingValues(horizontal = 4.dp),
            )
        }
    }
}
