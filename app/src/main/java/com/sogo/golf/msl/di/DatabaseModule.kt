package com.sogo.golf.msl.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sogo.golf.msl.data.local.database.AppDatabase
import com.sogo.golf.msl.data.local.database.dao.CompetitionDao
import com.sogo.golf.msl.data.local.database.dao.MslGolferDao
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
        Log.d("DatabaseModule", "Creating database...")

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration(dropAllTables = true) // Updated API
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    super.onCreate(db)
                    Log.d("DatabaseModule", "Database created")
                }

                override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    super.onOpen(db)
                    Log.d("DatabaseModule", "Database opened")
                }
            })
            .build()
    }

    @Provides
    fun provideCompetitionDao(database: AppDatabase): CompetitionDao {
        return database.competitionDao()
    }

    @Provides
    fun provideGolferDao(database: AppDatabase): MslGolferDao {
        return database.mslGolferDao()
    }
}