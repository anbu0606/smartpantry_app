package com.lab03g10.smartpantry.di

import com.lab03g10.smartpantry.worker.ShelfLifeRecalcScheduler
import com.lab03g10.smartpantry.worker.ShelfLifeRecalcSchedulerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkerModule {

    @Binds
    @Singleton
    abstract fun bindShelfLifeRecalcScheduler(
        impl: ShelfLifeRecalcSchedulerImpl,
    ): ShelfLifeRecalcScheduler
}
