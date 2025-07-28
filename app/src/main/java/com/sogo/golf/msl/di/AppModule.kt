package com.sogo.golf.msl.di

import com.sogo.golf.msl.app.lifecycle.AppLifecycleManager
import com.sogo.golf.msl.domain.repository.remote.AuthRepository
import com.sogo.golf.msl.domain.usecase.date.ResetStaleDataUseCase
import com.sogo.golf.msl.domain.usecase.date.ValidateGameDataFreshnessUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppLifecycleManager(
        validateGameDataFreshnessUseCase: ValidateGameDataFreshnessUseCase,
        resetStaleDataUseCase: ResetStaleDataUseCase,
        authRepository: AuthRepository
    ): AppLifecycleManager = AppLifecycleManager(
        validateGameDataFreshnessUseCase,
        resetStaleDataUseCase,
        authRepository
    )
}