package com.hisabkitab

import com.hisabkitab.core.common.di.ApplicationScope
import com.hisabkitab.core.data.repository.UserPreferencesRepository
import com.hisabkitab.core.notifications.ReminderNotifier
import com.hisabkitab.core.notifications.ReminderScheduler
import com.hisabkitab.core.security.AppLockManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/** App-start work: create notification channels and keep the reminder schedule in sync with settings. */
@Singleton
class AppInitializer @Inject constructor(
    @ApplicationScope private val scope: CoroutineScope,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val reminderScheduler: ReminderScheduler,
    private val reminderNotifier: ReminderNotifier,
    private val appLockManager: AppLockManager,
) {
    fun initialize() {
        reminderNotifier.createChannel()
        appLockManager.start()

        scope.launch {
            userPreferencesRepository.userPreferences
                .map { it.reminder }
                .distinctUntilChanged()
                .collect { reminder ->
                    if (reminder.enabled) reminderScheduler.schedule(reminder.time) else reminderScheduler.cancel()
                }
        }
    }
}
