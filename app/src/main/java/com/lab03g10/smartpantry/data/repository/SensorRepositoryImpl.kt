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

    override fun observeConfigs(): Flow<List<SensorConfigEntity>> =
        configDao.observeAll()

    override fun observeConfigById(id: String): Flow<SensorConfigEntity?> =
        configDao.observeById(id)

    override suspend fun upsertConfig(config: SensorConfigEntity) =
        configDao.upsert(config)

    override suspend fun deleteConfig(id: String) =
        configDao.delete(id)

    override fun observeLatestPerSensor(): Flow<List<SensorReadingEntity>> =
        readingDao.observeLatestPerSensor()

    override fun observeReadingsForSensorSince(
        sensorId: String,
        since: Long,
    ): Flow<List<SensorReadingEntity>> =
        readingDao.observeForSensorSince(sensorId, since)

    override suspend fun insertReading(reading: SensorReadingEntity) =
        readingDao.insert(reading)

    override suspend fun pruneReadingsOlderThan(cutoff: Long) =
        readingDao.pruneOlderThan(cutoff)

    override suspend fun getReadingsForSensorSince(sensorId: String, since: Long) =
        readingDao.getReadingsForSensorSince(sensorId, since)
}