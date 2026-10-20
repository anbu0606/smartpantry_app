package com.lab03g10.smartpantry.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lab03g10.smartpantry.notification.ExpiryNotifier
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ExpiryDigestWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val notifier: ExpiryNotifier,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {
        notifier.notifyExpiringItems()
        Result.success()
    } catch (_: Throwable) {
        Result.retry()
    }
}
