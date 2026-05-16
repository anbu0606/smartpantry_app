package com.lab03g10.smartpantry.domain.repo

import kotlinx.coroutines.flow.Flow

/**
 * This repository stores small user preference values.
 *
 * For shopping suggestions, it remembers which category suggestion
 * the user already accepted or dismissed.
 */
interface PreferenceRepository {

    /**
     * This gives all dismissed shopping suggestions.
     */
    fun observeDismissedSuggestions(): Flow<Map<String, Long>>

    /**
     * This saves the time when a suggestion was dismissed.
     *
     * category means the food category, for example Meat or Dairy.
     */
    suspend fun markSuggestionDismissed(
        category: String,
        dismissedAt: Long = System.currentTimeMillis()
    )
}
