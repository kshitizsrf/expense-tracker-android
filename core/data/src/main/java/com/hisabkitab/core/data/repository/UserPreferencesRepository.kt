package com.hisabkitab.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hisabkitab.core.common.money.MoneyFormatter
import com.hisabkitab.core.model.Budget
import com.hisabkitab.core.model.BudgetPeriod
import com.hisabkitab.core.model.ChartTransition
import com.hisabkitab.core.model.AppTheme
import com.hisabkitab.core.model.DateRange
import com.hisabkitab.core.model.NavBarStyle
import com.hisabkitab.core.model.ReminderSettings
import com.hisabkitab.core.model.ThemeMode
import com.hisabkitab.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

interface UserPreferencesRepository {
    val userPreferences: Flow<UserPreferences>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setTheme(theme: AppTheme)
    suspend fun setCurrencyCode(code: String)
    suspend fun setReminderEnabled(enabled: Boolean)
    suspend fun setReminderTime(time: LocalTime)
    suspend fun setBudget(budget: Budget?)
    suspend fun setNotificationPermissionRequested()
    suspend fun setOnboardingCompleted()
    suspend fun setAppLockEnabled(enabled: Boolean)
    suspend fun setNavBarStyle(style: NavBarStyle)
    suspend fun setChartTransition(transition: ChartTransition)
}

class DataStoreUserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {

    override val userPreferences: Flow<UserPreferences> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { it.toUserPreferences() }

    override suspend fun setThemeMode(mode: ThemeMode) = edit { it[Keys.THEME_MODE] = mode.name }

    override suspend fun setTheme(theme: AppTheme) = edit { it[Keys.THEME] = theme.name }

    override suspend fun setCurrencyCode(code: String) = edit { it[Keys.CURRENCY_CODE] = code }

    override suspend fun setReminderEnabled(enabled: Boolean) = edit { it[Keys.REMINDER_ENABLED] = enabled }

    override suspend fun setReminderTime(time: LocalTime) =
        edit { it[Keys.REMINDER_MINUTE_OF_DAY] = time.hour * 60 + time.minute }

    override suspend fun setBudget(budget: Budget?) = edit { prefs ->
        if (budget == null) {
            prefs.remove(Keys.BUDGET_AMOUNT)
            prefs.remove(Keys.BUDGET_PERIOD)
            prefs.remove(Keys.BUDGET_CUSTOM_START)
            prefs.remove(Keys.BUDGET_CUSTOM_END)
        } else {
            prefs[Keys.BUDGET_AMOUNT] = budget.amountMinor
            prefs[Keys.BUDGET_PERIOD] = budget.period.name
            val range = budget.customRange
            if (range != null) {
                prefs[Keys.BUDGET_CUSTOM_START] = range.start.toEpochDay()
                prefs[Keys.BUDGET_CUSTOM_END] = range.endInclusive.toEpochDay()
            } else {
                prefs.remove(Keys.BUDGET_CUSTOM_START)
                prefs.remove(Keys.BUDGET_CUSTOM_END)
            }
        }
    }

    override suspend fun setNotificationPermissionRequested() =
        edit { it[Keys.NOTIFICATION_PERMISSION_REQUESTED] = true }

    override suspend fun setOnboardingCompleted() = edit { it[Keys.ONBOARDING_COMPLETED] = true }

    override suspend fun setAppLockEnabled(enabled: Boolean) = edit { it[Keys.APP_LOCK] = enabled }

    override suspend fun setNavBarStyle(style: NavBarStyle) = edit { it[Keys.NAV_BAR_STYLE] = style.name }

    override suspend fun setChartTransition(transition: ChartTransition) = edit { it[Keys.CHART_TRANSITION] = transition.name }

    private suspend fun edit(block: (MutablePreferences) -> Unit) {
        dataStore.edit { block(it) }
    }

    private fun Preferences.toUserPreferences(): UserPreferences = UserPreferences(
        themeMode = this[Keys.THEME_MODE].toEnumOrNull<ThemeMode>() ?: ThemeMode.SYSTEM,
        theme = this[Keys.THEME].toEnumOrNull<AppTheme>() ?: AppTheme.SKYLINE,
        currencyCode = this[Keys.CURRENCY_CODE] ?: MoneyFormatter.defaultCurrencyCode(),
        reminder = ReminderSettings(
            enabled = this[Keys.REMINDER_ENABLED] ?: true,
            time = (this[Keys.REMINDER_MINUTE_OF_DAY] ?: DEFAULT_REMINDER_MINUTE).let {
                LocalTime.of(it / 60, it % 60)
            },
        ),
        budget = toBudget(),
        hasRequestedNotificationPermission = this[Keys.NOTIFICATION_PERMISSION_REQUESTED] ?: false,
        hasCompletedOnboarding = this[Keys.ONBOARDING_COMPLETED] ?: false,
        appLockEnabled = this[Keys.APP_LOCK] ?: false,
        navBarStyle = this[Keys.NAV_BAR_STYLE].toEnumOrNull<NavBarStyle>() ?: NavBarStyle.LIQUID,
        chartTransition = this[Keys.CHART_TRANSITION].toEnumOrNull<ChartTransition>() ?: ChartTransition.FADE,
    )

    private fun Preferences.toBudget(): Budget? {
        val amount = this[Keys.BUDGET_AMOUNT] ?: return null
        val period = this[Keys.BUDGET_PERIOD].toEnumOrNull<BudgetPeriod>() ?: return null
        val start = this[Keys.BUDGET_CUSTOM_START]
        val end = this[Keys.BUDGET_CUSTOM_END]
        val range = if (start != null && end != null && end >= start) {
            DateRange(LocalDate.ofEpochDay(start), LocalDate.ofEpochDay(end))
        } else {
            null
        }
        return Budget(amountMinor = amount, period = period, customRange = range)
    }

    private inline fun <reified T : Enum<T>> String?.toEnumOrNull(): T? =
        this?.let { name -> enumValues<T>().firstOrNull { it.name == name } }

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val THEME = stringPreferencesKey("app_theme")
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_MINUTE_OF_DAY = intPreferencesKey("reminder_minute_of_day")
        val BUDGET_AMOUNT = longPreferencesKey("budget_amount_minor")
        val BUDGET_PERIOD = stringPreferencesKey("budget_period")
        val BUDGET_CUSTOM_START = longPreferencesKey("budget_custom_start_epoch_day")
        val BUDGET_CUSTOM_END = longPreferencesKey("budget_custom_end_epoch_day")
        val NOTIFICATION_PERMISSION_REQUESTED = booleanPreferencesKey("notification_permission_requested")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val APP_LOCK = booleanPreferencesKey("app_lock_enabled")
        val NAV_BAR_STYLE = stringPreferencesKey("nav_bar_style")
        val CHART_TRANSITION = stringPreferencesKey("chart_transition")
    }

    private companion object {
        const val DEFAULT_REMINDER_MINUTE = 20 * 60 // 8:00 PM
    }
}
