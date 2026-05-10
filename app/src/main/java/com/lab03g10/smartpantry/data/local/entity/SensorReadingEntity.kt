package com.lab03g10.smartpantry.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * @(#) SensorReadingEntity.kt 1.0 2026-05-10
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
@Entity(
    tableName = "sensor_readings",
    foreignKeys = [
        ForeignKey(
            entity = SensorConfigEntity::class,
            parentColumns = ["id"],
            childColumns = ["sensorId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class SensorReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(index = true) val sensorId: String,
    val timestamp: Long,
    val temperatureC: Float,
    val humidity: Float,
    val lux: Float,
    // Bitset: bit0 = temp, bit1 = humidity, bit2 = lux. 0 = clean reading
    val breachFlag: Int,
)
