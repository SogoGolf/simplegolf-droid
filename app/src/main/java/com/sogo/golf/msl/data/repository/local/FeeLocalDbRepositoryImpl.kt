package com.sogo.golf.msl.data.repository.local

import android.util.Log
import com.sogo.golf.msl.data.local.database.dao.mongodb.FeeDao
import com.sogo.golf.msl.data.local.database.entities.mongodb.FeeEntity
import com.sogo.golf.msl.data.network.NetworkChecker
import com.sogo.golf.msl.data.repository.BaseRepository
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.mongodb.Fee
import com.sogo.golf.msl.domain.repository.FeeLocalDbRepository
import com.sogo.golf.msl.domain.repository.remote.SogoMongoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeeLocalDbRepositoryImpl @Inject constructor(
    private val networkChecker: NetworkChecker,
    private val feeDao: FeeDao,
    private val sogoMongoRepository: SogoMongoRepository
) : BaseRepository(networkChecker), FeeLocalDbRepository {

    companion object {
        private const val TAG = "FeeLocalDbRepo"
    }

    override fun getAllFees(): Flow<List<Fee>> {
        Log.d(TAG, "getAllFees called")
        return feeDao.getAllFees()
            .map { entities ->
                Log.d(TAG, "getAllFees mapped: ${entities.size} entities")
                entities.map { it.toDomainModel() }
            }
    }

    override fun getFeesByEntityId(entityId: String): Flow<List<Fee>> {
        Log.d(TAG, "getFeesByEntityId called with: $entityId")
        return feeDao.getFeesByEntityId(entityId)
            .map { entities ->
                Log.d(TAG, "getFeesByEntityId mapped: ${entities.size} entities")
                entities.map { it.toDomainModel() }
            }
    }

    override fun getFeesByNumberHoles(numberHoles: Int): Flow<List<Fee>> {
        Log.d(TAG, "getFeesByNumberHoles called with: $numberHoles")
        return feeDao.getFeesByNumberHoles(numberHoles)
            .map { entities ->
                Log.d(TAG, "getFeesByNumberHoles mapped: ${entities.size} entities")
                entities.map { it.toDomainModel() }
            }
    }

    override fun getActiveFees(): Flow<List<Fee>> {
        Log.d(TAG, "getActiveFees called")
        return feeDao.getActiveFees()
            .map { entities ->
                Log.d(TAG, "getActiveFees mapped: ${entities.size} entities")
                entities.map { it.toDomainModel() }
            }
    }

    override fun getWaivedFees(): Flow<List<Fee>> {
        Log.d(TAG, "getWaivedFees called")
        return feeDao.getWaivedFees()
            .map { entities ->
                Log.d(TAG, "getWaivedFees mapped: ${entities.size} entities")
                entities.map { it.toDomainModel() }
            }
    }

    override suspend fun getFeeById(feeId: String): Fee? {
        Log.d(TAG, "getFeeById called with: $feeId")
        val entity = feeDao.getFeeById(feeId)
        Log.d(TAG, "Found entity: $entity")
        return entity?.toDomainModel()
    }

    override suspend fun fetchAndSaveFees(): NetworkResult<List<Fee>> {
        Log.d(TAG, "fetchAndSaveFees called")

        return safeNetworkCall {
            // Call the API to get fees data
            when (val result = sogoMongoRepository.getFees()) {
                is NetworkResult.Success -> {
                    val fees = result.data
                    Log.d(TAG, "API returned ${fees.size} fees")

                    // Replace all fees in database
                    replaceFeesInDatabase(fees)

                    Log.d(TAG, "Fees replaced successfully in database")
                    fees
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "API call failed: ${result.error}")
                    throw Exception("Failed to fetch fees: ${result.error.toUserMessage()}")
                }
                is NetworkResult.Loading -> {
                    throw Exception("Unexpected loading state")
                }
            }
        }
    }

    private suspend fun replaceFeesInDatabase(fees: List<Fee>) {
        Log.d(TAG, "replaceFeesInDatabase called")

        // Check current count before replace
        val countBefore = feeDao.getFeeCount()
        Log.d(TAG, "Fees in database before replace: $countBefore")

        val entities = fees.map { FeeEntity.fromDomainModel(it) }
        Log.d(TAG, "Created ${entities.size} entities")

        // Replace all fees
        feeDao.replaceFees(entities)
        Log.d(TAG, "Fees replaced in database")

        // Verify the replace worked
        val countAfter = feeDao.getFeeCount()
        Log.d(TAG, "Fees in database after replace: $countAfter")
    }

    override suspend fun clearAllFees() {
        Log.d(TAG, "clearAllFees called")
        feeDao.clearAllFees()
        Log.d(TAG, "All fees cleared from database")
    }

    override suspend fun hasFees(): Boolean {
        val count = feeDao.getFeeCount()
        Log.d(TAG, "hasFees: $count fees in database")
        return count > 0
    }
}