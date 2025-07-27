package com.sogo.golf.msl.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.sogo.golf.msl.data.local.database.dao.CompetitionDao
//import com.sogo.golf.msl.data.local.database.dao.RoundDao
//import com.sogo.golf.msl.data.local.database.dao.ScoreDao
//import com.sogo.golf.msl.data.local.database.dao.UserDao
import com.sogo.golf.msl.data.local.database.entities.CompetitionEntity
//import com.sogo.golf.msl.data.local.database.entities.RoundEntity
//import com.sogo.golf.msl.data.local.database.entities.ScoreEntity
//import com.sogo.golf.msl.data.local.database.entities.UserEntity

@Database(
    entities = [
//        UserEntity::class,
//        RoundEntity::class,
//        ScoreEntity::class,
        CompetitionEntity::class // Add this
    ],
    version = 2, // Increment version
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
//    abstract fun userDao(): UserDao
//    abstract fun roundDao(): RoundDao
//    abstract fun scoreDao(): ScoreDao
    abstract fun competitionDao(): CompetitionDao // Add this

    companion object {
        const val DATABASE_NAME = "msl_golf_database"
    }
}
