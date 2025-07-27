package com.sogo.golf.msl.data.repository.local

import android.util.Log
import com.sogo.golf.msl.data.local.database.dao.CompetitionDao
import com.sogo.golf.msl.data.local.database.entities.CompetitionEntity
import com.sogo.golf.msl.data.network.NetworkChecker
import com.sogo.golf.msl.data.repository.BaseRepository
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.msl.MslCompetition
import com.sogo.golf.msl.domain.model.msl.MslHole
import com.sogo.golf.msl.domain.model.msl.MslPlayer
import com.sogo.golf.msl.domain.repository.MslCompetitionLocalDbRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MslCompetitionLocalDbRepositoryImpl @Inject constructor(
    private val networkChecker: NetworkChecker,
    private val competitionDao: CompetitionDao
) : BaseRepository(networkChecker), MslCompetitionLocalDbRepository {

    companion object {
        private const val TAG = "CompetitionRepo"
    }

    // Get local competition data (always available)
    override fun getCurrentCompetition(): Flow<MslCompetition?> {
        Log.d(TAG, "getCurrentCompetition called")
        return competitionDao.getCurrentCompetition()
            .map { entity ->
                Log.d(TAG, "getCurrentCompetition mapped: entity = $entity")
                entity?.toDomainModel()
            }
    }

    // Get all competitions
    override fun getAllCompetitions(): Flow<List<MslCompetition>> {
        Log.d(TAG, "getAllCompetitions called")
        return competitionDao.getAllCompetitions()
            .map { entities ->
                Log.d(TAG, "getAllCompetitions mapped: ${entities.size} entities")
                entities.map { it.toDomainModel() }
            }
    }

    // Fetch from network and save locally
    override suspend fun fetchAndSaveCompetition(competitionId: String): NetworkResult<MslCompetition> {
        Log.d(TAG, "fetchAndSaveCompetition called with ID: $competitionId")

        return safeNetworkCall {
            // Mock competition for now
            val mockCompetition = MslCompetition(
                players = listOf(
                    MslPlayer(
                        firstName = "John",
                        lastName = "Doe",
                        dailyHandicap = 15,
                        golfLinkNumber = "12345",
                        competitionName = "Weekend Tournament",
                        competitionType = "Stroke Play",
                        teeName = "Championship",
                        teeColour = "Blue",
                        teeColourName = "Blue Tees",
                        scoreType = "Gross",
                        slopeRating = 113,
                        scratchRating = 72.0,
                        holes = listOf(
                            MslHole(
                                par = 4,
                                strokes = 0,
                                strokeIndexes = listOf(1),
                                distance = 350,
                                holeNumber = 1,
                                holeName = "First Tee",
                                holeAlias = "1st"
                            )
                        )
                    )
                )
            )

            Log.d(TAG, "Created mock competition with ${mockCompetition.players.size} players")

            // Save to local database
            saveCompetitionLocally(mockCompetition, competitionId)

            Log.d(TAG, "Mock competition saved successfully")

            mockCompetition
        }
    }

    // Save competition locally
    private suspend fun saveCompetitionLocally(competition: MslCompetition, competitionId: String) {
        Log.d(TAG, "saveCompetitionLocally called with ID: $competitionId")

        val entity = CompetitionEntity.fromDomainModel(competition, competitionId)
        Log.d(TAG, "Created entity: $entity")

        competitionDao.insertCompetition(entity)
        Log.d(TAG, "Entity inserted into database")

        // Verify it was saved
        val savedEntity = competitionDao.getCompetitionById(competitionId)
        Log.d(TAG, "Verification - saved entity: $savedEntity")

        // Get all competitions to see what's in the database
        val allCompetitions = competitionDao.getUnsyncedCompetitions()
        Log.d(TAG, "All competitions in database: ${allCompetitions.size}")
    }

    // Sync competition to server
    override suspend fun syncCompetitionToServer(competitionId: String): NetworkResult<Unit> {
        return safeNetworkCall {
            val competition = competitionDao.getCompetitionById(competitionId)
            Log.d(TAG, "syncCompetitionToServer - found competition: $competition")

            // TODO: Replace with actual API call
            // mslApi.submitCompetition(competition)

            // Mark as synced
            competitionDao.markAsSynced(competitionId)
            Log.d(TAG, "Competition marked as synced")
        }
    }

    // Get unsynced competitions for background sync
    override suspend fun getUnsyncedCompetitions(): List<MslCompetition> {
        val entities = competitionDao.getUnsyncedCompetitions()
        Log.d(TAG, "getUnsyncedCompetitions: ${entities.size} entities")
        return entities.map { it.toDomainModel() }
    }

    // Clear all competitions (useful for logout)
    override suspend fun clearAllCompetitions() {
        Log.d(TAG, "clearAllCompetitions called")
        competitionDao.clearAllCompetitions()
        Log.d(TAG, "All competitions cleared from database")
    }
}