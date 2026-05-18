package com.lab03g10.smartpantry.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @(#) ShoppingItemEntity.kt 1.0 2026-05-10
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
@Entity(
    tableName = "shopping",
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
data class ShoppingItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val quantity: String,
    // One of: g / kg / ml / L / pcs (matches FoodItemEntity)
    val unit: String,
    val category: String,
    val checked: Boolean = false,
    val addedAt: Long,
    // Default expiry carried over to inventory when the user marks the
    // item as purchased. Editable from the shopping list row.
    val nominalExpiryDate: Long,
    // Pre-purchase storage assignment; copied into FoodItemEntity on
    // transfer so ShelfLifeRecalcWorker can pick up the item. null =
    // "Unmonitored" (also the FK SET_NULL fallback if the sensor is
    // deleted before purchase).
    val storageLocationId: String?,
    // true if added by the waste-log-driven auto-suggest, not the user
    val autoSuggested: Boolean = false,
)
