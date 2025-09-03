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

    // Get all games (should only ever return 1 or 0)
    override fun getAllGames(): Flow<List<MslGame>> {
        Log.d(TAG, "getAllGames called")
        return gameDao.getAllGames()
            .map { entities ->
                Log.d(TAG, "getAllGames mapped: ${entities.size} entities (should be 0 or 1)")
                entities.map { it.toDomainModel() }
            }
    }

    // UPDATED: Fetch from network and REPLACE in database
    override suspend fun fetchAndSaveGame(gameId: String): NetworkResult<MslGame> {
        Log.d(TAG, "fetchAndSaveGame called with ID: $gameId")

        return safeNetworkCall {
            // Call the API to get game data
            when (val result = mslRepository.getGame(gameId)) {
                is NetworkResult.Success -> {
                    val game = result.data
                    Log.d(TAG, "API returned game with competition ID: ${game.mainCompetitionId}")
                    Log.d(TAG, "🔍 DEBUG: startingHoleNumber from API: ${game.startingHoleNumber}")
                    Log.d(TAG, "🔍 DEBUG: numberOfHoles from API: ${game.numberOfHoles}")

                    // REPLACE the single game in database
                    replaceGameInDatabase(game, gameId)

                    Log.d(TAG, "Game replaced successfully in database")
                    game
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "API call failed: ${result.error}")
                    throw Exception(result.error.toUserMessage())
                }
                is NetworkResult.Loading -> {
                    throw Exception("Unexpected loading state")
                }
            }
        }
    }

    // UPDATED: Replace the single game record
    private suspend fun replaceGameInDatabase(game: MslGame, gameId: String) {
        Log.d(TAG, "replaceGameInDatabase called")

        // Check current count before replace
        val countBefore = gameDao.getGameCount()
        Log.d(TAG, "Games in database before replace: $countBefore")

        val entity = MslGameEntity.fromDomainModel(game, MslGameDao.SINGLE_GAME_ID)
        Log.d(TAG, "Created entity with fixed ID: ${entity.id}")

        // Use the new replace method
        gameDao.replaceGame(entity)
        Log.d(TAG, "Game replaced in database")

        // Verify the replace worked
        val countAfter = gameDao.getGameCount()
        Log.d(TAG, "Games in database after replace: $countAfter (should be 1)")

        val savedEntity = gameDao.getGameById(MslGameDao.SINGLE_GAME_ID)
        Log.d(TAG, "Verification - saved entity: $savedEntity")
    }

    // Sync game to server
    override suspend fun syncGameToServer(gameId: String): NetworkResult<Unit> {
        return safeNetworkCall {
            val game = gameDao.getGameById(MslGameDao.SINGLE_GAME_ID)
            Log.d(TAG, "syncGameToServer - found game: $game")

            // TODO: Replace with actual API call if needed
            // mslApi.submitGame(game)

            // Mark as synced
            gameDao.markAsSynced(MslGameDao.SINGLE_GAME_ID)
            Log.d(TAG, "Game marked as synced")
        }
    }

    // Get unsynced games (should only ever be 0 or 1)
    override suspend fun getUnsyncedGames(): List<MslGame> {
        val entities = gameDao.getUnsyncedGames()
        Log.d(TAG, "getUnsyncedGames: ${entities.size} entities (should be 0 or 1)")
        return entities.map { it.toDomainModel() }
    }

    // Clear the single game
    override suspend fun clearAllGames() {
        Log.d(TAG, "clearAllGames called")
        gameDao.clearAllGames()
        Log.d(TAG, "Single game cleared from database")
    }

    override suspend fun getGameCount(): Int {
        Log.d(TAG, "getGameCount called")
        val count = gameDao.getGameCount()
        Log.d(TAG, "Found $count games")
        return count
    }
}