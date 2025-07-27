package com.sogo.golf.msl.domain.repository

import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.msl.MslCompetition
import kotlinx.coroutines.flow.Flow

interface MslCompetitionLocalDbRepository {
    fun getCompetition(): Flow<MslCompetition?>
    fun getAllCompetitions(): Flow<List<MslCompetition>>
    suspend fun fetchAndSaveCompetition(competitionId: String): NetworkResult<MslCompetition>
    suspend fun syncCompetitionToServer(competitionId: String): NetworkResult<Unit>
    suspend fun getUnsyncedCompetitions(): List<MslCompetition>
    suspend fun clearAllCompetitions()
}