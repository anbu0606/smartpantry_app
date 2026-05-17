package com.lab03g10.smartpantry.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.lab03g10.smartpantry.domain.repo.FoodRepository
import com.lab03g10.smartpantry.domain.repo.PreferenceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpiryNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val foodRepository: FoodRepository,
    private val preferenceRepository: PreferenceRepository,
) {
    suspend fun notifyExpiringItems() {
        if (!hasPostNotificationsPermission()) return
        if (isInQuietHours()) return

        val notifyDaysBefore = preferenceRepository.observeNotifyDaysBefore().first()
        val startOfToday = startOfDay()
        val windowEnd = startOfToday + notifyDaysBefore * DAY_MS

        val items = foodRepository.findExpiringBetween(startOfToday, windowEnd)
        if (items.isEmpty()) return

        val nm = NotificationManagerCompat.from(context)
        items.forEach { item ->
            val daysLeft = ((item.effectiveExpiryDate - startOfToday) / DAY_MS).toInt()
            val contentText = when (daysLeft) {
                0 -> "Use it today before it expires!"
                1 -> "Expires tomorrow — use it soon."
                else -> "Expires in $daysLeft days."
            }
            val notification = NotificationCompat.Builder(context, NotificationChannels.EXPIRY)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Use Today: ${item.name}")
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
            nm.notify(item.id.hashCode(), notification)
        }
    }

    private fun hasPostNotificationsPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    private suspend fun isInQuietHours(): Boolean {
        val start = preferenceRepository.observeQuietHoursStart().first()
        val end = preferenceRepository.observeQuietHoursEnd().first()
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return if (start <= end) hour in start until end else hour >= start || hour < end
    }

    private fun startOfDay(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    companion object {
        private const val DAY_MS = 24 * 60 * 60 * 1000L
    }
}
