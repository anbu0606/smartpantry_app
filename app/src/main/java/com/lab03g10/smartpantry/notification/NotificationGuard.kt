package com.lab03g10.smartpantry.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.lab03g10.smartpantry.domain.repo.PreferenceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared gate for posting system notifications.
 *
 * Both ExpiryNotifier and SensorBreachNotifier route through [suppress]
 * so the user's POST_NOTIFICATIONS permission state and Quiet Hours
 * window are honored identically — fixing those rules in one place
 * keeps the two channels from drifting (a bug we already had once when
 * ExpiryNotifier's private quiet-hours check wasn't copied into a new
 * notifier draft).
 */
@Singleton
class NotificationGuard @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceRepository: PreferenceRepository,
) {
    /** True when system notifications should NOT be posted. */
    suspend fun suppress(): Boolean =
        !hasPostNotificationsPermission() || isInQuietHours()

    fun hasPostNotificationsPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    suspend fun isInQuietHours(): Boolean {
        val start = preferenceRepository.observeQuietHoursStart().first()
        val end = preferenceRepository.observeQuietHoursEnd().first()
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        // Window may straddle midnight (e.g. 22:00 → 08:00) — the OR branch
        // covers that. Equal start/end is treated as "no quiet hours".
        return if (start == end) false
        else if (start < end) hour in start until end
        else hour >= start || hour < end
    }
}
