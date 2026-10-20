package com.lab03g10.smartpantry.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lab03g10.smartpantry.data.sensor.ThresholdEngine
import com.lab03g10.smartpantry.domain.repo.FoodRepository
import com.lab03g10.smartpantry.domain.repo.SensorRepository
import com.lab03g10.smartpantry.domain.usecase.ShelfLifeCalculator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * @(#) ShelfLifeRecalcWorker.kt 1.0 2026-05-15
 * Copyright (c) 2026 the SmartPantry team.
 */
@HiltWorker
class ShelfLifeRecalcWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val sensorRepo: SensorRepository,
    private val foodRepo: FoodRepository,
    private val engine: ThresholdEngine,
    private val calculator: ShelfLifeCalculator,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {
        recalculateAll()
        Result.success()
    } catch (_: Throwable) {
        Result.retry()
    }

    // Extracted for testability — can be called from tests with mocked repos
    internal suspend fun recalculateAll() {
        val configs = sensorRepo.observeConfigs().first().filter { it.active }
        val now = System.currentTimeMillis()

        for (config in configs) {
            val foods = foodRepo.findActiveByStorageLocation(config.id)
            if (foods.isEmpty()) continue

            // Lookback floor: for each food we use whichever is later —
            // when it was bought, or when it was moved into this sensor.
            // Without `storageChangedAt`, a food just moved from a colder
            // location would have the destination sensor's older readings
            // applied as if it had been there the whole time.
            val earliestFloor = foods.minOf { foodFloor(it) }
            val lookbackFloor = now - LOOKBACK_WINDOW_MS
            val since = maxOf(earliestFloor, lookbackFloor)

            val readings = sensorRepo.getReadingsForSensorSince(config.id, since)
            if (readings.isEmpty()) continue

            val stress = engine.analyse(config, readings)

            for (food in foods) {
                val floor = foodFloor(food)
                // Filter readings to only those after this food's effective floor
                val foodReadings = if (floor > since) {
                    readings.filter { it.timestamp >= floor }
                } else {
                    readings
                }
                if (foodReadings.isEmpty()) continue

                val foodStress = if (foodReadings.size == readings.size) {
                    stress  // reuse if same window
                } else {
                    engine.analyse(config, foodReadings)  // re-analyse for this food's window
                }

                val newExpiry = calculator.recalculate(food, foodStress, now)

                // Only write if changed (avoid unnecessary DB writes + UI recomposition triggers)
                if (newExpiry != food.effectiveExpiryDate) {
                    foodRepo.updateEffectiveExpiry(food.id, newExpiry)
                }
            }
        }
    }

    private fun foodFloor(food: com.lab03g10.smartpantry.data.local.entity.FoodItemEntity): Long =
        maxOf(food.purchaseDate, food.storageChangedAt)

    companion object {
        /** Maximum lookback window: 7 days of readings. */
        private const val LOOKBACK_WINDOW_MS = 7L * 24 * 60 * 60 * 1000
    }}