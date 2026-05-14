package com.lab03g10.smartpantry.data.sensor

import com.lab03g10.smartpantry.data.local.entity.SensorConfigEntity
import com.lab03g10.smartpantry.data.local.entity.SensorReadingEntity
import javax.inject.Inject
import javax.inject.Singleton


/**
 * @(#) ThresholdEngine.kt 1.0 2026-05-14
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
@Singleton
class ThresholdEngine @Inject constructor() {



    /**
     * Analyse [readings] against [config] thresholds and return a stress summary.
     *
     * A reading is considered a "breach" if temperature OR lux exceeds the
     * config's threshold — matching the same semantics as
     * [SensorTickWorker.computeBreachFlag].
     */
    fun analyse(
        config: SensorConfigEntity,
        readings: List<SensorReadingEntity>,
    ): StressSummary {
        if (readings.isEmpty()) return StressSummary(0, 0, 0, 0L, 0L)

        var breach = 0
        var clean = 0
        for (r in readings) {
            if (r.temperatureC > config.thresholdC || r.lux > config.maxLux) breach++ else clean++
        }
        return StressSummary(
            breachCount = breach,
            cleanCount = clean,
            totalCount = readings.size,
            windowStartMs = readings.first().timestamp,
            windowEndMs = readings.last().timestamp,
        )
    }
}

/**
 * Aggregated stress metrics for a batch of sensor readings.
 *
 * @property breachCount  Number of readings where temp > thresholdC OR lux > maxLux.
 * @property cleanCount   Number of readings where both metrics are within threshold.
 * @property totalCount   breachCount + cleanCount (== readings.size).
 * @property windowStartMs Timestamp of the earliest reading analysed (0 if empty).
 * @property windowEndMs   Timestamp of the latest reading analysed (0 if empty).
 */
data class StressSummary(
    val breachCount: Int,
    val cleanCount: Int,
    val totalCount: Int,
    val windowStartMs: Long,
    val windowEndMs: Long,
)

