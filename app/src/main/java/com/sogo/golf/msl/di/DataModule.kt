package com.sogo.golf.msl.di

import com.sogo.golf.msl.data.local.preferences.AuthPreferences
import com.sogo.golf.msl.data.local.preferences.AuthPreferencesImpl
import com.sogo.golf.msl.data.repository.AuthRepositoryImpl
import com.sogo.golf.msl.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindAuthPreferences(
        authPreferencesImpl: AuthPreferencesImpl
    ): AuthPreferences

    @Binds
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}