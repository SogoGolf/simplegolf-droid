package com.sogo.golf.msl.data.repository.local

import android.util.Log
import com.sogo.golf.msl.data.local.database.dao.RoundDao
import com.sogo.golf.msl.data.local.database.entities.RoundEntity
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.repository.RoundLocalDbRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoundLocalDbRepositoryImpl @Inject constructor(
    private val roundDao: RoundDao
) : RoundLocalDbRepository {

    companion object {
        private const val TAG = "RoundLocalDbRepo"
    }

    override fun getAllRounds(): Flow<List<Round>> {
        Log.d(TAG, "getAllRounds called")
        return roundDao.getAllRounds()
            .map { entities ->
                Log.d(TAG, "getAllRounds mapped: ${entities.size} entities")
                entities.map { it.toDomainModel() }
            }
    }

    override suspend fun getRoundById(roundId: String): Round? {
        Log.d(TAG, "getRoundById called with: $roundId")
        val entity = roundDao.getRoundById(roundId)
        Log.d(TAG, "Found entity: $entity")
        return entity?.toDomainModel()
    }

    override suspend fun saveRound(round: Round) {
        Log.d(TAG, "saveRound called for: ${round.id}")
        val entity = RoundEntity.fromDomainModel(round)
        roundDao.insertRound(entity)
        Log.d(TAG, "Round saved to database")
    }

    override suspend fun deleteRound(roundId: String) {
        Log.d(TAG, "deleteRound called for: $roundId")
        roundDao.deleteRound(roundId)
        Log.d(TAG, "Round deleted from database")
    }

    override suspend fun clearAllRounds() {
        Log.d(TAG, "clearAllRounds called")
        roundDao.clearAllRounds()
        Log.d(TAG, "All rounds cleared from database")
    }

    override suspend fun getUnsyncedRounds(): List<Round> {
        val entities = roundDao.getUnsyncedRounds()
        Log.d(TAG, "getUnsyncedRounds: ${entities.size} entities")
        return entities.map { it.toDomainModel() }
    }

    override suspend fun markAsSynced(roundId: String) {
        Log.d(TAG, "markAsSynced called for: $roundId")
        roundDao.markAsSynced(roundId)
        Log.d(TAG, "Round marked as synced")
    }
}
