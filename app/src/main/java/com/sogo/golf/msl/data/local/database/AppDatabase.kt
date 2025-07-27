package com.sogo.golf.msl.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sogo.golf.msl.data.local.database.dao.CompetitionDao
import com.sogo.golf.msl.data.local.database.entities.CompetitionEntity

@Database(
    entities = [
        CompetitionEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun competitionDao(): CompetitionDao

    companion object {
        const val DATABASE_NAME = "msl_golf_database"
    }
}