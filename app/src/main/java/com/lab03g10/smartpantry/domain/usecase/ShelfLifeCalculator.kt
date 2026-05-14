package com.lab03g10.smartpantry.domain.usecase

import com.lab03g10.smartpantry.data.local.entity.FoodItemEntity
import com.lab03g10.smartpantry.data.sensor.StressSummary
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * @(#) ShelfLifeCalculator.kt 1.0 2026-05-14
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
@Singleton
class ShelfLifeCalculator @Inject constructor() {

    /**
     * Recalculate the effective expiry date for [food] given the [stress]
     * summary from ThresholdEngine.
     *
     * Applies a Q10-lite model: breach ticks consume shelf-life at 2× rate,
     * clean ticks at 0.9× rate. Returns the adjusted epoch millis.
     */
    fun recalculate(
        food: FoodItemEntity,
        stress: StressSummary,
        nowMillis: Long = System.currentTimeMillis(),
    ): Long {
        // Guard: nothing to do
        if (stress.totalCount == 0) return food.nominalExpiryDate

        val nominalShelfLifeMs = food.nominalExpiryDate - food.purchaseDate
        if (nominalShelfLifeMs <= 0) return food.nominalExpiryDate   // bad data guard

        // How much shelf-life has the monitored period consumed?
        val consumedMs = (
                stress.breachCount * BREACH_RATE + stress.cleanCount * CLEAN_RATE
                ) * TICK_INTERVAL_MS

        val effectiveRemainingMs = nominalShelfLifeMs - consumedMs
        val effectiveExpiry = food.purchaseDate + max(effectiveRemainingMs.toLong(), 0L)

        // Don't set expiry in the past — if already expired, clamp to now
        return if (effectiveExpiry < nowMillis) nowMillis else effectiveExpiry
    }

    companion object {
        /** Breach ticks consume shelf-life at 2× normal rate. */
        internal const val BREACH_RATE = 2.0

        /** Clean ticks consume only 0.9× normal rate (cold storage extends shelf-life). */
        internal const val CLEAN_RATE = 0.9

        /**
         * Approximate interval between sensor ticks in milliseconds.
         * WorkManager minimum periodic interval is 15 minutes.
         */
        internal const val TICK_INTERVAL_MS = 15.0 * 60 * 1000   // 900,000 ms
    }
}