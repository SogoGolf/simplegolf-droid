package com.sogo.golf.msl.data.repository.local

import android.util.Log
import com.sogo.golf.msl.data.local.database.dao.mongodb.SogoGolferDao
import com.sogo.golf.msl.data.local.database.entities.mongodb.SogoGolferEntity
import com.sogo.golf.msl.data.network.NetworkChecker
import com.sogo.golf.msl.data.network.api.CreateGolferRequestDto
import com.sogo.golf.msl.data.network.api.UpdateGolferRequestDto
import com.sogo.golf.msl.data.repository.BaseRepository
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.mongodb.SogoGolfer
import com.sogo.golf.msl.domain.repository.SogoGolferLocalDbRepository
import com.sogo.golf.msl.domain.repository.remote.SogoMongoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SogoGolferLocalDbRepositoryImpl @Inject constructor(
    private val networkChecker: NetworkChecker,
    private val sogoGolferDao: SogoGolferDao,
    private val sogoMongoRepository: SogoMongoRepository
) : BaseRepository(networkChecker), SogoGolferLocalDbRepository {

    companion object {
        private const val TAG = "SogoGolferLocalDbRepo"
    }

    override fun getSogoGolferByGolfLinkNo(golfLinkNo: String): Flow<SogoGolfer?> {
        Log.d(TAG, "getSogoGolferByGolfLinkNo called with: $golfLinkNo")
        return sogoGolferDao.getSogoGolferByGolfLinkNoFlow(golfLinkNo)
            .map { entity ->
                Log.d(TAG, "getSogoGolferByGolfLinkNo mapped: entity = $entity")
                entity?.toDomainModel()
            }
    }

    override suspend fun getSogoGolferByGolfLinkNoOnce(golfLinkNo: String): SogoGolfer? {
        Log.d(TAG, "getSogoGolferByGolfLinkNoOnce called with: $golfLinkNo")
        val entity = sogoGolferDao.getSogoGolferByGolfLinkNo(golfLinkNo)
        Log.d(TAG, "Found entity: $entity")
        return entity?.toDomainModel()
    }

    override suspend fun getSogoGolferById(id: String): SogoGolfer? {
        Log.d(TAG, "getSogoGolferById called with: $id")
        val entity = sogoGolferDao.getSogoGolferById(id)
        Log.d(TAG, "Found entity: $entity")
        return entity?.toDomainModel()
    }

    override fun getAllSogoGolfers(): Flow<List<SogoGolfer>> {
        Log.d(TAG, "getAllSogoGolfers called")
        return sogoGolferDao.getAllSogoGolfers()
            .map { entities ->
                Log.d(TAG, "getAllSogoGolfers mapped: ${entities.size} entities")
                entities.map { it.toDomainModel() }
            }
    }

    override fun getActiveSogoGolfers(): Flow<List<SogoGolfer>> {
        Log.d(TAG, "getActiveSogoGolfers called")
        return sogoGolferDao.getActiveSogoGolfers()
            .map { entities ->
                Log.d(TAG, "getActiveSogoGolfers mapped: ${entities.size} entities")
                entities.map { it.toDomainModel() }
            }
    }

    override suspend fun fetchAndSaveSogoGolfer(golfLinkNo: String): NetworkResult<SogoGolfer> {
        Log.d(TAG, "fetchAndSaveSogoGolfer called with: $golfLinkNo")

        return safeNetworkCall {
            // Call the API to get SogoGolfer data
            when (val result = sogoMongoRepository.getSogoGolferByGolfLinkNo(golfLinkNo)) {
                is NetworkResult.Success -> {
                    val sogoGolfer = result.data
                    Log.d(TAG, "API returned SogoGolfer: ${sogoGolfer.firstName} ${sogoGolfer.lastName}")

                    // Save/replace in database
                    saveSogoGolferToDatabase(sogoGolfer)

                    Log.d(TAG, "SogoGolfer saved successfully in database")
                    sogoGolfer
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "API call failed: ${result.error}")
                    throw Exception("Failed to fetch SogoGolfer: ${result.error.toUserMessage()}")
                }
                is NetworkResult.Loading -> {
                    throw Exception("Unexpected loading state")
                }
            }
        }
    }

    override suspend fun saveSogoGolfer(sogoGolfer: SogoGolfer) {
        Log.d(TAG, "saveSogoGolfer called for: ${sogoGolfer.firstName} ${sogoGolfer.lastName} (${sogoGolfer.golfLinkNo})")
        saveSogoGolferToDatabase(sogoGolfer)
    }

    private suspend fun saveSogoGolferToDatabase(sogoGolfer: SogoGolfer) {
        Log.d(TAG, "saveSogoGolferToDatabase called")

        val entity = SogoGolferEntity.fromDomainModel(sogoGolfer)
        Log.d(TAG, "Created entity: $entity")

        // Use replace strategy
        sogoGolferDao.replaceSogoGolfer(entity)
        Log.d(TAG, "SogoGolfer replaced in database")

        // Verify the save worked
        val savedEntity = sogoGolferDao.getSogoGolferByGolfLinkNo(sogoGolfer.golfLinkNo)
        Log.d(TAG, "Verification - saved entity: $savedEntity")
    }

    override suspend fun deleteSogoGolferByGolfLinkNo(golfLinkNo: String) {
        Log.d(TAG, "deleteSogoGolferByGolfLinkNo called with: $golfLinkNo")
        sogoGolferDao.deleteSogoGolferByGolfLinkNo(golfLinkNo)
        Log.d(TAG, "SogoGolfer deleted from database")
    }

    override suspend fun clearAllSogoGolfers() {
        Log.d(TAG, "clearAllSogoGolfers called")
        sogoGolferDao.clearAllSogoGolfers()
        Log.d(TAG, "All SogoGolfers cleared from database")
    }

    override suspend fun hasSogoGolferByGolfLinkNo(golfLinkNo: String): Boolean {
        val count = sogoGolferDao.hasSogoGolferByGolfLinkNo(golfLinkNo)
        Log.d(TAG, "hasSogoGolferByGolfLinkNo: $count golfers found for $golfLinkNo")
        return count > 0
    }

    override suspend fun createAndSaveGolfer(request: CreateGolferRequestDto): NetworkResult<SogoGolfer> {
        Log.d(TAG, "createAndSaveGolfer called for: ${request.firstName} ${request.lastName}")
        
        return safeNetworkCall {
            when (val result = sogoMongoRepository.createGolfer(request)) {
                is NetworkResult.Success -> {
                    val sogoGolfer = result.data
                    Log.d(TAG, "API created SogoGolfer: ${sogoGolfer.firstName} ${sogoGolfer.lastName}")
                    
                    saveSogoGolferToDatabase(sogoGolfer)
                    
                    Log.d(TAG, "SogoGolfer created and saved successfully in database")
                    sogoGolfer
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "API call failed: ${result.error}")
                    throw Exception("Failed to create SogoGolfer: ${result.error.toUserMessage()}")
                }
                is NetworkResult.Loading -> {
                    throw Exception("Unexpected loading state")
                }
            }
        }
    }

    override suspend fun updateAndSaveGolfer(golflinkNo: String, request: UpdateGolferRequestDto): NetworkResult<SogoGolfer> {
        Log.d(TAG, "updateAndSaveGolfer called for: $golflinkNo")
        
        return safeNetworkCall {
            when (val result = sogoMongoRepository.updateGolferData(golflinkNo, request)) {
                is NetworkResult.Success -> {
                    val sogoGolfer = result.data
                    Log.d(TAG, "API updated SogoGolfer: ${sogoGolfer.firstName} ${sogoGolfer.lastName}")
                    
                    saveSogoGolferToDatabase(sogoGolfer)
                    
                    Log.d(TAG, "SogoGolfer updated and saved successfully in database")
                    sogoGolfer
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "API call failed: ${result.error}")
                    throw Exception("Failed to update SogoGolfer: ${result.error.toUserMessage()}")
                }
                is NetworkResult.Loading -> {
                    throw Exception("Unexpected loading state")
                }
            }
        }
    }
}
