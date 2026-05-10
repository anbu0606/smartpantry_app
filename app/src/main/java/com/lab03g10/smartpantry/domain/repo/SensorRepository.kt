package com.lab03g10.smartpantry.domain.repo

import com.lab03g10.smartpantry.data.local.entity.SensorConfigEntity
import com.lab03g10.smartpantry.data.local.entity.SensorReadingEntity
import kotlinx.coroutines.flow.Flow


/**
 * @(#) SensorRepository.kt 1.0 2026-05-10
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
interface SensorRepository {

    fun observeConfigs(): Flow<List<SensorConfigEntity>>

    fun observeConfigById(id: String): Flow<SensorConfigEntity?>

    suspend fun upsertConfig(config: SensorConfigEntity)

    suspend fun deleteConfig(id: String)

    fun observeLatestPerSensor(): Flow<List<SensorReadingEntity>>

    fun observeReadingsForSensorSince(sensorId: String, since: Long): Flow<List<SensorReadingEntity>>

    suspend fun insertReading(reading: SensorReadingEntity)

    suspend fun pruneReadingsOlderThan(cutoff: Long)

}