// Update to app/src/main/java/com/sogo/golf/msl/di/DomainModule.kt
package com.sogo.golf.msl.di

import com.sogo.golf.msl.data.local.preferences.ClubPreferences
import com.sogo.golf.msl.domain.repository.MslCompetitionLocalDbRepository
import com.sogo.golf.msl.domain.repository.MslGameLocalDbRepository
import com.sogo.golf.msl.domain.repository.MslGolferLocalDbRepository
import com.sogo.golf.msl.domain.repository.remote.AuthRepository
import com.sogo.golf.msl.domain.repository.remote.MslRepository
import com.sogo.golf.msl.domain.usecase.auth.GetAuthStateUseCase
import com.sogo.golf.msl.domain.usecase.auth.LoginUseCase
import com.sogo.golf.msl.domain.usecase.auth.LogoutUseCase
import com.sogo.golf.msl.domain.usecase.auth.ProcessMslAuthCodeUseCase
import com.sogo.golf.msl.domain.usecase.auth.SetFinishedRoundUseCase
import com.sogo.golf.msl.domain.usecase.club.GetMslClubAndTenantIdsUseCase
import com.sogo.golf.msl.domain.usecase.club.SetSelectedClubUseCase
import com.sogo.golf.msl.domain.usecase.competition.FetchAndSaveCompetitionUseCase
import com.sogo.golf.msl.domain.usecase.competition.GetCompetitionUseCase
import com.sogo.golf.msl.domain.usecase.competition.GetLocalCompetitionUseCase
import com.sogo.golf.msl.domain.usecase.game.FetchAndSaveGameUseCase
import com.sogo.golf.msl.domain.usecase.game.GetGameUseCase
import com.sogo.golf.msl.domain.usecase.game.GetLocalGameUseCase
import com.sogo.golf.msl.domain.usecase.marker.SelectMarkerUseCase
import com.sogo.golf.msl.domain.usecase.marker.RemoveMarkerUseCase
import com.sogo.golf.msl.domain.usecase.msl_golfer.GetMslGolferUseCase
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

    @Provides
    fun provideProcessMslAuthCodeUseCase(
        mslRepository: MslRepository,
        authRepository: AuthRepository,
        mslGolferLocalDbRepository: MslGolferLocalDbRepository
    ): ProcessMslAuthCodeUseCase = ProcessMslAuthCodeUseCase(mslRepository, authRepository, mslGolferLocalDbRepository)

    @Provides
    fun provideGetGameUseCase(
        mslRepository: MslRepository
    ): GetGameUseCase = GetGameUseCase(mslRepository)

    @Provides
    fun provideGetCurrentGolferUseCase(
        mslGolferLocalDbRepository: MslGolferLocalDbRepository
    ): GetMslGolferUseCase = GetMslGolferUseCase(mslGolferLocalDbRepository)

    @Provides
    fun provideGetCompetitionUseCase(
        mslRepository: MslRepository
    ): GetCompetitionUseCase = GetCompetitionUseCase(mslRepository)

    // Club UseCases
    @Provides
    fun provideGetMslClubAndTenantIdsUseCase(
        clubPreferences: ClubPreferences
    ): GetMslClubAndTenantIdsUseCase = GetMslClubAndTenantIdsUseCase(clubPreferences)

    @Provides
    fun provideSetSelectedClubUseCase(
        clubPreferences: ClubPreferences
    ): SetSelectedClubUseCase = SetSelectedClubUseCase(clubPreferences)

    // Game UseCases
    @Provides
    fun provideGetLocalGameUseCase(
        gameRepository: MslGameLocalDbRepository
    ): GetLocalGameUseCase = GetLocalGameUseCase(gameRepository)

    @Provides
    fun provideFetchAndSaveGameUseCase(
        gameRepository: MslGameLocalDbRepository
    ): FetchAndSaveGameUseCase = FetchAndSaveGameUseCase(gameRepository)

    @Provides
    fun provideGetLocalCompetitionUseCase(
        competitionRepository: MslCompetitionLocalDbRepository
    ): GetLocalCompetitionUseCase = GetLocalCompetitionUseCase(competitionRepository)

    @Provides
    fun provideFetchAndSaveCompetitionUseCase(
        competitionRepository: MslCompetitionLocalDbRepository
    ): FetchAndSaveCompetitionUseCase = FetchAndSaveCompetitionUseCase(competitionRepository)

    // Marker UseCases
    @Provides
    fun provideSelectMarkerUseCase(
        mslRepository: MslRepository
    ): SelectMarkerUseCase = SelectMarkerUseCase(mslRepository)

    @Provides
    fun provideRemoveMarkerUseCase(
        mslRepository: MslRepository
    ): RemoveMarkerUseCase = RemoveMarkerUseCase(mslRepository)
}