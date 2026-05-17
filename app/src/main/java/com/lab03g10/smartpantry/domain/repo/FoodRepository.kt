package com.lab03g10.smartpantry.domain.repo

import com.lab03g10.smartpantry.data.local.entity.FoodItemEntity
import kotlinx.coroutines.flow.Flow


/**
 * @(#) FoodRepository.kt 1.0 2026-05-10
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
interface FoodRepository {

    fun observeAll(): Flow<List<FoodItemEntity>>

    fun observeUseToday(today: Long): Flow<List<FoodItemEntity>>

    fun observeByCategory(category: String): Flow<List<FoodItemEntity>>

    fun observeById(id: String): Flow<FoodItemEntity?>

    suspend fun findByBarcode(barcode: String): FoodItemEntity?

    suspend fun upsert(item: FoodItemEntity)

    suspend fun delete(id: String)

    suspend fun markConsumed(id: String)

    suspend fun markWasted(id: String)

    /** All active (non-consumed, non-wasted) food items stored at a given sensor location. */
    suspend fun findActiveByStorageLocation(sensorId: String): List<FoodItemEntity>

    /** Targeted update: only effectiveExpiryDate + updatedAt. */
    suspend fun updateEffectiveExpiry(id: String, effectiveExpiryDate: Long)

    /** Items whose effectiveExpiryDate falls within [startEpoch, endEpoch], active only. */
    suspend fun findExpiringBetween(startEpoch: Long, endEpoch: Long): List<FoodItemEntity>
}