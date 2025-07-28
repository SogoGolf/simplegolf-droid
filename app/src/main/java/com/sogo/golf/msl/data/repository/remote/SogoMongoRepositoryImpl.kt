package com.sogo.golf.msl.data.repository.remote

import android.util.Log
import com.sogo.golf.msl.data.network.NetworkChecker
import com.sogo.golf.msl.data.network.api.SogoMongoApiService
import com.sogo.golf.msl.data.network.dto.mongodb.toDomainModel
import com.sogo.golf.msl.data.repository.BaseRepository
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.mongodb.Fee
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
}