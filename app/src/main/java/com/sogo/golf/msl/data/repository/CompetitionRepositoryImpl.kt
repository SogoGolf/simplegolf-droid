import com.sogo.golf.msl.data.local.database.dao.CompetitionDao
import com.sogo.golf.msl.data.local.database.entities.CompetitionEntity
import com.sogo.golf.msl.data.network.NetworkChecker
import com.sogo.golf.msl.data.repository.BaseRepository
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.msl.Competition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompetitionRepositoryImpl @Inject constructor(
    private val networkChecker: NetworkChecker,
    private val competitionDao: CompetitionDao
    // private val mslApi: MslApi  // Your API interface when you add it
) : BaseRepository(networkChecker) {

    // Get local competition data (always available)
    fun getCurrentCompetition(): Flow<Competition?> {
        return competitionDao.getCurrentCompetition()
            .map { entity -> entity?.toDomainModel() }
    }

    // Get all competitions
    fun getAllCompetitions(): Flow<List<Competition>> {
        return competitionDao.getAllCompetitions()
            .map { entities -> entities.map { it.toDomainModel() } }
    }

    // Fetch from network and save locally
    suspend fun fetchAndSaveCompetition(competitionId: String): NetworkResult<Competition> {
        return safeNetworkCall {
            // TODO: Replace with actual API call
            // val competition = mslApi.getCompetition(competitionId)

            // Mock competition for now
            val mockCompetition = Competition(
                players = listOf(
                    com.sogo.golf.msl.domain.model.msl.Player(
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
                            com.sogo.golf.msl.domain.model.msl.Hole(
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

            // Save to local database
            saveCompetitionLocally(mockCompetition, competitionId)

            mockCompetition
        }
    }

    // Save competition locally
    private suspend fun saveCompetitionLocally(competition: Competition, competitionId: String) {
        val entity = CompetitionEntity.fromDomainModel(competition, competitionId)
        competitionDao.insertCompetition(entity)
    }

    // Sync competition to server
    suspend fun syncCompetitionToServer(competitionId: String): NetworkResult<Unit> {
        return safeNetworkCall {
            val competition = competitionDao.getCompetitionById(competitionId)
            // TODO: Replace with actual API call
            // mslApi.submitCompetition(competition)

            // Mark as synced
            competitionDao.markAsSynced(competitionId)
        }
    }

    // Get unsynced competitions for background sync
    suspend fun getUnsyncedCompetitions(): List<Competition> {
        return competitionDao.getUnsyncedCompetitions().map { it.toDomainModel() }
    }

    // Clear all competitions (useful for logout)
    suspend fun clearAllCompetitions() {
        competitionDao.clearAllCompetitions()
    }
}