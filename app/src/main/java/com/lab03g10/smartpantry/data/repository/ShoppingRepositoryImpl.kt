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
class ShoppingRepositoryImpl @Inject constructor (
    private val dao: ShoppingItemDao,
) : ShoppingRepository {
    override fun observeAll(): Flow<List<ShoppingItemEntity>> {
        TODO("Not yet implemented")
    }

    override suspend fun upsert(item: ShoppingItemEntity) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun toggle(id: String) {
        TODO("Not yet implemented")
    }
}