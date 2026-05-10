package com.lab03g10.smartpantry.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lab03g10.smartpantry.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

/**
 * @(#) NotificationDao.kt 1.0 2026-05-10
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE read = 0")
    fun observeUnreadCount(): Flow<Int>

    @Query("UPDATE notifications SET read = 1 WHERE id = :id")
    suspend fun markRead(id: String)

    @Insert
    suspend fun insert(notif: NotificationEntity)
}
