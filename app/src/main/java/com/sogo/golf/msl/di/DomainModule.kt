package com.sogo.golf.msl.di

import com.sogo.golf.msl.domain.repository.AuthRepository
import com.sogo.golf.msl.domain.usecase.auth.GetAuthStateUseCase
import com.sogo.golf.msl.domain.usecase.auth.LoginUseCase
import com.sogo.golf.msl.domain.usecase.auth.LogoutUseCase
import com.sogo.golf.msl.domain.usecase.auth.SetFinishedRoundUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object DomainModule {

    @Provides
    fun provideLoginUseCase(
        authRepository: AuthRepository
    ): LoginUseCase = LoginUseCase(authRepository)

    @Provides
    fun provideLogoutUseCase(
        authRepository: AuthRepository
    ): LogoutUseCase = LogoutUseCase(authRepository)

    @Provides
    fun provideGetAuthStateUseCase(
        authRepository: AuthRepository
    ): GetAuthStateUseCase = GetAuthStateUseCase(authRepository)

    @Provides
    fun provideSetFinishedRoundUseCase(
        authRepository: AuthRepository
    ): SetFinishedRoundUseCase = SetFinishedRoundUseCase(authRepository)
}