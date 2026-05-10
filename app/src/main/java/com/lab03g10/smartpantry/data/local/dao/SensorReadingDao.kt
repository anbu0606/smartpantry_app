package com.lab03g10.smartpantry.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lab03g10.smartpantry.data.local.entity.SensorReadingEntity
import kotlinx.coroutines.flow.Flow

/**
 * @(#) SensorReadingDao.kt 1.0 2026-05-10
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
@Dao
interface SensorReadingDao {

    // Newest reading per sensor; subquery picks MAX(id) per sensorId group
    @Query(
        "SELECT * FROM sensor_readings " +
            "WHERE id IN (SELECT MAX(id) FROM sensor_readings GROUP BY sensorId)"
    )
    fun observeLatestPerSensor(): Flow<List<SensorReadingEntity>>

    @Query(
        "SELECT * FROM sensor_readings " +
            "WHERE sensorId = :sensorId AND timestamp >= :since " +
            "ORDER BY timestamp ASC"
    )
    fun observeForSensorSince(sensorId: String, since: Long): Flow<List<SensorReadingEntity>>

    @Insert
    suspend fun insert(reading: SensorReadingEntity)

    @Query("DELETE FROM sensor_readings WHERE timestamp < :cutoff")
    suspend fun pruneOlderThan(cutoff: Long)
}
