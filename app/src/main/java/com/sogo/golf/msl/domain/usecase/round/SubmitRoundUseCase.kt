package com.sogo.golf.msl.domain.usecase.round

import com.sogo.golf.msl.common.Resource
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.msl.v2.ScoresContainer
import com.sogo.golf.msl.domain.model.msl.v2.ScoresResponse
import com.sogo.golf.msl.domain.repository.remote.MslRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SubmitRoundUseCase @Inject constructor(
    private val repository: MslRepository
) {
    operator fun invoke(clubId: String, mslScores: ScoresContainer): Flow<Resource<ScoresResponse>> = flow {
        try {
            emit(Resource.Loading())
            when (val result = repository.postMslScores(clubId, mslScores)) {
                is NetworkResult.Success -> {
                    emit(Resource.Success(result.data))
                }
                is NetworkResult.Error -> {
                    emit(Resource.Error(result.error.toUserMessage()))
                }
                is NetworkResult.Loading -> {
                    emit(Resource.Loading())
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }
}
