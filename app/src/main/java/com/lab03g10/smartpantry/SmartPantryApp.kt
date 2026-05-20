package com.lab03g10.smartpantry

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.lab03g10.smartpantry.domain.repo.PreferenceRepository
import com.lab03g10.smartpantry.notification.ExpiryAlarmScheduler
import com.lab03g10.smartpantry.notification.NotificationChannels
import com.lab03g10.smartpantry.worker.ExpiryDigestScheduler
import com.lab03g10.smartpantry.worker.SensorTickScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * @(#) SmartPantryApp.kt 1.0 2026-05-10
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * Application entry point with Hilt-managed dependency injection.
 *
 * Hilt was chosen as the DI mechanism per Google's official Android Architecture
 * Guide, which states: "Hilt is the recommended dependency injection solution
 * for Android apps." See https://developer.android.com/topic/architecture/recommendations#di
 *
 * Hilt also provides first-class WorkManager integration via [HiltWorkerFactory],
 * which is required for our SensorTickWorker (Advanced Feature 1) to inject
 * SensorRepository at construction time.
 */
@HiltAndroidApp
class SmartPantryApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var sensorTickScheduler: SensorTickScheduler
    @Inject lateinit var expiryAlarmScheduler: ExpiryAlarmScheduler
    @Inject lateinit var expiryDigestScheduler: ExpiryDigestScheduler
    @Inject lateinit var preferenceRepository: PreferenceRepository

    // Process-lifetime scope for app-startup side effects. Survives every
    // Activity teardown and dies only when the process exits.
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.create(this)
        expiryAlarmScheduler.schedule()
        expiryDigestScheduler.schedule()

        // Honor the user's "Background monitoring" toggle. The collector
        // fires on startup with whatever is persisted (defaults to `true`)
        // and reruns when ProfileScreen flips the preference, so
        // schedule/cancel and the Switch UI stay in sync via a single
        // side-effect point.
        appScope.launch {
            preferenceRepository.observeSensorMonitoringEnabled()
                .distinctUntilChanged()
                .collect { enabled ->
                    if (enabled) {
                        sensorTickScheduler.schedule()
                    } else {
                        sensorTickScheduler.cancel()
                    }
                }
        }
    }
}
