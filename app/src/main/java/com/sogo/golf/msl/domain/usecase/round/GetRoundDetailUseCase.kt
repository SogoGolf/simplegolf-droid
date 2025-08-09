package com.sogo.golf.msl.domain.usecase.round

import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.mongodb.RoundDetail
import com.sogo.golf.msl.domain.repository.remote.SogoMongoRepository
import javax.inject.Inject

class GetRoundDetailUseCase @Inject constructor(
    private val sogoMongoRepository: SogoMongoRepository
) {
    suspend operator fun invoke(id: String): NetworkResult<RoundDetail> {
        return sogoMongoRepository.getRoundDetail(id)
    }
}