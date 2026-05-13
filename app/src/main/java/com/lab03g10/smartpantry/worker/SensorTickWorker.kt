package com.lab03g10.smartpantry.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lab03g10.smartpantry.data.local.entity.NotificationEntity
import com.lab03g10.smartpantry.data.local.entity.SensorConfigEntity
import com.lab03g10.smartpantry.data.local.entity.SensorReadingEntity
import com.lab03g10.smartpantry.data.sensor.SensorSimulator
import com.lab03g10.smartpantry.domain.repo.NotificationRepository
import com.lab03g10.smartpantry.domain.repo.SensorRepository
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
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {
        val configs = sensorRepo.observeConfigs().first().filter { it.active }
        for (config in configs) {
            simulator.load(config.id, config.csvAssetName)
            val raw = simulator.nextReading(config.id) ?: continue
            val flag = computeBreachFlag(raw, config)
            val reading = raw.copy(breachFlag = flag)
            sensorRepo.insertReading(reading)

            if (flag != 0) {
                notifRepo.insert(
                    NotificationEntity(
                        id = UUID.randomUUID().toString(),
                        type = "SENSOR_BREACH",
                        title = "${config.displayName} alert",
                        body = describeBreach(reading, config, flag),
                        relatedFoodId = null,
                        relatedSensorId = config.id,
                        createdAt = System.currentTimeMillis(),
                        read = false,
                    )
                )
            }
        }
        Result.success()
    } catch (_: Throwable) {
        Result.retry()
    }
}

// Bitset positions match SensorReadingEntity.breachFlag (bit 1 reserved for humidity).
internal const val BREACH_BIT_TEMPERATURE = 0b001
internal const val BREACH_BIT_LUX = 0b100

internal fun computeBreachFlag(
    reading: SensorReadingEntity,
    config: SensorConfigEntity,
): Int {
    var flag = 0
    if (reading.temperatureC > config.thresholdC) flag = flag or BREACH_BIT_TEMPERATURE
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
    if (flag and BREACH_BIT_LUX != 0) {
        parts += "Light ${reading.lux} lux exceeds maximum ${config.maxLux} lux"
    }
    return parts.joinToString("; ")
}