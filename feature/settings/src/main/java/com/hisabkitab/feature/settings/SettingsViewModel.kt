package com.hisabkitab.feature.settings

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisabkitab.core.data.export.CsvExporter
import com.hisabkitab.core.data.export.CsvImporter
import com.hisabkitab.core.data.export.UnsupportedCsvException
import com.hisabkitab.core.data.repository.UserPreferencesRepository
import com.hisabkitab.core.model.AppTheme
import com.hisabkitab.core.model.ChartTransition
import com.hisabkitab.core.model.NavBarStyle
import com.hisabkitab.core.model.ThemeMode
import com.hisabkitab.core.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

enum class ExportKind { TRANSACTIONS, CATEGORIES }

sealed interface SettingsMessage {
    data class Exported(val kind: ExportKind, val count: Int) : SettingsMessage
    data object ExportFailed : SettingsMessage
    data class Imported(val count: Int, val skipped: Int) : SettingsMessage
    data object ImportFailed : SettingsMessage
    data object ImportUnsupported : SettingsMessage
}

data class SettingsUiState(
    val preferences: UserPreferences? = null,
    val isExporting: Boolean = false,
    val message: SettingsMessage? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val csvExporter: CsvExporter,
    private val csvImporter: CsvImporter,
) : ViewModel() {

    private val isExporting = MutableStateFlow(false)
    private val message = MutableStateFlow<SettingsMessage?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferencesRepository.userPreferences,
        isExporting,
        message,
    ) { preferences, exporting, message ->
        SettingsUiState(preferences = preferences, isExporting = exporting, message = message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setUserName(name: String) = launchUpdate { userPreferencesRepository.setUserName(name) }

    fun setThemeMode(mode: ThemeMode) = launchUpdate { userPreferencesRepository.setThemeMode(mode) }

    fun setTheme(theme: AppTheme) = launchUpdate { userPreferencesRepository.setTheme(theme) }

    fun setAppLockEnabled(enabled: Boolean) = launchUpdate { userPreferencesRepository.setAppLockEnabled(enabled) }

    fun setNavBarStyle(style: NavBarStyle) = launchUpdate { userPreferencesRepository.setNavBarStyle(style) }

    fun setChartTransition(transition: ChartTransition) = launchUpdate { userPreferencesRepository.setChartTransition(transition) }

    fun setCurrency(code: String) = launchUpdate { userPreferencesRepository.setCurrencyCode(code) }

    fun setReminderEnabled(enabled: Boolean) = launchUpdate { userPreferencesRepository.setReminderEnabled(enabled) }

    fun setReminderTime(time: LocalTime) = launchUpdate { userPreferencesRepository.setReminderTime(time) }

    fun export(kind: ExportKind, destination: Uri) {
        if (isExporting.value) return
        isExporting.value = true
        viewModelScope.launch {
            message.value = try {
                val count = when (kind) {
                    ExportKind.TRANSACTIONS -> csvExporter.exportTransactions(destination)
                    ExportKind.CATEGORIES -> csvExporter.exportCategories(destination)
                }
                SettingsMessage.Exported(kind, count)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Export failed", e)
                SettingsMessage.ExportFailed
            } finally {
                isExporting.value = false
            }
        }
    }

    fun importTransactions(source: Uri) {
        if (isExporting.value) return
        isExporting.value = true
        viewModelScope.launch {
            message.value = try {
                val result = csvImporter.importTransactions(source)
                SettingsMessage.Imported(result.imported, result.skipped)
            } catch (e: CancellationException) {
                throw e
            } catch (e: UnsupportedCsvException) {
                SettingsMessage.ImportUnsupported
            } catch (e: Exception) {
                Log.w(TAG, "Import failed", e)
                SettingsMessage.ImportFailed
            } finally {
                isExporting.value = false
            }
        }
    }

    fun onMessageShown() {
        message.value = null
    }

    private fun launchUpdate(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    private companion object {
        const val TAG = "Settings"
    }
}
