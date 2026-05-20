package com.lab03g10.smartpantry.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.lab03g10.smartpantry.data.local.entity.NotificationEntity
import com.lab03g10.smartpantry.data.local.entity.SensorConfigEntity
import com.lab03g10.smartpantry.data.local.entity.SensorReadingEntity
import com.lab03g10.smartpantry.data.sensor.SensorSimulator
import com.lab03g10.smartpantry.domain.repo.NotificationRepository
import com.lab03g10.smartpantry.domain.repo.SensorRepository
import com.lab03g10.smartpantry.notification.SensorBreachNotifier
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.UUID

/**
 * @(#) SensorTickWorker.kt 1.0 2026-05-13
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
@HiltWorker
class SensorTickWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val simulator: SensorSimulator,
    private val sensorRepo: SensorRepository,
    private val notifRepo: NotificationRepository,
    private val breachNotifier: SensorBreachNotifier,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {
        val configs = sensorRepo.observeConfigs().first().filter { it.active }
        // Look up the most recent reading per sensor ONCE so we can space new
        // readings at least one tick-interval apart from the previous one.
        // Why: rapid debug-button clicks would otherwise stamp every reading
        // with the current wall-clock minute, producing chart labels like
        // "21h 21h 21h 22h 22h 22h" — visually broken and useless for demo.
        // Production behaviour is unchanged: real WorkManager ticks fire
        // ≥15 min apart, so max(now, lastTs + 15min) reduces to `now`.
        val latestBySensor = sensorRepo.observeLatestPerSensor().first()
            .associateBy { it.sensorId }
        val now = System.currentTimeMillis()
        for (config in configs) {
            simulator.load(config.id, config.csvAssetName)
            // Capture the previous flag BEFORE inserting the new reading so
            // we can edge-detect a clean→breach transition. Without this,
            // every 15-minute tick during a sustained breach would re-fire
            // the system notification (and re-insert an in-app row).
            val previousFlag = latestBySensor[config.id]?.breachFlag ?: 0
            val nextTs = latestBySensor[config.id]
                ?.let { maxOf(now, it.timestamp + TICK_INTERVAL_MS) }
                ?: now
            val raw = simulator.nextReading(config.id, nextTs) ?: continue
            val flag = computeBreachFlag(raw, config)
            val reading = raw.copy(breachFlag = flag)
            sensorRepo.insertReading(reading)

            // Only act on edge transitions (was clean, now breaching).
            // Keeps both the in-app log and the system shade quiet during a
            // sustained breach; the user already saw the alert at onset.
            if (flag != 0 && previousFlag == 0) {
                val body = describeBreach(reading, config, flag)
                notifRepo.insert(
                    NotificationEntity(
                        id = UUID.randomUUID().toString(),
                        type = "SENSOR_BREACH",
                        title = "${config.displayName} alert",
                        body = body,
                        relatedFoodId = null,
                        relatedSensorId = config.id,
                        createdAt = System.currentTimeMillis(),
                        read = false,
                    )
                )
                breachNotifier.notifyBreach(config, body)
            }
        }
        // Chain: trigger shelf-life recalculation with fresh readings.
        // Periodic workers can't use WorkContinuation.then(), so we enqueue a
        // one-shot ShelfLifeRecalcWorker with REPLACE policy — at most one
        // pending recalc, no stacking if ticks come faster than recalc finishes.
        enqueueRecalcWorker()
        Result.success()
    } catch (_: Throwable) {
        Result.retry()
    }

    private fun enqueueRecalcWorker() {
        val request = OneTimeWorkRequestBuilder<ShelfLifeRecalcWorker>().build()
        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                RECALC_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request,
            )
    }

    companion object {
        internal const val RECALC_WORK_NAME = "smartpantry.shelf-life-recalc"
        // Matches the PeriodicWorkRequest interval in SensorTickScheduler.schedule().
        private const val TICK_INTERVAL_MS: Long = 15L * 60 * 1000
    }
}

// Bitset positions match SensorReadingEntity.breachFlag.
internal const val BREACH_BIT_TEMPERATURE = 0b001
internal const val BREACH_BIT_HUMIDITY    = 0b010
internal const val BREACH_BIT_LUX         = 0b100

internal fun computeBreachFlag(
    reading: SensorReadingEntity,
    config: SensorConfigEntity,
): Int {
    var flag = 0
    if (reading.temperatureC > config.thresholdC) flag = flag or BREACH_BIT_TEMPERATURE
    if (reading.humidity > config.humidityThreshold) flag = flag or BREACH_BIT_HUMIDITY
    if (reading.lux > config.maxLux) flag = flag or BREACH_BIT_LUX
    return flag
}

internal fun describeBreach(
    reading: SensorReadingEntity,
    config: SensorConfigEntity,
    flag: Int,
): String {
    val parts = mutableListOf<String>()
    if (flag and BREACH_BIT_TEMPERATURE != 0) {
        parts += "Temperature ${reading.temperatureC}°C exceeds threshold ${config.thresholdC}°C"
    }
    if (flag and BREACH_BIT_HUMIDITY != 0) {
        parts += "Humidity ${reading.humidity}% exceeds threshold ${config.humidityThreshold}%"
    }
    if (flag and BREACH_BIT_LUX != 0) {
        parts += "Light ${reading.lux} lux exceeds maximum ${config.maxLux} lux"
    }
    return parts.joinToString("; ")
}