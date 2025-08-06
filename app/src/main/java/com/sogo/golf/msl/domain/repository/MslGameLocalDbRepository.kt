// app/src/main/java/com/sogo/golf/msl/domain/repository/MslGameLocalDbRepository.kt
package com.sogo.golf.msl.domain.repository

import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.msl.MslGame
import kotlinx.coroutines.flow.Flow

interface MslGameLocalDbRepository {
    fun getGame(): Flow<MslGame?>
    fun getAllGames(): Flow<List<MslGame>>
    suspend fun fetchAndSaveGame(gameId: String): NetworkResult<MslGame>
    suspend fun syncGameToServer(gameId: String): NetworkResult<Unit>
    suspend fun getUnsyncedGames(): List<MslGame>
    suspend fun clearAllGames()
    suspend fun getGameCount(): Int
}