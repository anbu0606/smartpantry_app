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
 * Copyright (c) 2026 the SmartPantry team.
 */
@Singleton
class WasteRepositoryImpl @Inject constructor(
    private val dao: WasteLogDao,
) : WasteRepository {

    override fun observeAll(): Flow<List<WasteLogEntity>> =
        dao.observeAll()

    override fun observeMonthlyTotals(): Flow<List<MonthlyTotal>> =
        dao.observeMonthlyTotals()

    override fun observeByCategory(): Flow<List<CategoryTotal>> =
        dao.observeByCategory()

    // Repo method named `log` for semantic clarity at call sites; delegates to DAO's `insert`
    override suspend fun log(entry: WasteLogEntity) =
        dao.insert(entry)
}