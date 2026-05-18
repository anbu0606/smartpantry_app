package com.lab03g10.smartpantry.domain.repo

import com.lab03g10.smartpantry.data.local.entity.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow


/**
 * @(#) ShoppingRepository.kt 1.0 2026-05-10
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
interface ShoppingRepository {

    fun observeAll(): Flow<List<ShoppingItemEntity>>

    suspend fun upsert(item: ShoppingItemEntity)

    suspend fun delete(id: String)

    suspend fun toggle(id: String)

    suspend fun updateQuantity(id: String, quantity: String)

    suspend fun updateUnit(id: String, unit: String)

    suspend fun updateExpiry(id: String, nominalExpiryDate: Long)

    suspend fun updateStorage(id: String, storageLocationId: String?)

}