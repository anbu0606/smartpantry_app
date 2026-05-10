package com.lab03g10.smartpantry.domain.repo

import com.lab03g10.smartpantry.data.local.entity.WasteLogEntity
import com.lab03g10.smartpantry.domain.model.CategoryTotal
import com.lab03g10.smartpantry.domain.model.MonthlyTotal
import kotlinx.coroutines.flow.Flow


/**
 * @(#) WasteRepository.kt 1.0 2026-05-10
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
interface WasteRepository {

    fun observeAll(): Flow<List<WasteLogEntity>>

    fun observeMonthlyTotals(): Flow<List<MonthlyTotal>>

    fun observeByCategory(): Flow<List<CategoryTotal>>

    suspend fun log(entry: WasteLogEntity)

}