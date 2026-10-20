package com.lab03g10.smartpantry.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @(#) NotificationEntity.kt 1.0 2026-05-10
 * Copyright (c) 2026 the SmartPantry team.
 */
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    // One of: "EXPIRY_SOON" / "SENSOR_BREACH" / "SHOPPING" / "LOW_STOCK"
    val type: String,
    val title: String,
    val body: String,
    // Deep-link targets when the user taps the notification
    val relatedFoodId: String?,
    val relatedSensorId: String?,
    val createdAt: Long,
    val read: Boolean = false,
)
