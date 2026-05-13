package com.lab03g10.smartpantry.domain.repo

import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {
    fun observeIsDarkMode(): Flow<Boolean>
    suspend fun setDarkMode(enabled: Boolean)

    fun observeNotifyDaysBefore(): Flow<Int>
    suspend fun setNotifyDaysBefore(days: Int)

    fun observeQuietHoursStart(): Flow<Int>
    suspend fun setQuietHoursStart(hour: Int)

    fun observeQuietHoursEnd(): Flow<Int>
    suspend fun setQuietHoursEnd(hour: Int)

    fun observeOnboardingCompleted(): Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)

    fun observeUserName(): Flow<String>
    suspend fun setUserName(name: String)
}
