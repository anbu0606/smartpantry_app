package com.lab03g10.smartpantry.domain.repo

import kotlinx.coroutines.flow.Flow

/**
 * This repository stores small user preference values.
 *
 * For shopping suggestions, it remembers which category suggestion
 * the user already accepted or dismissed.
 */
interface PreferenceRepository {
    fun observeIsDarkMode(): Flow<Boolean>
    suspend fun setDarkMode(enabled: Boolean)

    fun observeNotifyDaysBefore(): Flow<Int>
    suspend fun setNotifyDaysBefore(days: Int)

    /**
     * This gives all dismissed shopping suggestions.
     */
    fun observeDismissedSuggestions(): Flow<Map<String, Long>>
    fun observeQuietHoursStart(): Flow<Int>
    suspend fun setQuietHoursStart(hour: Int)

    /**
     * This saves the time when a suggestion was dismissed.
     *
     * category means the food category, for example Meat or Dairy.
     */
    suspend fun markSuggestionDismissed(
        category: String,
        dismissedAt: Long = System.currentTimeMillis()
    )
    fun observeQuietHoursEnd(): Flow<Int>
    suspend fun setQuietHoursEnd(hour: Int)

    fun observeOnboardingCompleted(): Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)

    fun observeUserName(): Flow<String>
    suspend fun setUserName(name: String)

    /**
     * Master switch for the periodic SensorTickWorker. When `false`, the
     * 15-minute background sweep that ingests CSV sensor readings and
     * chains a ShelfLifeRecalcWorker is cancelled — the inventory still
     * works but `effectiveExpiryDate` will only change when the user
     * edits a food directly. Defaults to `true` so the context-aware
     * demo runs out of the box.
     */
    fun observeSensorMonitoringEnabled(): Flow<Boolean>
    suspend fun setSensorMonitoringEnabled(enabled: Boolean)
}
