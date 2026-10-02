package com.hisabkitab.feature.home

import java.time.ZoneId
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hisabkitab.core.designsystem.R as DesignR
import com.hisabkitab.core.designsystem.component.CategoryIconBadge
import com.hisabkitab.core.designsystem.component.EmptyState
import com.hisabkitab.core.designsystem.component.GlassCard
import com.hisabkitab.core.designsystem.component.GlassIconButton
import com.hisabkitab.core.designsystem.component.SpotlightItem
import com.hisabkitab.core.designsystem.component.spotlightOnClick
import com.hisabkitab.core.designsystem.component.glass
import com.hisabkitab.core.designsystem.theme.HisabKitabTheme
import com.hisabkitab.core.designsystem.theme.LocalNavBarClearance
import com.hisabkitab.core.model.BudgetPeriod
import com.hisabkitab.core.model.BudgetStatus
import com.hisabkitab.core.model.PeriodTotals
import com.hisabkitab.core.ui.AnimatedMoneyText
import com.hisabkitab.core.ui.displayName
import com.hisabkitab.core.ui.DateFormats
import com.hisabkitab.core.ui.LocalMoneyFormatter
import com.hisabkitab.core.ui.TransactionListItem
import com.hisabkitab.core.ui.greeting
import com.hisabkitab.core.ui.percentOf
import com.hisabkitab.core.ui.percentText
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs

@Composable
fun HomeScreen(
    onAddTransaction: () -> Unit,
    onOpenTransaction: (Long) -> Unit,
    onSeeAllTransactions: () -> Unit,
    onOpenBudget: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        userName = userName,
        onPreviousMonth = viewModel::showPreviousMonth,
        onNextMonth = viewModel::showNextMonth,
        onCurrentMonth = viewModel::showCurrentMonth,
        onAddTransaction = onAddTransaction,
        onOpenTransaction = onOpenTransaction,
        onSeeAllTransactions = onSeeAllTransactions,
        onOpenBudget = onOpenBudget,
        onOpenSettings = onOpenSettings,
    )
}

@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    userName: String = "",
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onCurrentMonth: () -> Unit,
    onAddTransaction: () -> Unit,
    onOpenTransaction: (Long) -> Unit,
    onSeeAllTransactions: () -> Unit,
    onOpenBudget: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val systemBars = WindowInsets.systemBars.asPaddingValues()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = systemBars.calculateTopPadding() + 8.dp,
            bottom = systemBars.calculateBottomPadding() + LocalNavBarClearance.current + 8.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "header") { Header(userName, onOpenSettings) }
        item(key = "hero") {
            BalanceHero(
                month = uiState.month,
                isCurrentMonth = uiState.isCurrentMonth,
                totals = uiState.totals,
                dailySpending = uiState.dailySpending,
                dailyIncome = uiState.dailyIncome,
                dailyCount = uiState.dailyCount,
                insights = uiState.insights,
                onPrevious = onPreviousMonth,
                onNext = onNextMonth,
                onCurrent = onCurrentMonth,
            )
        }
        item(key = "budget") {
            BudgetCard(
                status = uiState.budgetStatus,
                onClick = onOpenBudget,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        item(key = "recent") {
            GlassCard(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.recent_transactions), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    if (uiState.recentTransactions.isNotEmpty()) {
                        TextButton(onClick = onSeeAllTransactions) { Text(stringResource(R.string.see_all)) }
                    }
                }
                if (uiState.recentTransactions.isEmpty() && !uiState.isLoading) {
                    EmptyState(
                        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                        title = stringResource(R.string.empty_home_title),
                        message = stringResource(R.string.empty_home_message),
                        action = { TextButton(onClick = onAddTransaction) { Text(stringResource(DesignR.string.add_transaction)) } },
                    )
                } else {
                    uiState.recentTransactions.forEach { transaction ->
                        TransactionListItem(
                            transaction = transaction,
                            onClick = { onOpenTransaction(transaction.id) },
                            showDate = true,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(userName: String, onOpenSettings: () -> Unit) {
    val colors = HisabKitabTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                greeting(userName),
                style = MaterialTheme.typography.titleMedium,
                color = colors.onBackdropMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                DateFormats.weekdayLong(LocalDate.now()),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onBackdrop,
            )
        }
        GlassIconButton(Icons.Outlined.Settings, stringResource(DesignR.string.settings_title), onOpenSettings)
    }
}

@Composable
private fun BalanceHero(
    month: YearMonth,
    isCurrentMonth: Boolean,
    totals: PeriodTotals,
    dailySpending: List<Long>,
    dailyIncome: List<Long>,
    dailyCount: List<Int>,
    insights: MonthInsights,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onCurrent: () -> Unit,
) {
    val colors = HisabKitabTheme.colors
    val formatter = LocalMoneyFormatter.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            // Swipe the hero sideways to change month.
            .pointerInput(Unit) {
                var drag = 0f
                detectHorizontalDragGestures(
                    onDragStart = { drag = 0f },
                    onDragEnd = {
                        if (abs(drag) > 120f) if (drag > 0) onPrevious() else onNext()
                    },
                    onHorizontalDrag = { _, delta -> drag += delta },
                )
            },
    ) {
        MonthSwitcher(month, isCurrentMonth, onPrevious, onNext, onCurrent)
        Spacer(Modifier.height(18.dp))
        Text(stringResource(R.string.balance), style = MaterialTheme.typography.labelLarge, color = colors.onBackdropMuted)
        val monthLabel = DateFormats.monthYear(month)
        val balanceTitle = stringResource(R.string.balance)
        val incomeLabel = stringResource(DesignR.string.income)
        val expenseLabel = stringResource(DesignR.string.expenses)
        val savedLabel = stringResource(R.string.spotlight_saved)
        val savedPercent = if (totals.incomeMinor > 0) {
            percentText(percentOf(totals.balanceMinor, totals.incomeMinor).coerceIn(-999.0, 100.0))
        } else {
            null
        }
        AnimatedMoneyText(
            modifier = Modifier.spotlightOnClick {
                SpotlightItem(
                    title = balanceTitle,
                    value = formatter.formatNet(totals.balanceMinor),
                    caption = monthLabel,
                    icon = Icons.Outlined.AccountBalanceWallet,
                    details = buildList {
                        add(incomeLabel to formatter.format(totals.incomeMinor))
                        add(expenseLabel to formatter.format(totals.expenseMinor))
                        if (savedPercent != null) add(savedLabel to savedPercent)
                    },
                )
            },
            amountMinor = totals.balanceMinor,
            format = formatter::formatNet,
            style = MaterialTheme.typography.displayMedium.copy(
                color = colors.onBackdrop,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
            ),
            minFontSize = 24.sp,
        )
        Spacer(Modifier.height(16.dp))
        if (dailySpending.size > 1) {
            DailySpendingChart(
                month = month,
                spending = dailySpending,
                income = dailyIncome,
                counts = dailyCount,
                averageMinor = insights.averageDailySpendMinor,
                entryCount = insights.transactionCount,
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FlowPill(
                label = incomeLabel,
                amountMinor = totals.incomeMinor,
                icon = Icons.AutoMirrored.Outlined.TrendingUp,
                tint = colors.income,
                modifier = Modifier.weight(1f),
                spotlight = {
                    SpotlightItem(incomeLabel, formatter.format(totals.incomeMinor), monthLabel, Icons.AutoMirrored.Outlined.TrendingUp, colors.income)
                },
            )
            FlowPill(
                label = expenseLabel,
                amountMinor = totals.expenseMinor,
                icon = Icons.AutoMirrored.Outlined.TrendingDown,
                tint = colors.expense,
                modifier = Modifier.weight(1f),
                spotlight = {
                    SpotlightItem(expenseLabel, formatter.format(totals.expenseMinor), monthLabel, Icons.AutoMirrored.Outlined.TrendingDown, colors.expense)
                },
            )
        }
    }
}

@Composable
private fun MonthSwitcher(
    month: YearMonth,
    isCurrentMonth: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onCurrent: () -> Unit,
) {
    val colors = HisabKitabTheme.colors
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(colors.onBackdrop.copy(alpha = if (colors.isDark) 0.1f else 0.14f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SwitcherArrow(Icons.AutoMirrored.Filled.KeyboardArrowLeft, stringResource(R.string.previous_month), onPrevious)
        AnimatedContent(
            targetState = month,
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (slideInHorizontally { it / 2 * direction } + fadeIn())
                    .togetherWith(slideOutHorizontally { -it / 2 * direction } + fadeOut())
                    .using(SizeTransform(clip = false))
            },
            label = "month",
        ) { target ->
            Text(
                text = DateFormats.monthYear(target),
                style = MaterialTheme.typography.titleSmall,
                color = colors.onBackdrop,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(enabled = !isCurrentMonth, onClick = onCurrent)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            )
        }
        SwitcherArrow(Icons.AutoMirrored.Filled.KeyboardArrowRight, stringResource(R.string.next_month), onNext)
    }
}

@Composable
private fun SwitcherArrow(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(36.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = HisabKitabTheme.colors.onBackdrop)
    }
}

@Composable
private fun FlowPill(
    label: String,
    amountMinor: Long,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    spotlight: () -> SpotlightItem,
) {
    Row(
        modifier = modifier
            .glass(MaterialTheme.shapes.medium)
            .spotlightOnClick(spotlight)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(tint.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            AnimatedMoneyText(
                amountMinor = amountMinor,
                style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                minFontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun BudgetCard(status: BudgetStatus?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val formatter = LocalMoneyFormatter.current
    val colors = HisabKitabTheme.colors
    GlassCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        if (status == null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(52.dp).clip(CircleShape).background(Brush.linearGradient(colors.accentGradient)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Savings, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.budget_set_title), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.budget_set_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
            return@GlassCard
        }

        val ringColor = when {
            status.isOverBudget -> colors.expense
            status.progress >= 0.8f -> colors.warning
            else -> MaterialTheme.colorScheme.primary
        }
        val title = when (status.budget.period) {
            BudgetPeriod.DAILY -> R.string.budget_title_daily
            BudgetPeriod.WEEKLY -> R.string.budget_title_weekly
            BudgetPeriod.MONTHLY -> R.string.budget_title_monthly
            BudgetPeriod.CUSTOM -> R.string.budget_title_custom
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            BudgetRing(progress = status.progress, color = ringColor, size = 104.dp) {
                Text(
                    text = "${(status.progress * 100).toInt().coerceAtMost(999)}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(title), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                when {
                    status.hasEnded -> Text(stringResource(R.string.budget_ended), style = MaterialTheme.typography.bodyMedium)
                    !status.isActive -> Text(
                        stringResource(R.string.budget_not_started, DateFormats.medium(status.range.start)),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    status.isOverBudget -> Text(
                        stringResource(R.string.budget_over_by, formatter.format(-status.remainingMinor)),
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.expense,
                        fontWeight = FontWeight.Bold,
                    )
                    else -> {
                        Text(stringResource(R.string.budget_safe_today), style = MaterialTheme.typography.bodySmall)
                        Text(
                            formatter.format(status.dailyAllowanceMinor),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Text(
                    stringResource(
                        R.string.budget_spent_of,
                        formatter.format(status.spentMinor),
                        formatter.format(status.budget.amountMinor),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (status.isActive) {
                    Text(
                        pluralStringResource(R.plurals.budget_days_left, status.daysLeft.toInt(), status.daysLeft.toInt()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** Circular progress with a rounded, animated sweep. */
@Composable
private fun BudgetRing(progress: Float, color: Color, size: Dp, content: @Composable () -> Unit) {
    val animated = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        animated.animateTo(progress.coerceIn(0f, 1f), tween(900, easing = FastOutSlowInEasing))
    }
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 11.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            drawArc(track, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            drawArc(
                brush = Brush.sweepGradient(listOf(color.copy(alpha = 0.55f), color, color)),
                startAngle = -90f,
                sweepAngle = 360f * animated.value,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        content()
    }
}

@Preview
@Composable
private fun HomeHeroPreview() {
    com.hisabkitab.core.designsystem.theme.HisabKitabTheme {
        com.hisabkitab.core.designsystem.component.AuroraBackground {
            BalanceHero(
                month = YearMonth.of(2026, 9),
                isCurrentMonth = true,
                totals = PeriodTotals(incomeMinor = 8_500_000, expenseMinor = 3_245_050),
                dailySpending = listOf(1200, 400, 3000, 800, 0, 2500, 900).map { it * 100L },
                dailyIncome = listOf(0, 0, 0, 0, 0, 85_000, 0).map { it * 100L },
                dailyCount = listOf(3, 1, 4, 2, 0, 5, 2),
                insights = MonthInsights(averageDailySpendMinor = 125_000, transactionCount = 17),
                onPrevious = {},
                onNext = {},
                onCurrent = {},
            )
        }
    }
}
