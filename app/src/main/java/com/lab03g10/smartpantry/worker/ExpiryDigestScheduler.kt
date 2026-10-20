package com.lab03g10.smartpantry.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpiryDigestScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun schedule() {
        val request = PeriodicWorkRequestBuilder<ExpiryDigestWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(millisUntilNineAm(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    private fun millisUntilNineAm(): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            if (get(Calendar.HOUR_OF_DAY) >= 9) add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return (target.timeInMillis - now.timeInMillis).coerceAtLeast(0L)
    }

    companion object {
        const val UNIQUE_WORK_NAME = "smartpantry.expiry-digest"
    }
}
