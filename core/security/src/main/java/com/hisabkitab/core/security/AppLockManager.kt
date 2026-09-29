package com.hisabkitab.core.security

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.hisabkitab.core.common.di.ApplicationScope
import com.hisabkitab.core.data.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Decides when the app is locked: on a cold start and whenever it returns from the
 * background after [GRACE_PERIOD_MS], if the user turned app lock on.
 */
@Singleton
class AppLockManager @Inject constructor(
    @ApplicationScope private val scope: CoroutineScope,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val clock: Clock,
) {
    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    @Volatile private var enabled = false
    @Volatile private var backgroundedAt: Long? = null
    private var receivedFirstValue = false

    /** Call once from `Application.onCreate` (main thread). */
    fun start() {
        scope.launch {
            userPreferencesRepository.userPreferences
                .map { it.appLockEnabled }
                .distinctUntilChanged()
                .collect { isEnabled ->
                    enabled = isEnabled
                    when {
                        !isEnabled -> _isLocked.value = false
                        // Cold start with the lock on: start locked. Turning it on later doesn't lock.
                        !receivedFirstValue -> _isLocked.value = true
                    }
                    receivedFirstValue = true
                }
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStop(owner: LifecycleOwner) {
                    backgroundedAt = clock.millis()
                }

                override fun onStart(owner: LifecycleOwner) {
                    val since = backgroundedAt ?: return
                    if (enabled && clock.millis() - since > GRACE_PERIOD_MS) _isLocked.value = true
                }
            },
        )
    }

    fun unlock() {
        _isLocked.value = false
    }

    private companion object {
        const val GRACE_PERIOD_MS = 30_000L
    }
}
