package com.lab03g10.smartpantry.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lab03g10.smartpantry.data.local.entity.WasteLogEntity
import com.lab03g10.smartpantry.domain.model.CategoryTotal
import com.lab03g10.smartpantry.domain.model.MonthlyTotal
import kotlinx.coroutines.flow.Flow

/**
 * @(#) WasteLogDao.kt 1.0 2026-05-10
 * Copyright (c) 2026 the SmartPantry team.
 */
@Dao
interface WasteLogDao {

    @Query("SELECT * FROM waste_log ORDER BY wastedAt DESC")
    fun observeAll(): Flow<List<WasteLogEntity>>

    @Query(
        "SELECT strftime('%Y-%m', datetime(wastedAt / 1000, 'unixepoch')) AS yearMonth, " +
            "COUNT(*) AS count, " +
            "SUM(co2eGrams) AS co2eGramsTotal " +
            "FROM waste_log " +
            "GROUP BY yearMonth " +
            "ORDER BY yearMonth ASC"
    )
    fun observeMonthlyTotals(): Flow<List<MonthlyTotal>>

    @Query(
        "SELECT category, COUNT(*) AS count, SUM(co2eGrams) AS co2eGramsTotal " +
            "FROM waste_log " +
            "GROUP BY category " +
            "ORDER BY co2eGramsTotal DESC"
    )
    fun observeByCategory(): Flow<List<CategoryTotal>>

    @Insert
    suspend fun insert(entry: WasteLogEntity)
}
