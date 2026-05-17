package com.lab03g10.smartpantry.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ExpiryAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var notifier: ExpiryNotifier
    @Inject lateinit var scheduler: ExpiryAlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    Intent.ACTION_BOOT_COMPLETED -> scheduler.schedule()
                    else -> {
                        notifier.notifyExpiringItems()
                        scheduler.schedule()
                    }
                }
            } finally {
                result.finish()
            }
        }
    }
}
