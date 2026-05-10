package com.lab03g10.smartpantry.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.lab03g10.smartpantry.data.local.entity.SensorConfigEntity
import kotlinx.coroutines.flow.Flow

/**
 * @(#) SensorConfigDao.kt 1.0 2026-05-10
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
@Dao
interface SensorConfigDao {

    @Query("SELECT * FROM sensors WHERE active = 1 ORDER BY pairedAt DESC")
    fun observeAll(): Flow<List<SensorConfigEntity>>

    @Query("SELECT * FROM sensors WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<SensorConfigEntity?>

    @Upsert
    suspend fun upsert(config: SensorConfigEntity)

    // Soft delete: rows stay in the table but are filtered out by observeAll
    @Query("UPDATE sensors SET active = 0 WHERE id = :id")
    suspend fun delete(id: String)
}
