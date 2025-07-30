package com.sogo.golf.msl.domain.usecase.round

import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.NetworkError
import com.sogo.golf.msl.domain.repository.RoundLocalDbRepository
import com.sogo.golf.msl.domain.repository.remote.SogoMongoRepository
import javax.inject.Inject

class DeleteLocalAndRemoteRoundUseCase @Inject constructor(
    private val sogoMongoRepository: SogoMongoRepository,
    private val roundLocalDbRepository: RoundLocalDbRepository
) {
    suspend operator fun invoke(roundId: String): NetworkResult<Unit> {
        android.util.Log.d("DeleteRound", "Starting deletion process for round: $roundId")
        
        when (val remoteResult = sogoMongoRepository.deleteRound(roundId)) {
            is NetworkResult.Success -> {
                android.util.Log.d("DeleteRound", "✅ Successfully deleted round from MongoDB")
                
                try {
                    roundLocalDbRepository.deleteRound(roundId)
                    android.util.Log.d("DeleteRound", "✅ Successfully deleted round from local database")
                    return NetworkResult.Success(Unit)
                } catch (e: Exception) {
                    android.util.Log.e("DeleteRound", "❌ Failed to delete from local database", e)
                    return NetworkResult.Error(NetworkError.Unknown("Failed to delete round locally: ${e.message}"))
                }
            }
            is NetworkResult.Error -> {
                android.util.Log.e("DeleteRound", "❌ Failed to delete round from MongoDB: ${remoteResult.error}")
                return remoteResult
            }
            is NetworkResult.Loading -> return NetworkResult.Loading()
        }
    }
}
