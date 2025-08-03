package com.sogo.golf.msl.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sogo.golf.msl.data.local.database.AppDatabase
import com.sogo.golf.msl.data.local.database.MIGRATION_7_8
import com.sogo.golf.msl.data.local.database.MIGRATION_8_9
import com.sogo.golf.msl.data.local.database.MIGRATION_9_10
import com.sogo.golf.msl.data.local.database.MIGRATION_10_11
import com.sogo.golf.msl.data.local.database.MIGRATION_11_12
import com.sogo.golf.msl.data.local.database.dao.RoundDao
import com.sogo.golf.msl.data.local.database.dao.TransactionDao
import com.sogo.golf.msl.data.local.database.dao.CompetitionDao
import com.sogo.golf.msl.data.local.database.dao.MslGameDao
import com.sogo.golf.msl.data.local.database.dao.MslGolferDao
import com.sogo.golf.msl.data.local.database.dao.mongodb.FeeDao
import com.sogo.golf.msl.data.local.database.dao.mongodb.SogoGolferDao
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
        Log.d("DatabaseModule", "Creating database with migration...")

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
            .fallbackToDestructiveMigration() // Keep this as fallback
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

    @Provides
    fun provideGameDao(database: AppDatabase): MslGameDao {
        return database.mslGameDao()
    }

    @Provides
    fun provideFeeDao(database: AppDatabase): FeeDao {
        return database.feeDao()
    }

    @Provides
    fun provideSogoGolferDao(database: AppDatabase): SogoGolferDao {
        return database.sogoGolferDao()
    }

    @Provides
    fun provideRoundDao(database: AppDatabase): RoundDao {
        return database.roundDao()
    }

    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }
}
