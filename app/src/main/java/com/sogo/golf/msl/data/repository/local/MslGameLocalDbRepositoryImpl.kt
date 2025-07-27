// app/src/main/java/com/sogo/golf/msl/data/repository/local/MslGameLocalDbRepositoryImpl.kt
package com.sogo.golf.msl.data.repository.local

import android.util.Log
import com.sogo.golf.msl.data.local.database.dao.MslGameDao
import com.sogo.golf.msl.data.local.database.entities.MslGameEntity
import com.sogo.golf.msl.data.network.NetworkChecker
import com.sogo.golf.msl.data.repository.BaseRepository
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.msl.MslGame
import com.sogo.golf.msl.domain.repository.MslGameLocalDbRepository
import com.sogo.golf.msl.domain.repository.remote.MslRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MslGameLocalDbRepositoryImpl @Inject constructor(
    private val networkChecker: NetworkChecker,
    private val gameDao: MslGameDao,
    private val mslRepository: MslRepository
) : BaseRepository(networkChecker), MslGameLocalDbRepository {

    companion object {
        private const val TAG = "GameRepo"
    }

    // Get local game data (always available)
    override fun getGame(): Flow<MslGame?> {
        Log.d(TAG, "getCurrentGame called")
        return gameDao.getCurrentGame()
            .map { entity ->
                Log.d(TAG, "getCurrentGame mapped: entity = $entity")
                entity?.toDomainModel()
            }
    }

    // Get all games
    override fun getAllGames(): Flow<List<MslGame>> {
        Log.d(TAG, "getAllGames called")
        return gameDao.getAllGames()
            .map { entities ->
                Log.d(TAG, "getAllGames mapped: ${entities.size} entities")
                entities.map { it.toDomainModel() }
            }
    }

    // Fetch from network and save locally
    override suspend fun fetchAndSaveGame(gameId: String): NetworkResult<MslGame> {
        Log.d(TAG, "fetchAndSaveGame called with ID: $gameId")

        return safeNetworkCall {
            // Call the API to get game data
            when (val result = mslRepository.getGame(gameId)) {
                is NetworkResult.Success -> {
                    val game = result.data
                    Log.d(TAG, "API returned game with competition ID: ${game.mainCompetitionId}")

                    // Save to local database
                    saveGameLocally(game, gameId)

                    Log.d(TAG, "Game saved successfully")
                    game
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "API call failed: ${result.error}")
                    throw Exception("Failed to fetch game: ${result.error.toUserMessage()}")
                }
                is NetworkResult.Loading -> {
                    throw Exception("Unexpected loading state")
                }
            }
        }
    }

    // Save game locally
    private suspend fun saveGameLocally(game: MslGame, gameId: String) {
        Log.d(TAG, "saveGameLocally called with ID: $gameId")

        val entity = MslGameEntity.fromDomainModel(game, gameId)
        Log.d(TAG, "Created entity: $entity")

        gameDao.insertGame(entity)
        Log.d(TAG, "Entity inserted into database")

        // Verify it was saved
        val savedEntity = gameDao.getGameById(gameId)
        Log.d(TAG, "Verification - saved entity: $savedEntity")

        // Get all games to see what's in the database
        val allGames = gameDao.getUnsyncedGames()
        Log.d(TAG, "All games in database: ${allGames.size}")
    }

    // Sync game to server
    override suspend fun syncGameToServer(gameId: String): NetworkResult<Unit> {
        return safeNetworkCall {
            val game = gameDao.getGameById(gameId)
            Log.d(TAG, "syncGameToServer - found game: $game")

            // TODO: Replace with actual API call if needed
            // mslApi.submitGame(game)

            // Mark as synced
            gameDao.markAsSynced(gameId)
            Log.d(TAG, "Game marked as synced")
        }
    }

    // Get unsynced games for background sync
    override suspend fun getUnsyncedGames(): List<MslGame> {
        val entities = gameDao.getUnsyncedGames()
        Log.d(TAG, "getUnsyncedGames: ${entities.size} entities")
        return entities.map { it.toDomainModel() }
    }

    // Clear all games (useful for logout)
    override suspend fun clearAllGames() {
        Log.d(TAG, "clearAllGames called")
        gameDao.clearAllGames()
        Log.d(TAG, "All games cleared from database")
    }
}