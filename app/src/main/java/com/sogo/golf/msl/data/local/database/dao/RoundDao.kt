package com.sogo.golf.msl.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sogo.golf.msl.data.local.database.entities.RoundEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoundDao {

    @Query("SELECT * FROM rounds WHERE id = :roundId")
    suspend fun getRoundById(roundId: String): RoundEntity?

    @Query("SELECT * FROM rounds ORDER BY createdDate DESC")
    fun getAllRounds(): Flow<List<RoundEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRound(round: RoundEntity)

    @Query("DELETE FROM rounds WHERE id = :roundId")
    suspend fun deleteRound(roundId: String)

    @Query("DELETE FROM rounds")
    suspend fun clearAllRounds()

    @Query("UPDATE rounds SET isSynced = 1 WHERE id = :roundId")
    suspend fun markAsSynced(roundId: String)

    @Query("SELECT * FROM rounds WHERE isSynced = 0")
    suspend fun getUnsyncedRounds(): List<RoundEntity>

    @Query("SELECT COUNT(*) FROM rounds")
    suspend fun getRoundCount(): Int

    @Query("SELECT * FROM rounds WHERE DATE(roundDate) = :dateString AND (isSubmitted = 0 OR isSubmitted IS NULL) AND (isAbandoned = 0 OR isAbandoned IS NULL) LIMIT 1")
    suspend fun getActiveTodayRound(dateString: String): RoundEntity?
}
