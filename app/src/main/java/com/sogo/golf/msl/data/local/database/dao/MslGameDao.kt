// app/src/main/java/com/sogo/golf/msl/data/local/database/dao/MslGameDao.kt
package com.sogo.golf.msl.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sogo.golf.msl.data.local.database.entities.MslGameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MslGameDao {

    @Query("SELECT * FROM games WHERE id = :gameId")
    suspend fun getGameById(gameId: String): MslGameEntity?

    // Simple query: just get the first game (for testing)
    @Query("SELECT * FROM games LIMIT 1")
    fun getCurrentGame(): Flow<MslGameEntity?>

    @Query("SELECT * FROM games ORDER BY lastUpdated DESC")
    fun getAllGames(): Flow<List<MslGameEntity>>

    // Add a debug query to see all games with their timestamps
    @Query("SELECT id, mainCompetitionId, startingHoleNumber, lastUpdated FROM games ORDER BY lastUpdated DESC")
    suspend fun getAllGamesWithTimestamps(): List<GameSummary>

    @Query("SELECT * FROM games WHERE isSynced = 0")
    suspend fun getUnsyncedGames(): List<MslGameEntity>

    @Query("SELECT COUNT(*) FROM games")
    suspend fun getGameCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: MslGameEntity)

    @Update
    suspend fun updateGame(game: MslGameEntity)

    @Delete
    suspend fun deleteGame(game: MslGameEntity)

    @Query("DELETE FROM games")
    suspend fun clearAllGames()

    @Query("UPDATE games SET isSynced = 1 WHERE id = :gameId")
    suspend fun markAsSynced(gameId: String)
}

// Data class for debugging
data class GameSummary(
    val id: String,
    val mainCompetitionId: Int,
    val startingHoleNumber: Int,
    val lastUpdated: Long
)