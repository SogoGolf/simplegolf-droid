// app/src/main/java/com/sogo/golf/msl/data/repository/local/MslCompetitionLocalDbRepositoryImpl.kt
package com.sogo.golf.msl.data.repository.local

import android.util.Log
import com.sogo.golf.msl.data.local.database.dao.CompetitionDao
import com.sogo.golf.msl.data.local.database.entities.CompetitionEntity
import com.sogo.golf.msl.data.network.NetworkChecker
import com.sogo.golf.msl.data.repository.BaseRepository
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.msl.MslCompetition
import com.sogo.golf.msl.domain.repository.MslCompetitionLocalDbRepository
import com.sogo.golf.msl.domain.repository.remote.MslRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MslCompetitionLocalDbRepositoryImpl @Inject constructor(
    private val networkChecker: NetworkChecker,
    private val competitionDao: CompetitionDao,
    private val mslRepository: MslRepository
) : BaseRepository(networkChecker), MslCompetitionLocalDbRepository {

    companion object {
        private const val TAG = "CompetitionRepo"
    }

    // Get local competition data (always available)
    override fun getCompetition(): Flow<MslCompetition?> {
        Log.d(TAG, "getCurrentCompetition called")
        return competitionDao.getCurrentCompetition()
            .map { entity ->
                Log.d(TAG, "getCurrentCompetition mapped: entity = $entity")
                entity?.toDomainModel()
            }
    }

    // Get all competitions (should only ever return 1 or 0)
    override fun getAllCompetitions(): Flow<List<MslCompetition>> {
        Log.d(TAG, "getAllCompetitions called")
        return competitionDao.getAllCompetitions()
            .map { entities ->
                Log.d(TAG, "getAllCompetitions mapped: ${entities.size} entities (should be 0 or 1)")
                entities.map { it.toDomainModel() }
            }
    }

    // UPDATED: Fetch from network and REPLACE in database
    override suspend fun fetchAndSaveCompetition(competitionId: String): NetworkResult<MslCompetition> {
        Log.d(TAG, "fetchAndSaveCompetition called with ID: $competitionId")

        return safeNetworkCall {
            // Call the API to get competition data
            when (val result = mslRepository.getCompetition(competitionId)) {
                is NetworkResult.Success -> {
                    val competition = result.data
                    Log.d(TAG, "API returned competition with ${competition.players.size} players")

                    // REPLACE the single competition in database
                    replaceCompetitionInDatabase(competition, competitionId)

                    Log.d(TAG, "Competition replaced successfully in database")
                    competition
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "API call failed: ${result.error}")
                    throw Exception("Failed to fetch competition: ${result.error.toUserMessage()}")
                }
                is NetworkResult.Loading -> {
                    throw Exception("Unexpected loading state")
                }
            }
        }
    }

    // UPDATED: Replace the single competition record
    private suspend fun replaceCompetitionInDatabase(competition: MslCompetition, competitionId: String) {
        Log.d(TAG, "replaceCompetitionInDatabase called")

        // Check current count before replace
        val countBefore = competitionDao.getCompetitionCount()
        Log.d(TAG, "Competitions in database before replace: $countBefore")

        val entity = CompetitionEntity.fromDomainModel(
            competition,
            CompetitionDao.SINGLE_COMPETITION_ID,
            competition.players.firstOrNull()?.competitionName
        )
        Log.d(TAG, "Created entity with fixed ID: ${entity.id}")

        // Use the new replace method
        competitionDao.replaceCompetition(entity)
        Log.d(TAG, "Competition replaced in database")

        // Verify the replace worked
        val countAfter = competitionDao.getCompetitionCount()
        Log.d(TAG, "Competitions in database after replace: $countAfter (should be 1)")

        val savedEntity = competitionDao.getCompetitionById(CompetitionDao.SINGLE_COMPETITION_ID)
        Log.d(TAG, "Verification - saved entity: $savedEntity")
    }

    // Sync competition to server
    override suspend fun syncCompetitionToServer(competitionId: String): NetworkResult<Unit> {
        return safeNetworkCall {
            val competition = competitionDao.getCompetitionById(CompetitionDao.SINGLE_COMPETITION_ID)
            Log.d(TAG, "syncCompetitionToServer - found competition: $competition")

            // TODO: Replace with actual API call if needed
            // mslApi.submitCompetition(competition)

            // Mark as synced
            competitionDao.markAsSynced(CompetitionDao.SINGLE_COMPETITION_ID)
            Log.d(TAG, "Competition marked as synced")
        }
    }

    // Get unsynced competitions (should only ever be 0 or 1)
    override suspend fun getUnsyncedCompetitions(): List<MslCompetition> {
        val entities = competitionDao.getUnsyncedCompetitions()
        Log.d(TAG, "getUnsyncedCompetitions: ${entities.size} entities (should be 0 or 1)")
        return entities.map { it.toDomainModel() }
    }

    // Clear the single competition
    override suspend fun clearAllCompetitions() {
        Log.d(TAG, "clearAllCompetitions called")
        competitionDao.clearAllCompetitions()
        Log.d(TAG, "Single competition cleared from database")
    }
}