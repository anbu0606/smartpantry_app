package com.lab03g10.smartpantry.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab03g10.smartpantry.data.local.entity.SensorConfigEntity
import com.lab03g10.smartpantry.domain.repo.AuthRepository
import com.lab03g10.smartpantry.domain.repo.PreferenceRepository
import com.lab03g10.smartpantry.domain.repo.SensorRepository
import com.lab03g10.smartpantry.notification.ExpiryNotifier // TODO-TEST: remove before submission
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    // Master switch state for the periodic SensorTickWorker. The actual
    // schedule/cancel side-effect is driven by SmartPantryApp's collector
    // on this same flow, so this VM only writes the preference.
    val isSensorMonitoringEnabled: StateFlow<Boolean> =
        preferenceRepository.observeSensorMonitoringEnabled().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            true,
        )

    fun setSensorMonitoringEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferenceRepository.setSensorMonitoringEnabled(enabled)
        }
    }

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

    private val _isSigningOut = MutableStateFlow(false)

    /**
     * True while [signOut] is awaiting BackupRepository.pushSnapshot —
     * the Sign Out button binds to this to show a spinner and disable
     * itself, blocking the user from triggering a second sign-out
     * (and the navigation callback) before the push completes.
     */
    val isSigningOut: StateFlow<Boolean> = _isSigningOut.asStateFlow()

    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            _isSigningOut.value = true
            authRepository.signOut()
            _isSigningOut.value = false
            // Navigate only after push + auth-clear finish; ProfileScreen
            // will tear down here, cancelling viewModelScope, which is fine
            // since the suspending work has already returned.
            onComplete()
        }
    }

    // TODO-TEST: remove this function before submission
    fun triggerTestNotification() {
        viewModelScope.launch { expiryNotifier.notifyExpiringItems() }
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
