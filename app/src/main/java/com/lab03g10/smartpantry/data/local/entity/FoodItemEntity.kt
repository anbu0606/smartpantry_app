package com.lab03g10.smartpantry.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @(#) FoodItemEntity.kt 1.0 2026-05-10
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
@Entity(
    tableName = "food_items",
    foreignKeys = [
        ForeignKey(
            entity = SensorConfigEntity::class,
            parentColumns = ["id"],
            childColumns = ["storageLocationId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("storageLocationId")],
)
data class FoodItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    // One of: Dairy / Meat / Produce / Grains / Frozen / Other
    val category: String,
    val quantity: String,
    // One of: g / kg / ml / L / pcs
    val unit: String,
    val barcode: String?,
    val imageUrl: String?,
    val purchaseDate: Long,
    // Original printed expiry (epoch millis)
    val nominalExpiryDate: Long,
    // Recalculated by ShelfLifeCalculator from sensor history (epoch millis)
    val effectiveExpiryDate: Long,
    // FK to sensors.id; null = item in unmonitored storage
    val storageLocationId: String?,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val consumed: Boolean = false,
    val wasted: Boolean = false,
)