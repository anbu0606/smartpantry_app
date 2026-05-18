package com.lab03g10.smartpantry.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab03g10.smartpantry.data.local.entity.FoodItemEntity
import com.lab03g10.smartpantry.domain.repo.AuthRepository
import com.lab03g10.smartpantry.domain.repo.FoodRepository
import com.lab03g10.smartpantry.domain.repo.NotificationRepository
import com.lab03g10.smartpantry.domain.repo.PreferenceRepository
import com.lab03g10.smartpantry.domain.repo.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn


/**
 * @(#) DashboardViewModel.kt 1.0 2026-05-17
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/**
 * UI-model row for the sensor pager. Joins SensorConfig (for displayName, sensorId)
 * with the latest SensorReading (for live metrics + breach flag) so the screen
 * doesn't have to do the lookup itself. When no reading exists yet for a given
 * config (e.g. fresh install, before SensorTickWorker has fired), metrics
 * default to 0 and hasReading is false so the screen can show a placeholder.
 */
data class SensorCardUi(
    val sensorId: String,
    val displayName: String,
    val temperatureC: Float,
    val humidity: Float,
    val lux: Float,
    val isNormal: Boolean,
    val hasReading: Boolean,
)

data class DashboardUiState(
    val useTodayItems: List<FoodItemEntity> = emptyList(),
    val sensorCards: List<SensorCardUi> = emptyList(),
    val unreadNotificationCount: Int = 0,
    val totalItemCount: Int = 0,
    val expiringTodayCount: Int = 0,
    val expiringThisWeekCount: Int = 0,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val sensorRepository: SensorRepository,
    private val notificationRepository: NotificationRepository,
    authRepository: AuthRepository,
    preferenceRepository: PreferenceRepository,
) : ViewModel() {

    /**
     * First letter shown on the Dashboard top-bar avatar. Source order:
     *   1. User's preferred display name (`PreferenceRepository.observeUserName`)
     *   2. Firebase account displayName
     *   3. Firebase account email (first letter)
     *   4. "?" placeholder while everything is still null/blank
     *
     * Kept as a sibling StateFlow rather than folded into `state` because the
     * main combine is already at 5 arms and the avatar updates are independent
     * of the dashboard's food / sensor / notification streams.
     */
    val userInitial: StateFlow<String> = combine(
        authRepository.observeCurrentUser(),
        preferenceRepository.observeUserName(),
    ) { firebaseUser, prefName ->
        val name = prefName.takeIf { it.isNotBlank() }
            ?: firebaseUser?.displayName?.takeIf { it.isNotBlank() }
            ?: firebaseUser?.email
            ?: ""
        name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "?")

    val state: StateFlow<DashboardUiState> = combine(
        foodRepository.observeUseToday(today = endOfTodayMillis()),
        sensorRepository.observeConfigs(),
        sensorRepository.observeLatestPerSensor(),
        notificationRepository.observeUnreadCount(),
        foodRepository.observeAll(),
    ) { useToday, configs, readings, unread, allItems ->
        val readingsBySensor = readings.associateBy { it.sensorId }
        val cards = configs.filter { it.active }.map { config ->
            val reading = readingsBySensor[config.id]
            SensorCardUi(
                sensorId     = config.id,
                displayName  = config.displayName,
                temperatureC = reading?.temperatureC ?: 0f,
                humidity     = reading?.humidity ?: 0f,
                lux          = reading?.lux ?: 0f,
                // breachFlag 0 = clean reading; null reading = treat as normal placeholder
                isNormal     = (reading?.breachFlag ?: 0) == 0,
                hasReading   = reading != null,
            )
        }
        // Inventory counts: exclude already-consumed / already-wasted rows so the
        // tiles reflect what's *currently* in the pantry, not lifetime totals.
        val activeItems = allItems.filter { !it.consumed && !it.wasted }
        val endOfToday = endOfTodayMillis()
        val endOfWeek  = endOfThisWeekMillis()
        DashboardUiState(
            useTodayItems           = useToday,
            sensorCards             = cards,
            unreadNotificationCount = unread,
            totalItemCount          = activeItems.size,
            expiringTodayCount      = activeItems.count { it.effectiveExpiryDate <= endOfToday },
            expiringThisWeekCount   = activeItems.count { it.effectiveExpiryDate <= endOfWeek },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())
}

/**
 * 23:59:59.999 of "today" in the device's local timezone.
 *
 * Local — not UTC — because "Use Today" is a user-facing concept tied to
 * the user's wall-clock day. UTC end-of-day would shift the cutoff by up
 * to 14 h, causing items to appear/disappear at midnight UTC instead of
 * local midnight.  See Phase 10 (timezone audit) for the project-wide
 * decision.
 */
private fun endOfTodayMillis(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 23)
    set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59)
    set(Calendar.MILLISECOND, 999)
}.timeInMillis

/**
 * 23:59:59.999 of "today + 6 days" in the device's local timezone — i.e. a
 * rolling 7-day window starting today, matching the "This Week" stat tile
 * and the Expiry Calendar shortcut. Rolling — not "end of current calendar
 * week" — because the user's intuition for "this week" is "next 7 days",
 * not "until Sunday".
 */
private fun endOfThisWeekMillis(): Long = Calendar.getInstance().apply {
    add(Calendar.DAY_OF_YEAR, 6)
    set(Calendar.HOUR_OF_DAY, 23)
    set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59)
    set(Calendar.MILLISECOND, 999)
}.timeInMillis
