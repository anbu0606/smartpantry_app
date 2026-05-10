package com.lab03g10.smartpantry.domain.repo

import com.lab03g10.smartpantry.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow


/**
 * @(#) NotificationRepository.kt 1.0 2026-05-10
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
interface NotificationRepository {

    fun observeAll(): Flow<List<NotificationEntity>>

    fun observeUnreadCount(): Flow<Int>

    suspend fun markRead(id: String)

    suspend fun insert(notif: NotificationEntity)

}