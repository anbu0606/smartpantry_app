package com.lab03g10.smartpantry.data.repository

import com.lab03g10.smartpantry.data.local.dao.NotificationDao
import com.lab03g10.smartpantry.data.local.entity.NotificationEntity
import com.lab03g10.smartpantry.domain.repo.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


/**
 * @(#) NotificationRepositoryImpl.kt 1.0 2026-05-10
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val dao: NotificationDao,
) : NotificationRepository {

    override fun observeAll(): Flow<List<NotificationEntity>> =
        dao.observeAll()

    override fun observeUnreadCount(): Flow<Int> =
        dao.observeUnreadCount()

    override suspend fun markRead(id: String) =
        dao.markRead(id)

    override suspend fun insert(notif: NotificationEntity) =
        dao.insert(notif)
}