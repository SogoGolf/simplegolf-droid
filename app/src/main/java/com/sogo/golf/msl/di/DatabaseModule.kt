package com.sogo.golf.msl.di

import android.content.Context
import androidx.room.Room
import com.sogo.golf.msl.data.local.database.AppDatabase
import com.sogo.golf.msl.data.local.database.dao.CompetitionDao
//import com.sogo.golf.msl.data.local.database.dao.RoundDao
//import com.sogo.golf.msl.data.local.database.dao.ScoreDao
//import com.sogo.golf.msl.data.local.database.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // For now, since we're adding new entity
            .build()
    }

//    @Provides
//    fun provideUserDao(database: AppDatabase): UserDao {
//        return database.userDao()
//    }
//
//    @Provides
//    fun provideRoundDao(database: AppDatabase): RoundDao {
//        return database.roundDao()
//    }
//
//    @Provides
//    fun provideScoreDao(database: AppDatabase): ScoreDao {
//        return database.scoreDao()
//    }

    @Provides
    fun provideCompetitionDao(database: AppDatabase): CompetitionDao {
        return database.competitionDao()
    }
}