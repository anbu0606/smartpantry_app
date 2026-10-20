package com.lab03g10.smartpantry.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @(#) WasteLogEntity.kt 1.0 2026-05-10
 * Copyright (c) 2026 the SmartPantry team.
 */
@Entity(tableName = "waste_log")
data class WasteLogEntity(
    @PrimaryKey val id: String,
    // No FK — log must outlive the FoodItem row so history survives deletion
    val foodItemId: String?,
    // Snapshot of food at disposal time; intentionally duplicated from FoodItem
    val name: String,
    val category: String,
    val quantity: String,
    val wastedAt: Long,
    // One of: "EXPIRED" / "SPOILED" / "OTHER"
    val reason: String,
    // Estimated CO2-equivalent grams, looked up from a static category table
    val co2eGrams: Float,
)
