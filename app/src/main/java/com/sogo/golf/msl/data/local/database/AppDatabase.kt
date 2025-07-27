package com.sogo.golf.msl.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sogo.golf.msl.data.local.database.dao.CompetitionDao
import com.sogo.golf.msl.data.local.database.dao.MslGolferDao
import com.sogo.golf.msl.data.local.database.entities.CompetitionEntity
import com.sogo.golf.msl.data.local.database.entities.MslGolferEntity

@Database(
    entities = [
        CompetitionEntity::class,
        MslGolferEntity::class
    ],
    version = 3, // Increment from 2 to 3
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun competitionDao(): CompetitionDao
    abstract fun mslGolferDao(): MslGolferDao

    companion object {
        const val DATABASE_NAME = "msl_golf_database"
    }
}