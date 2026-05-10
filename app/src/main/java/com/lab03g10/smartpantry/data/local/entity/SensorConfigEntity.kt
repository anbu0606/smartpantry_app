package com.lab03g10.smartpantry.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @(#) SensorConfigEntity.kt 1.0 2026-05-10
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
@Entity(tableName = "sensors")
data class SensorConfigEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val deviceId: String,
    // EnvironmentType.name — stored as String to keep DB human-readable
    val environmentType: String,
    val thresholdC: Float,
    val maxLux: Float,
    // Filename inside assets/sensors/ (e.g. "fridge.csv") — drives the simulator
    val csvAssetName: String,
    val pairedAt: Long,
    // Soft-delete flag; rows are hidden, not removed
    val active: Boolean = true,
)
