package com.lab03g10.smartpantry.data.repository

import com.lab03g10.smartpantry.data.local.dao.SensorConfigDao
import com.lab03g10.smartpantry.data.local.dao.SensorReadingDao
import com.lab03g10.smartpantry.data.local.entity.SensorConfigEntity
import com.lab03g10.smartpantry.data.local.entity.SensorReadingEntity
import com.lab03g10.smartpantry.domain.repo.SensorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


/**
 * @(#) SensorRepositoryImpl.kt 1.0 2026-05-10
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
@Singleton
class SensorRepositoryImpl @Inject constructor(
    private val configDao: SensorConfigDao,
    private val readingDao: SensorReadingDao,
) : SensorRepository {
    override fun observeConfigs(): Flow<List<SensorConfigEntity>> {
        TODO("Not yet implemented")
    }

    override fun observeConfigById(id: String): Flow<SensorConfigEntity?> {
        TODO("Not yet implemented")
    }

    override suspend fun upsertConfig(config: SensorConfigEntity) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteConfig(id: String) {
        TODO("Not yet implemented")
    }

    override fun observeLatestPerSensor(): Flow<List<SensorReadingEntity>> {
        TODO("Not yet implemented")
    }

    override fun observeReadingsForSensorSince(
        sensorId: String,
        since: Long
    ): Flow<List<SensorReadingEntity>> {
        TODO("Not yet implemented")
    }

    override suspend fun insertReading(reading: SensorReadingEntity) {
        TODO("Not yet implemented")
    }

    override suspend fun pruneReadingsOlderThan(cutoff: Long) {
        TODO("Not yet implemented")
    }

}