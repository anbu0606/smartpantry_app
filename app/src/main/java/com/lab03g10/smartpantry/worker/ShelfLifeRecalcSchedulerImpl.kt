package com.lab03g10.smartpantry.worker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShelfLifeRecalcSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ShelfLifeRecalcScheduler {

    override fun triggerOnce() {
        val request = OneTimeWorkRequestBuilder<ShelfLifeRecalcWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            SensorTickWorker.RECALC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }
}
