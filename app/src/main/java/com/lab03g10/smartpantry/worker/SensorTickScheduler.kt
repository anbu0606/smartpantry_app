package com.lab03g10.smartpantry.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @(#) SensorTickScheduler.kt 1.0 2026-05-13
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
@Singleton
class SensorTickScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /** Enqueue (or keep existing) the 15-minute periodic sensor tick. */
    fun schedule() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)   // be polite about battery
            .build()

        val request = PeriodicWorkRequestBuilder<SensorTickWorker>(
            repeatInterval = 15, repeatIntervalTimeUnit = TimeUnit.MINUTES,
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,   // don't replace if already scheduled
            request,
        )
    }

    /** Cancel all sensor-tick work (used on sign-out, factory reset, etc.). */
    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    /** Fire-and-forget one-shot tick — SensorTickWorker.doWork() will chain to ShelfLifeRecalcWorker automatically. */
    fun triggerOnce() {
        val request = OneTimeWorkRequestBuilder<SensorTickWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }

    companion object {
        const val UNIQUE_WORK_NAME = "smartpantry.sensor-tick"
    }
}