package com.lab03g10.smartpantry.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lab03g10.smartpantry.domain.repo.PreferenceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/*
 * DataStore is used to save small app preference values.
 *
 * In this file, we use it to remember shopping suggestions that the user
 * already dismissed. This helps stop the same suggestion from
 * appearing again immediately after app restart.
 */
private val Context.smartPantryDataStore by preferencesDataStore(
    name = "smart_pantry_preferences"
)

@Singleton
class PreferenceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferenceRepository {

    companion object {
        /*
         * DataStore needs a key for each saved value.
         *
         * We store all dismissed suggestions in one String.
         */
        private val SUGGESTION_DISMISSALS =
            stringPreferencesKey("suggestion_dismissals")
    }

    private object PreferencesKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val NOTIFY_DAYS_BEFORE = intPreferencesKey("notify_days_before")
        val QUIET_HOURS_START = intPreferencesKey("quiet_hours_start")
        val QUIET_HOURS_END = intPreferencesKey("quiet_hours_end")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val USER_NAME = stringPreferencesKey("user_name")
    }

    /*
     * This function reads dismissed suggestions from DataStore.
     *
     * It returns a Map:
     * category name -> dismissed time
     */
    override fun observeDismissedSuggestions(): Flow<Map<String, Long>> {
        return context.smartPantryDataStore.data.map { preferences ->

            val savedText = preferences[SUGGESTION_DISMISSALS]

            if (savedText == null) {
                emptyMap()
            } else {
                textToDismissedMap(savedText)
            }
        }
    }

    /*
     * This function saves a category as dismissed.
     *
     * Example:
     * If the user accepts "Restock Meat", we save Meat with the current time.
     */
    override suspend fun markSuggestionDismissed(
        category: String,
        dismissedAt: Long
    ) {
        context.smartPantryDataStore.edit { preferences ->

            val savedText = preferences[SUGGESTION_DISMISSALS]

            val currentMap = if (savedText == null) {
                emptyMap()
            } else {
                textToDismissedMap(savedText)
            }

            val updatedMap = currentMap + (category to dismissedAt)

            preferences[SUGGESTION_DISMISSALS] = dismissedMapToText(updatedMap)
        }
    }

    /*
     * This changes saved text back into a Map.
     */
    private fun textToDismissedMap(savedText: String): Map<String, Long> {
        val result = mutableMapOf<String, Long>()

        val textItems = savedText.split(";")

        for (textItem in textItems) {
            if (textItem.isNotBlank()) {
                val parts = textItem.split("=")

                if (parts.size == 2) {
                    val category = parts[0]
                    val dismissedTime = parts[1].toLongOrNull()

                    if (category.isNotBlank() && dismissedTime != null) {
                        result[category] = dismissedTime
                    }
                }
            }
        }

        return result
    }

    /*
     * This changes a Map into text so DataStore can save it.
    */
    private fun dismissedMapToText(dismissedMap: Map<String, Long>): String {
        val textItems = mutableListOf<String>()

        for (entry in dismissedMap.entries) {
            val category = entry.key
            val dismissedTime = entry.value

            textItems.add("$category=$dismissedTime")
        }

        return textItems.joinToString(";")
    }

    override fun observeIsDarkMode(): Flow<Boolean> = context.smartPantryDataStore.data.map { preferences ->
        preferences[PreferencesKeys.DARK_MODE] ?: false
    }

    override suspend fun setDarkMode(enabled: Boolean) {
        context.smartPantryDataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = enabled
        }
    }

    override fun observeNotifyDaysBefore(): Flow<Int> = context.smartPantryDataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFY_DAYS_BEFORE] ?: 3 // Default 3 days
    }

    override suspend fun setNotifyDaysBefore(days: Int) {
        context.smartPantryDataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFY_DAYS_BEFORE] = days
        }
    }

    override fun observeQuietHoursStart(): Flow<Int> = context.smartPantryDataStore.data.map { preferences ->
        preferences[PreferencesKeys.QUIET_HOURS_START] ?: 22 // Default 10 PM
    }

    override suspend fun setQuietHoursStart(hour: Int) {
        context.smartPantryDataStore.edit { preferences ->
            preferences[PreferencesKeys.QUIET_HOURS_START] = hour
        }
    }

    override fun observeQuietHoursEnd(): Flow<Int> = context.smartPantryDataStore.data.map { preferences ->
        preferences[PreferencesKeys.QUIET_HOURS_END] ?: 8 // Default 8 AM
    }

    override suspend fun setQuietHoursEnd(hour: Int) {
        context.smartPantryDataStore.edit { preferences ->
            preferences[PreferencesKeys.QUIET_HOURS_END] = hour
        }
    }

    override fun observeOnboardingCompleted(): Flow<Boolean> = context.smartPantryDataStore.data.map { preferences ->
        preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.smartPantryDataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    override fun observeUserName(): Flow<String> = context.smartPantryDataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_NAME] ?: ""
    }

    override suspend fun setUserName(name: String) {
        context.smartPantryDataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
        }
    }
}
