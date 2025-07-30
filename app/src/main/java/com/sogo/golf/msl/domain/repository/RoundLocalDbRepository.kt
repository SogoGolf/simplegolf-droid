package com.sogo.golf.msl.domain.repository

import com.sogo.golf.msl.domain.model.Round
import kotlinx.coroutines.flow.Flow

interface RoundLocalDbRepository {
    fun getAllRounds(): Flow<List<Round>>
    suspend fun getRoundById(roundId: String): Round?
    suspend fun saveRound(round: Round)
    suspend fun deleteRound(roundId: String)
    suspend fun clearAllRounds()
    suspend fun getUnsyncedRounds(): List<Round>
    suspend fun markAsSynced(roundId: String)
    suspend fun getRoundCount(): Int
    suspend fun getActiveTodayRound(dateString: String): Round?
}
