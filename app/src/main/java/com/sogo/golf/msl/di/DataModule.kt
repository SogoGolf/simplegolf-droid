// app/src/main/java/com/sogo/golf/msl/di/DataModule.kt
package com.sogo.golf.msl.di

import com.sogo.golf.msl.data.local.preferences.AuthPreferences
import com.sogo.golf.msl.data.local.preferences.AuthPreferencesImpl
import com.sogo.golf.msl.data.local.preferences.ClubPreferences
import com.sogo.golf.msl.data.local.preferences.ClubPreferencesImpl
import com.sogo.golf.msl.data.local.preferencesdata.GameDataTimestampPreferences
import com.sogo.golf.msl.data.local.preferencesdata.GameDataTimestampPreferencesImpl
import com.sogo.golf.msl.data.repository.AuthRepositoryImpl
import com.sogo.golf.msl.data.repository.local.FeeLocalDbRepositoryImpl
import com.sogo.golf.msl.data.repository.local.MslCompetitionLocalDbRepositoryImpl
import com.sogo.golf.msl.data.repository.local.MslGameLocalDbRepositoryImpl
import com.sogo.golf.msl.data.repository.local.MslGolferLocalDbRepositoryImpl
import com.sogo.golf.msl.data.repository.local.SogoGolferLocalDbRepositoryImpl
import com.sogo.golf.msl.data.repository.remote.MslRepositoryImpl
import com.sogo.golf.msl.data.repository.remote.SogoMongoRepositoryImpl
import com.sogo.golf.msl.domain.repository.FeeLocalDbRepository
import com.sogo.golf.msl.domain.repository.MslCompetitionLocalDbRepository
import com.sogo.golf.msl.domain.repository.MslGameLocalDbRepository
import com.sogo.golf.msl.domain.repository.MslGolferLocalDbRepository
import com.sogo.golf.msl.domain.repository.SogoGolferLocalDbRepository
import com.sogo.golf.msl.domain.repository.remote.AuthRepository
import com.sogo.golf.msl.domain.repository.remote.MslRepository
import com.sogo.golf.msl.domain.repository.remote.SogoMongoRepository
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
    abstract fun bindClubPreferences(
        clubPreferencesImpl: ClubPreferencesImpl
    ): ClubPreferences

    @Binds
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    abstract fun bindMslCompetitionLocalDbRepository(
        mslCompetitionLocalDbRepositoryImpl: MslCompetitionLocalDbRepositoryImpl
    ): MslCompetitionLocalDbRepository

    @Binds
    abstract fun bindMslGameLocalDbRepository( // NEW: Bind game repository
        mslGameLocalDbRepositoryImpl: MslGameLocalDbRepositoryImpl
    ): MslGameLocalDbRepository

    @Binds
    abstract fun bindMslRepository(
        mslRepositoryImpl: MslRepositoryImpl
    ): MslRepository

    @Binds
    abstract fun bindMslGolferLocalDbRepository(
        mslGolferLocalDbRepositoryImpl: MslGolferLocalDbRepositoryImpl
    ): MslGolferLocalDbRepository

    @Binds
    abstract fun bindGameDataTimestampPreferences(
        gameDataTimestampPreferencesImpl: GameDataTimestampPreferencesImpl
    ): GameDataTimestampPreferences

    @Binds
    abstract fun bindFeeLocalDbRepository(
        feeLocalDbRepositoryImpl: FeeLocalDbRepositoryImpl
    ): FeeLocalDbRepository

    @Binds
    abstract fun bindSogoMongoRepository(
        sogoMongoRepositoryImpl: SogoMongoRepositoryImpl
    ): SogoMongoRepository

    @Binds
    abstract fun bindSogoGolferLocalDbRepository(
        sogoGolferLocalDbRepositoryImpl: SogoGolferLocalDbRepositoryImpl
    ): SogoGolferLocalDbRepository

}