package com.lab03g10.smartpantry.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.lab03g10.smartpantry.domain.repo.FoodRepository
import com.lab03g10.smartpantry.domain.repo.PreferenceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpiryNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val foodRepository: FoodRepository,
    private val preferenceRepository: PreferenceRepository,
    private val guard: NotificationGuard,
) {
    suspend fun notifyExpiringItems() {
        if (guard.suppress()) return

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

    private fun startOfDay(): Long = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    companion object {
        private const val DAY_MS = 24 * 60 * 60 * 1000L
    }
}
