package com.lab03g10.smartpantry.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @(#) NetworkModule.kt 1.0 2026-05-10
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // TODO(Day 7): provide Retrofit + OkHttp + Moshi + OpenFoodFactsApi
    // For Day 2, the module exists so future @Provides methods can be added without re-wiring.
    @Provides
    @Singleton
    fun provideOpenFoodFactsBaseUrl(): String = "https://world.openfoodfacts.org/"

}