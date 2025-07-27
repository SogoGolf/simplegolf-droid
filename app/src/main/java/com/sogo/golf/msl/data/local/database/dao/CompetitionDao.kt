// app/src/main/java/com/sogo/golf/msl/data/local/database/dao/CompetitionDao.kt
package com.sogo.golf.msl.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.sogo.golf.msl.data.local.database.entities.CompetitionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompetitionDao {

    companion object {
        const val SINGLE_COMPETITION_ID = "current_competition" // Fixed ID for the single competition record
    }

    @Query("SELECT * FROM competitions WHERE id = :competitionId")
    suspend fun getCompetitionById(competitionId: String): CompetitionEntity?

    // Get the current (and only) competition
    @Query("SELECT * FROM competitions LIMIT 1")
    fun getCurrentCompetition(): Flow<CompetitionEntity?>

    @Query("SELECT * FROM competitions ORDER BY lastUpdated DESC")
    fun getAllCompetitions(): Flow<List<CompetitionEntity>>

    @Query("SELECT COUNT(*) FROM competitions")
    suspend fun getCompetitionCount(): Int

    // UPDATED: Clear and insert - ensures only 1 record
    @Transaction
    suspend fun replaceCompetition(competition: CompetitionEntity) {
        // Clear all existing competitions
        clearAllCompetitions()
        // Insert the new competition with fixed ID
        val competitionWithFixedId = competition.copy(id = SINGLE_COMPETITION_ID)
        insertCompetitionInternal(competitionWithFixedId)
    }

    // Private internal insert method
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompetitionInternal(competition: CompetitionEntity)

    // Legacy insert method - now uses replace
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompetition(competition: CompetitionEntity)

    @Update
    suspend fun updateCompetition(competition: CompetitionEntity)

    @Query("DELETE FROM competitions")
    suspend fun clearAllCompetitions()

    @Query("UPDATE competitions SET isSynced = 1 WHERE id = :competitionId")
    suspend fun markAsSynced(competitionId: String)

    // Debug method
    @Query("SELECT id, competitionName, lastUpdated FROM competitions ORDER BY lastUpdated DESC")
    suspend fun getAllCompetitionsWithTimestamps(): List<CompetitionSummary>

    // Not needed for single record pattern, but keeping for compatibility
    @Query("SELECT * FROM competitions WHERE isSynced = 0")
    suspend fun getUnsyncedCompetitions(): List<CompetitionEntity>
}

// Data class for debugging
data class CompetitionSummary(
    val id: String,
    val competitionName: String?,
    val lastUpdated: Long
)