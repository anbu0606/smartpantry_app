package com.lab03g10.smartpantry.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @(#) ShoppingItemEntity.kt 1.0 2026-05-10
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
@Entity(tableName = "shopping")
data class ShoppingItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val quantity: String,
    val category: String,
    val checked: Boolean = false,
    val addedAt: Long,
    // true if added by the waste-log-driven auto-suggest, not the user
    val autoSuggested: Boolean = false,
)
