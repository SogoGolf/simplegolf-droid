// app/src/main/java/com/sogo/golf/msl/data/local/database/AppDatabase.kt
package com.sogo.golf.msl.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sogo.golf.msl.data.local.database.dao.CompetitionDao
import com.sogo.golf.msl.data.local.database.dao.MslGameDao
import com.sogo.golf.msl.data.local.database.dao.MslGolferDao
import com.sogo.golf.msl.data.local.database.dao.mongodb.FeeDao
import com.sogo.golf.msl.data.local.database.entities.CompetitionEntity
import com.sogo.golf.msl.data.local.database.entities.MslGameEntity
import com.sogo.golf.msl.data.local.database.entities.MslGolferEntity
import com.sogo.golf.msl.data.local.database.entities.mongodb.FeeEntity

@Database(
    entities = [
        CompetitionEntity::class,
        MslGolferEntity::class,
        MslGameEntity::class,
        FeeEntity::class // Add Fee entity
    ],
    version = 6, // Increment version for new entity
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun competitionDao(): CompetitionDao
    abstract fun mslGolferDao(): MslGolferDao
    abstract fun mslGameDao(): MslGameDao
    abstract fun feeDao(): FeeDao // Add Fee DAO

    companion object {
        const val DATABASE_NAME = "msl_golf_database"
    }
}