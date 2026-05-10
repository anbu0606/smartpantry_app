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
    version = 1,
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