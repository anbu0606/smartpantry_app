package com.lab03g10.smartpantry.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lab03g10.smartpantry.data.local.SmartPantryDatabase
import com.lab03g10.smartpantry.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @(#) DatabaseModule.kt 1.0 2026-05-10
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * Hilt module providing Room database + DAOs as application-scoped singletons.
 * Pattern follows the Android Architecture Guide's "single-instance database" rule:
 * https://developer.android.com/training/data-storage/room#database
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SmartPantryDatabase =
        // In provideDatabase(@ApplicationContext context: Context):
        Room.databaseBuilder(context, SmartPantryDatabase::class.java, "smart_pantry.db")
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    val now = System.currentTimeMillis()
                    // Seed 3 default sensors so demo has data on first launch.
                    // Matches the CSV assets shipped in Day 3 (assets/sensors/*.csv).
                    db.execSQL("""
                INSERT INTO sensors VALUES
                  ('sensor-fridge',  'Kitchen Fridge',  'dev-001', 'REFRIGERATOR',   7.0,  50.0, 'fridge.csv',  $now, 1),
                  ('sensor-freezer', 'Freezer',         'dev-002', 'FREEZER',      -15.0,  50.0, 'freezer.csv', $now, 1),
                  ('sensor-pantry',  'Pantry Shelf',    'dev-003', 'PANTRY',        28.0, 300.0, 'pantry.csv',  $now, 1)
            """.trimIndent())
                }
            })
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideFoodItemDao(db: SmartPantryDatabase): FoodItemDao = db.foodItemDao()

    @Provides fun provideSensorConfigDao(db: SmartPantryDatabase): SensorConfigDao = db.sensorConfigDao()

    @Provides fun provideSensorReadingDao(db: SmartPantryDatabase): SensorReadingDao = db.sensorReadingDao()

    @Provides fun provideWasteLogDao(db: SmartPantryDatabase): WasteLogDao = db.wasteLogDao()

    @Provides fun provideNotificationDao(db: SmartPantryDatabase): NotificationDao = db.notificationDao()

    @Provides fun provideShoppingItemDao(db: SmartPantryDatabase): ShoppingItemDao = db.shoppingItemDao()
}