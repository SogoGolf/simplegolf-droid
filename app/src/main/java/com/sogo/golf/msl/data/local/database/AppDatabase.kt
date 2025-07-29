// app/src/main/java/com/sogo/golf/msl/data/local/database/AppDatabase.kt
package com.sogo.golf.msl.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sogo.golf.msl.data.local.database.entities.RoundEntity
import com.sogo.golf.msl.data.local.database.dao.CompetitionDao
import com.sogo.golf.msl.data.local.database.dao.RoundDao
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

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create rounds table - SQL must match exactly what Room expects
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS rounds (
                id TEXT NOT NULL PRIMARY KEY,
                uuid TEXT,
                entityId TEXT,
                roundPlayedOff REAL,
                dailyHandicap REAL,
                golfLinkHandicap REAL,
                golflinkNo TEXT,
                scorecardUrl TEXT,
                roundRefCode TEXT,
                roundDate TEXT,
                roundType TEXT NOT NULL,
                startTime TEXT,
                finishTime TEXT,
                scratchRating REAL,
                slopeRating REAL,
                submittedTime TEXT,
                compScoreTotal INTEGER,
                whsFrontScoreStableford INTEGER,
                whsBackScoreStableford INTEGER,
                whsFrontScorePar INTEGER,
                whsBackScorePar INTEGER,
                whsFrontScoreStroke INTEGER,
                whsBackScoreStroke INTEGER,
                whsFrontScoreMaximumScore INTEGER,
                whsBackScoreMaximumScore INTEGER,
                roundApprovedBy TEXT,
                comment TEXT,
                createdDate TEXT,
                updateDate TEXT,
                updateUserId TEXT,
                courseId TEXT,
                courseUuid TEXT,
                isClubSubmitted INTEGER,
                isSubmitted INTEGER,
                isMarkedForReview INTEGER,
                isApproved INTEGER,
                teeColor TEXT,
                isClubComp INTEGER,
                isDeleted INTEGER,
                isAbandoned INTEGER,
                clubId TEXT,
                clubUuid TEXT,
                golferId TEXT,
                golferGender TEXT,
                golferEmail TEXT,
                golferFirstName TEXT,
                golferLastName TEXT,
                golferGLNumber TEXT,
                golferImageUrl TEXT,
                clubState TEXT,
                clubName TEXT,
                markerFirstName TEXT,
                markerLastName TEXT,
                markerEmail TEXT,
                markerGLNumber TEXT,
                compType TEXT,
                holeScores TEXT NOT NULL,
                sogoAppVersion TEXT,
                transactionId TEXT,
                playingPartnerRound TEXT,
                roundApprovalSignatureUrl TEXT,
                thirdPartyScorecardId TEXT,
                mslMetaData TEXT,
                lastUpdated INTEGER NOT NULL,
                isSynced INTEGER NOT NULL
            )
        """.trimIndent())
    }
}

@Database(
    entities = [
        CompetitionEntity::class,
        MslGolferEntity::class,
        MslGameEntity::class,
        FeeEntity::class,
        SogoGolferEntity::class,
        RoundEntity::class,
    ],
    version = 10,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun competitionDao(): CompetitionDao
    abstract fun mslGolferDao(): MslGolferDao
    abstract fun mslGameDao(): MslGameDao
    abstract fun feeDao(): FeeDao
    abstract fun sogoGolferDao(): SogoGolferDao
    abstract fun roundDao(): RoundDao

    companion object {
        const val DATABASE_NAME = "msl_golf_database"
    }
}
