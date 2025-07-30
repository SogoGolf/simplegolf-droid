package com.sogo.golf.msl.data.repository.remote

import android.util.Log
import com.sogo.golf.msl.data.network.NetworkChecker
import com.sogo.golf.msl.data.network.api.SogoMongoApiService
import com.sogo.golf.msl.data.network.dto.mongodb.toDomainModel
import com.sogo.golf.msl.data.network.dto.mongodb.toDto
import com.sogo.golf.msl.data.repository.BaseRepository
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.model.mongodb.Fee
import com.sogo.golf.msl.domain.model.mongodb.SogoGolfer
import com.sogo.golf.msl.domain.repository.remote.SogoMongoRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SogoMongoRepositoryImpl @Inject constructor(
    private val sogoMongoApiService: SogoMongoApiService,
    private val networkChecker: NetworkChecker
) : BaseRepository(networkChecker), SogoMongoRepository {

    companion object {
        private const val TAG = "SogoMongoRepository"
    }

    override suspend fun getFees(): NetworkResult<List<Fee>> {
        return safeNetworkCall {
            Log.d(TAG, "Getting fees from SOGO Mongo API")

            val response = sogoMongoApiService.getFees()

            if (response.isSuccessful) {
                val rawFees = response.body()
                Log.d(TAG, "Raw response body: $rawFees")

                val fees = rawFees?.toDomainModel() ?: emptyList()
                Log.d(TAG, "Successfully retrieved ${fees.size} fees")
                fees.forEach { fee ->
                    Log.d(TAG, "Fee: ${fee.description} - ${fee.numberHoles} holes - $${fee.cost}")
                }
                fees
            } else {
                Log.e(TAG, "Failed to get fees: ${response.code()} - ${response.message()}")
                Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                throw Exception("Failed to get fees: ${response.message()}")
            }
        }
    }

    override suspend fun getSogoGolferByGolfLinkNo(golfLinkNo: String): NetworkResult<SogoGolfer> {
        return safeNetworkCall {
            Log.d(TAG, "Getting SogoGolfer from SOGO Mongo API for golfLinkNo: $golfLinkNo")

            val response = sogoMongoApiService.getSogoGolferByGolfLinkNo(golfLinkNo)

            if (response.isSuccessful) {
                val rawSogoGolfer = response.body()
                Log.d(TAG, "Raw response body: $rawSogoGolfer")

                val sogoGolfer = rawSogoGolfer?.toDomainModel()
                    ?: throw Exception("Empty SogoGolfer response")

                Log.d(TAG, "Successfully retrieved SogoGolfer: ${sogoGolfer.firstName} ${sogoGolfer.lastName}")
                Log.d(TAG, "SogoGolfer details - Email: ${sogoGolfer.email}, Club: ${sogoGolfer.club}, Handicap: ${sogoGolfer.handicap}")

                sogoGolfer
            } else {
                Log.e(TAG, "Failed to get SogoGolfer: ${response.code()} - ${response.message()}")
                Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                throw Exception("Failed to get SogoGolfer: ${response.message()}")
            }
        }
    }

    override suspend fun createRound(round: Round): NetworkResult<Round> {
        return safeNetworkCall {
            Log.d(TAG, "Creating round in SOGO Mongo API for golfer: ${round.golflinkNo}")
            
            val roundDto = round.toDto()
            val response = sogoMongoApiService.createRound(roundDto)
            
            if (response.isSuccessful) {
                val createdRoundDto = response.body()
                Log.d(TAG, "Successfully created round with ID: ${createdRoundDto?.id}")
                
                round.copy(isSynced = true)
            } else {
                Log.e(TAG, "Failed to create round: ${response.code()} - ${response.message()}")
                Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                throw Exception("Failed to create round: ${response.message()}")
            }
        }
    }
}
