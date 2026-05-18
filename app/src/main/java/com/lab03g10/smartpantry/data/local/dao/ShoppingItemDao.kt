package com.lab03g10.smartpantry.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.lab03g10.smartpantry.data.local.entity.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * @(#) ShoppingItemDao.kt 1.0 2026-05-10
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
@Dao
interface ShoppingItemDao {

    // Unchecked items first, newest within each group
    @Query("SELECT * FROM shopping ORDER BY checked ASC, addedAt DESC")
    fun observeAll(): Flow<List<ShoppingItemEntity>>

    @Upsert
    suspend fun upsert(item: ShoppingItemEntity)

    @Query("DELETE FROM shopping WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE shopping SET checked = NOT checked WHERE id = :id")
    suspend fun toggle(id: String)

    @Query("UPDATE shopping SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: String, quantity: String)

    @Query("UPDATE shopping SET unit = :unit WHERE id = :id")
    suspend fun updateUnit(id: String, unit: String)

    @Query("UPDATE shopping SET nominalExpiryDate = :nominalExpiryDate WHERE id = :id")
    suspend fun updateExpiry(id: String, nominalExpiryDate: Long)

    @Query("UPDATE shopping SET storageLocationId = :storageLocationId WHERE id = :id")
    suspend fun updateStorage(id: String, storageLocationId: String?)
}
