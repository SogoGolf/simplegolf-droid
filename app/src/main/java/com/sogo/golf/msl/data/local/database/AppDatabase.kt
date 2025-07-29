// app/src/main/java/com/sogo/golf/msl/data/local/database/AppDatabase.kt
package com.sogo.golf.msl.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sogo.golf.msl.data.local.database.dao.CompetitionDao
import com.sogo.golf.msl.data.local.database.dao.MslGameDao
import com.sogo.golf.msl.data.local.database.dao.MslGolferDao
import com.sogo.golf.msl.data.local.database.dao.mongodb.FeeDao
import com.sogo.golf.msl.data.local.database.dao.mongodb.SogoGolferDao
import com.sogo.golf.msl.data.local.database.entities.CompetitionEntity
import com.sogo.golf.msl.data.local.database.entities.MslGameEntity
import com.sogo.golf.msl.data.local.database.entities.MslGolferEntity
import com.sogo.golf.msl.data.local.database.entities.mongodb.FeeEntity
import com.sogo.golf.msl.data.local.database.entities.mongodb.SogoGolferEntity

// ✅ NEW: Migration from version 7 to version 8 to add tokenBalance column
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add tokenBalance column to sogo_golfers table with default value 0
        database.execSQL("ALTER TABLE sogo_golfers ADD COLUMN tokenBalance INTEGER NOT NULL DEFAULT 0")
    }
}

// ✅ NEW: Migration from version 8 to version 9 to add bookingTime column
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add bookingTime column to games table
        database.execSQL("ALTER TABLE games ADD COLUMN bookingTime TEXT")
    }
}

@Database(
    entities = [
        CompetitionEntity::class,
        MslGolferEntity::class,
        MslGameEntity::class,
        FeeEntity::class,
        SogoGolferEntity::class
    ],
    version = 9, // ✅ NEW: Increment version to 9 for bookingTime field
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun competitionDao(): CompetitionDao
    abstract fun mslGolferDao(): MslGolferDao
    abstract fun mslGameDao(): MslGameDao
    abstract fun feeDao(): FeeDao
    abstract fun sogoGolferDao(): SogoGolferDao

    companion object {
        const val DATABASE_NAME = "msl_golf_database"
    }
}