package com.lab03g10.smartpantry.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab03g10.smartpantry.data.local.entity.SensorConfigEntity
import com.lab03g10.smartpantry.domain.repo.AuthRepository
import com.lab03g10.smartpantry.domain.repo.PreferenceRepository
import com.lab03g10.smartpantry.domain.repo.SensorRepository
import com.lab03g10.smartpantry.notification.ExpiryNotifier // TODO-TEST: remove before submission
import com.lab03g10.smartpantry.worker.SensorTickScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferenceRepository: PreferenceRepository,
    // TODO-TEST: remove expiryNotifier param and its function before submission
    private val expiryNotifier: ExpiryNotifier,
    private val sensorTickScheduler: SensorTickScheduler,
    private val sensorRepository: SensorRepository,
) : ViewModel() {

    /** Active sensors for the "Alert Thresholds" dropdown on Profile. */
    val sensors: StateFlow<List<SensorConfigEntity>> = sensorRepository.observeConfigs()
        .map { all -> all.filter { it.active } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currentUser = authRepository.observeCurrentUser().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    val isDarkMode: StateFlow<Boolean> = preferenceRepository.observeIsDarkMode().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    val notifyDaysBefore: StateFlow<Int> = preferenceRepository.observeNotifyDaysBefore().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        3
    )

    val quietHoursStart: StateFlow<Int> = preferenceRepository.observeQuietHoursStart().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        22
    )

    val quietHoursEnd: StateFlow<Int> = preferenceRepository.observeQuietHoursEnd().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        8
    )

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferenceRepository.setDarkMode(enabled)
        }
    }

    fun setNotifyDaysBefore(days: Int) {
        viewModelScope.launch {
            preferenceRepository.setNotifyDaysBefore(days)
        }
    }

    val userName: StateFlow<String> = preferenceRepository.observeUserName().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ""
    )

    fun setQuietHours(start: Int, end: Int) {
        viewModelScope.launch {
            preferenceRepository.setQuietHoursStart(start)
            preferenceRepository.setQuietHoursEnd(end)
        }
    }

    fun setUserName(name: String) {
        viewModelScope.launch { preferenceRepository.setUserName(name) }
    }

    fun signOut() {
        authRepository.signOut()
    }

    // TODO-TEST: remove this function before submission
    fun triggerTestNotification() {
        viewModelScope.launch { expiryNotifier.notifyExpiringItems() }
    }

    /**
     * Debug-only: enqueues a one-shot SensorTickWorker so demo + manual
     * verification don't have to wait for the 15-minute periodic interval.
     * Runs the exact same production worker — no fake data injected; the
     * UI entry point is gated on BuildConfig.DEBUG (see ProfileScreen).
     * See docs/bug-log.md BUG-009.
     */
    fun triggerSensorTickNow() {
        sensorTickScheduler.triggerOnce()
    }

    /**
     * Persist new alert thresholds for [sensorId]. Re-fetches the row so
     * unrelated fields (displayName, deviceId, csvAssetName, pairedAt,
     * active) survive untouched. The next SensorTickWorker tick picks up
     * the new values when computing breach flags.
     */
    fun saveSensorThresholds(
        sensorId: String,
        thresholdC: Float,
        humidityThreshold: Float,
        maxLux: Float,
    ) {
        viewModelScope.launch {
            val existing = sensorRepository.observeConfigById(sensorId).firstOrNull()
                ?: return@launch
            sensorRepository.upsertConfig(
                existing.copy(
                    thresholdC        = thresholdC,
                    humidityThreshold = humidityThreshold,
                    maxLux            = maxLux,
                ),
            )
        }
    }
}
