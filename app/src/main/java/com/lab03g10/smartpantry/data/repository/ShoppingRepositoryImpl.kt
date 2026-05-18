package com.lab03g10.smartpantry.data.repository

import com.lab03g10.smartpantry.data.local.dao.ShoppingItemDao
import com.lab03g10.smartpantry.data.local.entity.ShoppingItemEntity
import com.lab03g10.smartpantry.domain.repo.ShoppingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


/**
 * @(#) ShoppingRepositoryImpl.kt 1.0 2026-05-10
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
@Singleton
class ShoppingRepositoryImpl @Inject constructor(
    private val dao: ShoppingItemDao,
) : ShoppingRepository {

    override fun observeAll(): Flow<List<ShoppingItemEntity>> =
        dao.observeAll()

    override suspend fun upsert(item: ShoppingItemEntity) =
        dao.upsert(item)

    override suspend fun delete(id: String) =
        dao.delete(id)

    override suspend fun toggle(id: String) =
        dao.toggle(id)

    override suspend fun updateQuantity(id: String, quantity: String) =
        dao.updateQuantity(id, quantity)

    override suspend fun updateUnit(id: String, unit: String) =
        dao.updateUnit(id, unit)

    override suspend fun updateExpiry(id: String, nominalExpiryDate: Long) =
        dao.updateExpiry(id, nominalExpiryDate)

    override suspend fun updateStorage(id: String, storageLocationId: String?) =
        dao.updateStorage(id, storageLocationId)
}