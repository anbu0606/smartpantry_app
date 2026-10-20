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
 * Copyright (c) 2026 the SmartPantry team.
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
        Room.databaseBuilder(context, SmartPantryDatabase::class.java, "smart_pantry.db")
            .addCallback(object : RoomDatabase.Callback() {
                // onCreate fires once, when the DB is first created.
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    seedDefaultSensors(db)
                }

                // onDestructiveMigration fires when fallbackToDestructiveMigration
                // runs (e.g. after a schema bump). Room does NOT call onCreate
                // in that path — earlier we hit this bug after v1→v2 and the
                // DB was left without seed sensors. Re-running the same seed
                // here keeps the demo data alive across destructive recreates.
                override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                    super.onDestructiveMigration(db)
                    seedDefaultSensors(db)
                }
            })
            .fallbackToDestructiveMigration()
            .build()

    /**
     * Inserts the three demo sensors using `INSERT OR IGNORE` so an existing
     * row with the same PK survives intact — a user-customised threshold on
     * `sensor-fridge` won't be clobbered if this seed runs again. Named
     * columns (not positional) so future schema additions don't silently
     * corrupt the values.
     */
    private fun seedDefaultSensors(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()
        db.execSQL("""
            INSERT OR IGNORE INTO sensors
              (id, displayName, deviceId, environmentType,
               thresholdC, humidityThreshold, maxLux,
               csvAssetName, pairedAt, active)
            VALUES
              ('sensor-fridge',  'Kitchen Fridge', 'dev-001', 'REFRIGERATOR',  7.0,  70.0,  50.0, 'fridge.csv',  $now, 1),
              ('sensor-freezer', 'Freezer',        'dev-002', 'FREEZER',     -15.0, 70.0,  50.0, 'freezer.csv', $now, 1),
              ('sensor-pantry',  'Pantry Shelf',   'dev-003', 'PANTRY',       28.0, 65.0, 300.0, 'pantry.csv',  $now, 1)
        """.trimIndent())
    }

    @Provides fun provideFoodItemDao(db: SmartPantryDatabase): FoodItemDao = db.foodItemDao()

    @Provides fun provideSensorConfigDao(db: SmartPantryDatabase): SensorConfigDao = db.sensorConfigDao()

    @Provides fun provideSensorReadingDao(db: SmartPantryDatabase): SensorReadingDao = db.sensorReadingDao()

    @Provides fun provideWasteLogDao(db: SmartPantryDatabase): WasteLogDao = db.wasteLogDao()

    @Provides fun provideNotificationDao(db: SmartPantryDatabase): NotificationDao = db.notificationDao()

    @Provides fun provideShoppingItemDao(db: SmartPantryDatabase): ShoppingItemDao = db.shoppingItemDao()
}