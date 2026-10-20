package com.lab03g10.smartpantry.data.repository

import com.lab03g10.smartpantry.data.local.dao.FoodItemDao
import com.lab03g10.smartpantry.data.local.entity.FoodItemEntity
import com.lab03g10.smartpantry.domain.repo.FoodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


/**
 * @(#) FoodRepositoryImpl.kt 1.0 2026-05-10
 * Copyright (c) 2026 the SmartPantry team.
 */
@Singleton
class FoodRepositoryImpl @Inject constructor(
    private val dao: FoodItemDao,
) : FoodRepository {

    override fun observeAll(): Flow<List<FoodItemEntity>> =
        dao.observeAll()

    override fun observeUseToday(today: Long): Flow<List<FoodItemEntity>> =
        dao.observeUseToday(today)

    override fun observeByCategory(category: String): Flow<List<FoodItemEntity>> =
        dao.observeByCategory(category)

    override fun observeById(id: String): Flow<FoodItemEntity?> =
        dao.observeById(id)

    override suspend fun findByBarcode(barcode: String): FoodItemEntity? =
        dao.findByBarcode(barcode)

    override suspend fun upsert(item: FoodItemEntity) =
        dao.upsert(item)

    override suspend fun delete(id: String) =
        dao.delete(id)

    override suspend fun markConsumed(id: String) =
        dao.markConsumed(id, at = System.currentTimeMillis())

    override suspend fun markWasted(id: String) =
        dao.markWasted(id, at = System.currentTimeMillis())

    override suspend fun findActiveByStorageLocation(sensorId: String) =
        dao.findActiveByStorageLocation(sensorId)

    override suspend fun updateEffectiveExpiry(id: String, effectiveExpiryDate: Long) =
        dao.updateEffectiveExpiry(id, effectiveExpiryDate, System.currentTimeMillis())

    override suspend fun findExpiringBetween(startEpoch: Long, endEpoch: Long): List<FoodItemEntity> =
        dao.findExpiringBetween(startEpoch, endEpoch)
}