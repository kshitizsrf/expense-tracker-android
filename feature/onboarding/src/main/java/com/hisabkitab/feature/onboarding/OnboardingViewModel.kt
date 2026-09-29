package com.hisabkitab.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisabkitab.core.data.repository.UserPreferencesRepository
import com.hisabkitab.core.model.AppTheme
import com.hisabkitab.core.model.ThemeMode
import com.hisabkitab.core.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Choices made during onboarding are saved immediately, so the app re-themes live. */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val preferences: StateFlow<UserPreferences?> = userPreferencesRepository.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setTheme(theme: AppTheme) = launch { userPreferencesRepository.setTheme(theme) }

    fun setThemeMode(mode: ThemeMode) = launch { userPreferencesRepository.setThemeMode(mode) }

    fun setCurrency(code: String) = launch { userPreferencesRepository.setCurrencyCode(code) }

    fun setReminderEnabled(enabled: Boolean) = launch { userPreferencesRepository.setReminderEnabled(enabled) }

    fun setAppLockEnabled(enabled: Boolean) = launch { userPreferencesRepository.setAppLockEnabled(enabled) }

    fun finish() = launch { userPreferencesRepository.setOnboardingCompleted() }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
