package com.sogo.golf.msl.domain.repository

import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.msl.Competition
import kotlinx.coroutines.flow.Flow

interface CompetitionRepository {
    fun getCurrentCompetition(): Flow<Competition?>
    fun getAllCompetitions(): Flow<List<Competition>>
    suspend fun fetchAndSaveCompetition(competitionId: String): NetworkResult<Competition>
    suspend fun syncCompetitionToServer(competitionId: String): NetworkResult<Unit>
    suspend fun getUnsyncedCompetitions(): List<Competition>
    suspend fun clearAllCompetitions()
}