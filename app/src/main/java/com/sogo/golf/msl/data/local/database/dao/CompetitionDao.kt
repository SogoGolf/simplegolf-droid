package com.sogo.golf.msl.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sogo.golf.msl.data.local.database.entities.CompetitionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompetitionDao {

    @Query("SELECT * FROM competitions WHERE id = :competitionId")
    suspend fun getCompetitionById(competitionId: String): CompetitionEntity?

    @Query("SELECT * FROM competitions ORDER BY lastUpdated DESC LIMIT 1")
    fun getCurrentCompetition(): Flow<CompetitionEntity?>

    @Query("SELECT * FROM competitions ORDER BY lastUpdated DESC")
    fun getAllCompetitions(): Flow<List<CompetitionEntity>>

    @Query("SELECT * FROM competitions WHERE isSynced = 0")
    suspend fun getUnsyncedCompetitions(): List<CompetitionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompetition(competition: CompetitionEntity)

    @Update
    suspend fun updateCompetition(competition: CompetitionEntity)

    @Delete
    suspend fun deleteCompetition(competition: CompetitionEntity)

    @Query("DELETE FROM competitions")
    suspend fun clearAllCompetitions()

    @Query("UPDATE competitions SET isSynced = 1 WHERE id = :competitionId")
    suspend fun markAsSynced(competitionId: String)
}