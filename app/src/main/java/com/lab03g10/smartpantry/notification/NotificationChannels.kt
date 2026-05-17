package com.lab03g10.smartpantry.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {

    const val EXPIRY = "expiry_reminders"
    const val SENSOR_BREACH = "sensor_breach"
    const val SHOPPING = "shopping_reminders"
    const val LOW_STOCK = "low_stock"

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(NotificationManager::class.java)
        listOf(
            NotificationChannel(EXPIRY, "Expiry Reminders", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Daily reminders for items expiring today" },
            NotificationChannel(SENSOR_BREACH, "Sensor Alerts", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Temperature or humidity threshold breaches" },
            NotificationChannel(SHOPPING, "Shopping Reminders", NotificationManager.IMPORTANCE_LOW)
                .apply { description = "Shopping list reminders" },
            NotificationChannel(LOW_STOCK, "Low Stock Alerts", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Items running low in your pantry" },
        ).forEach { nm.createNotificationChannel(it) }
    }
}
