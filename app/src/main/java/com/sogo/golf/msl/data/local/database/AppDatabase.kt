// app/src/main/java/com/sogo/golf/msl/data/local/database/AppDatabase.kt
package com.sogo.golf.msl.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sogo.golf.msl.data.local.database.entities.RoundEntity
import com.sogo.golf.msl.data.local.database.entities.TransactionEntity
import com.sogo.golf.msl.data.local.database.dao.CompetitionDao
import com.sogo.golf.msl.data.local.database.dao.RoundDao
import com.sogo.golf.msl.data.local.database.dao.TransactionDao
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

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add entityId column to sogo_golfers table
        database.execSQL("ALTER TABLE sogo_golfers ADD COLUMN entityId TEXT")
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create transactions table for duplicate prevention
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS transactions (
                id TEXT NOT NULL PRIMARY KEY,
                entityId TEXT,
                transactionId TEXT NOT NULL,
                golferId TEXT NOT NULL,
                golferEmail TEXT,
                transactionDate INTEGER NOT NULL,
                amount INTEGER NOT NULL,
                transactionType TEXT NOT NULL,
                debitCreditType TEXT NOT NULL,
                comment TEXT,
                status TEXT,
                mainCompetitionId INTEGER,
                lastUpdated INTEGER NOT NULL,
                isSynced INTEGER NOT NULL
            )
        """.trimIndent())
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Recreate the sogo_golfers table with the correct schema including appSettings
        database.execSQL("""
            CREATE TABLE sogo_golfers_new (
                id TEXT NOT NULL PRIMARY KEY,
                entityId TEXT,
                golfLinkNo TEXT NOT NULL,
                firstName TEXT NOT NULL,
                lastName TEXT NOT NULL,
                email TEXT,
                phone TEXT,
                dateOfBirth TEXT,
                handicap REAL,
                club TEXT,
                membershipType TEXT,
                isActive INTEGER NOT NULL,
                createdAt TEXT,
                updatedAt TEXT,
                tokenBalance INTEGER NOT NULL,
                appSettings TEXT,
                lastUpdated INTEGER NOT NULL
            )
        """.trimIndent())
        
        // Copy data from old table to new table (excluding any problematic columns)
        database.execSQL("""
            INSERT INTO sogo_golfers_new (id, entityId, golfLinkNo, firstName, lastName, email, phone, dateOfBirth, handicap, club, membershipType, isActive, createdAt, updatedAt, tokenBalance, lastUpdated)
            SELECT id, entityId, golfLinkNo, firstName, lastName, email, phone, dateOfBirth, handicap, club, membershipType, isActive, createdAt, updatedAt, tokenBalance, lastUpdated
            FROM sogo_golfers
        """.trimIndent())
        
        // Drop old table and rename new table
        database.execSQL("DROP TABLE sogo_golfers")
        database.execSQL("ALTER TABLE sogo_golfers_new RENAME TO sogo_golfers")
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add new fields to sogo_golfers table
        database.execSQL("ALTER TABLE sogo_golfers ADD COLUMN mobileNo TEXT")
        database.execSQL("ALTER TABLE sogo_golfers ADD COLUMN postCode TEXT")
        database.execSQL("ALTER TABLE sogo_golfers ADD COLUMN state TEXT")
        database.execSQL("ALTER TABLE sogo_golfers ADD COLUMN gender TEXT")
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
        TransactionEntity::class,
    ],
    version = 14,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun competitionDao(): CompetitionDao
    abstract fun mslGolferDao(): MslGolferDao
    abstract fun mslGameDao(): MslGameDao
    abstract fun feeDao(): FeeDao
    abstract fun sogoGolferDao(): SogoGolferDao
    abstract fun roundDao(): RoundDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "msl_golf_database"
    }
}
