package com.lab03g10.smartpantry.worker

/**
 * Triggers a one-shot ShelfLifeRecalcWorker run.
 *
 * Both UI flows (food move, shopping → inventory transfer) and the
 * periodic SensorTickWorker chain through the same unique work name
 * so concurrent triggers dedupe — at most one recalc is pending at
 * any given moment, matching the REPLACE semantics SensorTickWorker
 * uses.
 *
 * Interface-shaped so JVM unit tests can pass a no-op without
 * standing up WorkManager.
 */
interface ShelfLifeRecalcScheduler {
    fun triggerOnce()
}
