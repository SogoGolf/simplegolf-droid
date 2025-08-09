package com.sogo.golf.msl.domain.usecase.round

import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.mongodb.RoundSummary
import com.sogo.golf.msl.domain.repository.remote.SogoMongoRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetRoundsSummaryUseCase @Inject constructor(
    private val sogoMongoRepository: SogoMongoRepository
) {
    suspend operator fun invoke(golfLinkNo: String): NetworkResult<List<RoundSummary>> {
        return sogoMongoRepository.getRoundsSummary(golfLinkNo)
    }
}