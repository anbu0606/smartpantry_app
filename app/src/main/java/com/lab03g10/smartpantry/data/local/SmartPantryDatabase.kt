package com.lab03g10.smartpantry.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lab03g10.smartpantry.data.local.dao.*
import com.lab03g10.smartpantry.data.local.entity.*


/**
 * @(#) SmartPantryDatabase.kt 1.0 2026-05-10
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
@Database(
    entities = [
        FoodItemEntity::class,
        SensorConfigEntity::class,
        SensorReadingEntity::class,
        WasteLogEntity::class,
        NotificationEntity::class,
        ShoppingItemEntity::class,
    ],
    // v2 (2026-05-19): added SensorConfigEntity.humidityThreshold.
    // v3 (2026-05-19): no schema change vs v2 — bumped to retrigger
    //   fallbackToDestructiveMigration. v2 shipped without an
    //   onDestructiveMigration callback in DatabaseModule, so the seed
    //   sensors were never inserted on the v1→v2 destructive migration
    //   (onCreate only fires on FIRST creation, not on destructive recreate).
    //   v3 + the new onDestructiveMigration hook re-seeds correctly.
    // v4 (2026-05-19): ShoppingItemEntity gained `unit` and
    //   `nominalExpiryDate` so the shopping row carries inventory-bound
    //   metadata; FAB ✓ now transfers checked rows into food_items.
    // v5 (2026-05-19): ShoppingItemEntity gained `storageLocationId`
    //   (FK→sensors, SET_NULL) so the transferred FoodItemEntity is
    //   assigned to a sensor zone and ShelfLifeRecalcWorker can adjust
    //   its effectiveExpiryDate from sensor history.
    version = 5,
    // matches the ksp { arg("room.schemaLocation", ...) } from Day 1
    exportSchema = true,
)
abstract class SmartPantryDatabase : RoomDatabase() {
    abstract fun foodItemDao(): FoodItemDao
    abstract fun sensorConfigDao(): SensorConfigDao
    abstract fun sensorReadingDao(): SensorReadingDao
    abstract fun wasteLogDao(): WasteLogDao
    abstract fun notificationDao(): NotificationDao
    abstract fun shoppingItemDao(): ShoppingItemDao
}