package com.lab03g10.smartpantry.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.lab03g10.smartpantry.data.local.entity.SensorConfigEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Posts a system-tray notification when a monitored sensor crosses from
 * a clean reading into a breach. Edge detection is the caller's
 * responsibility (see SensorTickWorker) so this class stays a thin
 * adapter over the NotificationCompat builder — easier to reason about,
 * easier to swap if we later replace the channel.
 *
 * Notification ID = sensor id hash. Re-posting for the same sensor
 * replaces (rather than stacks) the existing alert so a flaky sensor
 * can't spam the shade with duplicates.
 */
@Singleton
class SensorBreachNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val guard: NotificationGuard,
) {

    suspend fun notifyBreach(config: SensorConfigEntity, body: String) {
        if (guard.suppress()) return

        val notification = NotificationCompat.Builder(context, NotificationChannels.SENSOR_BREACH)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("${config.displayName} alert")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(config.id.hashCode(), notification)
    }
}
