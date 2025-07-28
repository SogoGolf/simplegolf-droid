// Update to app/src/main/java/com/sogo/golf/msl/di/DomainModule.kt
package com.sogo.golf.msl.di

import com.sogo.golf.msl.data.local.preferences.ClubPreferences
import com.sogo.golf.msl.data.local.preferencesdata.GameDataTimestampPreferences
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
import com.sogo.golf.msl.domain.usecase.date.ResetStaleDataUseCase
import com.sogo.golf.msl.domain.usecase.date.SaveGameDataDateUseCase
import com.sogo.golf.msl.domain.usecase.date.ValidateGameDataFreshnessUseCase
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
    fun provideGetLocalCompetitionUseCase(
        competitionRepository: MslCompetitionLocalDbRepository
    ): GetLocalCompetitionUseCase = GetLocalCompetitionUseCase(competitionRepository)

    // ✅ Date UseCases (provide this first since others depend on it)
    @Provides
    fun provideSaveGameDataDateUseCase(
        gameDataTimestampPreferences: GameDataTimestampPreferences
    ): SaveGameDataDateUseCase = SaveGameDataDateUseCase(gameDataTimestampPreferences)

    // ✅ Updated fetch use cases with date saving capability
    @Provides
    fun provideFetchAndSaveGameUseCase(
        gameRepository: MslGameLocalDbRepository,
        saveGameDataDateUseCase: SaveGameDataDateUseCase
    ): FetchAndSaveGameUseCase = FetchAndSaveGameUseCase(gameRepository, saveGameDataDateUseCase)

    @Provides
    fun provideFetchAndSaveCompetitionUseCase(
        competitionRepository: MslCompetitionLocalDbRepository,
        saveGameDataDateUseCase: SaveGameDataDateUseCase
    ): FetchAndSaveCompetitionUseCase = FetchAndSaveCompetitionUseCase(competitionRepository, saveGameDataDateUseCase)

    // Marker UseCases
    @Provides
    fun provideSelectMarkerUseCase(
        mslRepository: MslRepository
    ): SelectMarkerUseCase = SelectMarkerUseCase(mslRepository)

    @Provides
    fun provideRemoveMarkerUseCase(
        mslRepository: MslRepository
    ): RemoveMarkerUseCase = RemoveMarkerUseCase(mslRepository)

    // ✅ Date validation use cases
    @Provides
    fun provideValidateGameDataFreshnessUseCase(
        gameDataTimestampPreferences: GameDataTimestampPreferences
    ): ValidateGameDataFreshnessUseCase = ValidateGameDataFreshnessUseCase(gameDataTimestampPreferences)

    @Provides
    fun provideResetStaleDataUseCase(
        mslGameLocalDbRepository: MslGameLocalDbRepository,
        mslCompetitionLocalDbRepository: MslCompetitionLocalDbRepository,
        mslGolferLocalDbRepository: MslGolferLocalDbRepository,
        fetchAndSaveGameUseCase: FetchAndSaveGameUseCase,
        fetchAndSaveCompetitionUseCase: FetchAndSaveCompetitionUseCase,
        getMslClubAndTenantIdsUseCase: GetMslClubAndTenantIdsUseCase,
        gameDataTimestampPreferences: GameDataTimestampPreferences,
        authRepository: AuthRepository
    ): ResetStaleDataUseCase = ResetStaleDataUseCase(
        mslGameLocalDbRepository,
        mslCompetitionLocalDbRepository,
        mslGolferLocalDbRepository,
        fetchAndSaveGameUseCase,
        fetchAndSaveCompetitionUseCase,
        getMslClubAndTenantIdsUseCase,
        gameDataTimestampPreferences,
        authRepository
    )
}