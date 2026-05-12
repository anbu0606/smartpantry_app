package com.lab03g10.smartpantry.data.repository

import com.lab03g10.smartpantry.data.local.dao.WasteLogDao
import com.lab03g10.smartpantry.data.local.entity.WasteLogEntity
import com.lab03g10.smartpantry.domain.model.CategoryTotal
import com.lab03g10.smartpantry.domain.model.MonthlyTotal
import com.lab03g10.smartpantry.domain.repo.WasteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


/**
 * @(#) WasteRepositoryImpl.kt 1.0 2026-05-10
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
@Singleton
class WasteRepositoryImpl @Inject constructor (
    private val dao: WasteLogDao,
) : WasteRepository {
    override fun observeAll(): Flow<List<WasteLogEntity>> {
        return dao.observeAll()
    }

    override fun observeMonthlyTotals(): Flow<List<MonthlyTotal>> {
        return dao.observeMonthlyTotals()
    }

    override fun observeByCategory(): Flow<List<CategoryTotal>> {
        return dao.observeByCategory()
    }

    override suspend fun log(entry: WasteLogEntity) {
        dao.insert(entry)
    }

}