package com.lab03g10.smartpantry.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.lab03g10.smartpantry.data.local.entity.FoodItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * @(#) FoodItemDao.kt 1.0 2026-05-10
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
@Dao
interface FoodItemDao {

    @Query(
        "SELECT * FROM food_items " +
            "WHERE consumed = 0 AND wasted = 0 " +
            "ORDER BY effectiveExpiryDate ASC"
    )
    fun observeAll(): Flow<List<FoodItemEntity>>

    @Query(
        "SELECT * FROM food_items " +
            "WHERE effectiveExpiryDate <= :today AND consumed = 0 AND wasted = 0 " +
            "ORDER BY effectiveExpiryDate ASC"
    )
    fun observeUseToday(today: Long): Flow<List<FoodItemEntity>>

    @Query(
        "SELECT * FROM food_items " +
            "WHERE category = :category AND consumed = 0 AND wasted = 0"
    )
    fun observeByCategory(category: String): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM food_items WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<FoodItemEntity?>

    @Query("SELECT * FROM food_items WHERE barcode = :barcode LIMIT 1")
    suspend fun findByBarcode(barcode: String): FoodItemEntity?

    @Upsert
    suspend fun upsert(item: FoodItemEntity)

    @Query("DELETE FROM food_items WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE food_items SET consumed = 1, updatedAt = :at WHERE id = :id")
    suspend fun markConsumed(id: String, at: Long)

    @Query("UPDATE food_items SET wasted = 1, updatedAt = :at WHERE id = :id")
    suspend fun markWasted(id: String, at: Long)

    @Query(
        "SELECT * FROM food_items " +
                "WHERE storageLocationId = :sensorId AND consumed = 0 AND wasted = 0"
    )
    suspend fun findActiveByStorageLocation(sensorId: String): List<FoodItemEntity>

    @Query(
        "UPDATE food_items SET effectiveExpiryDate = :effectiveExpiryDate, updatedAt = :updatedAt " +
                "WHERE id = :id"
    )
    suspend fun updateEffectiveExpiry(id: String, effectiveExpiryDate: Long, updatedAt: Long)

    @Query(
        "SELECT * FROM food_items " +
            "WHERE effectiveExpiryDate >= :startEpoch AND effectiveExpiryDate <= :endEpoch " +
            "AND consumed = 0 AND wasted = 0 " +
            "ORDER BY effectiveExpiryDate ASC"
    )
    suspend fun findExpiringBetween(startEpoch: Long, endEpoch: Long): List<FoodItemEntity>
}
