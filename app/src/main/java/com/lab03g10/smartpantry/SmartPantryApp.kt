package com.lab03g10.smartpantry

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

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

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // TODO: Channel creation deferred (Willy Day 6)
    }
}
