// app/src/main/java/com/sogo/golf/msl/data/local/database/dao/MslGameDao.kt
package com.sogo.golf.msl.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.sogo.golf.msl.data.local.database.entities.MslGameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MslGameDao {

    companion object {
        const val SINGLE_GAME_ID = "current_game" // Fixed ID for the single game record
    }

    // Get the single game record
    @Query("SELECT * FROM games WHERE id = :gameId")
    suspend fun getGameById(gameId: String): MslGameEntity?

    // Get the current (and only) game
    @Query("SELECT * FROM games LIMIT 1")
    fun getCurrentGame(): Flow<MslGameEntity?>

    // Get all games (should only ever be 1)
    @Query("SELECT * FROM games ORDER BY lastUpdated DESC")
    fun getAllGames(): Flow<List<MslGameEntity>>

    @Query("SELECT COUNT(*) FROM games")
    suspend fun getGameCount(): Int

    // UPDATED: Clear and insert - ensures only 1 record
    @Transaction
    suspend fun replaceGame(game: MslGameEntity) {
        // Clear all existing games
        clearAllGames()
        // Insert the new game with fixed ID
        val gameWithFixedId = game.copy(id = SINGLE_GAME_ID)
        insertGameInternal(gameWithFixedId)
    }

    // Private internal insert method
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameInternal(game: MslGameEntity)

    // Legacy insert method - now uses replace
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: MslGameEntity)

    @Update
    suspend fun updateGame(game: MslGameEntity)

    @Query("DELETE FROM games")
    suspend fun clearAllGames()

    @Query("UPDATE games SET isSynced = 1 WHERE id = :gameId")
    suspend fun markAsSynced(gameId: String)

    // Debug method
    @Query("SELECT id, mainCompetitionId, startingHoleNumber, lastUpdated FROM games ORDER BY lastUpdated DESC")
    suspend fun getAllGamesWithTimestamps(): List<GameSummary>

    // Not needed for single record pattern, but keeping for compatibility
    @Query("SELECT * FROM games WHERE isSynced = 0")
    suspend fun getUnsyncedGames(): List<MslGameEntity>
}

// Data class for debugging
data class GameSummary(
    val id: String,
    val mainCompetitionId: Int,
    val startingHoleNumber: Int,
    val lastUpdated: Long
)