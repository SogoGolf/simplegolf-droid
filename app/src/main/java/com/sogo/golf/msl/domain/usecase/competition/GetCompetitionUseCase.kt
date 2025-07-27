package com.sogo.golf.msl.domain.usecase.competition

import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.msl.MslCompetition
import com.sogo.golf.msl.domain.repository.remote.MslRepository
import javax.inject.Inject

class GetCompetitionUseCase @Inject constructor(
    private val mslRepository: MslRepository
) {
    suspend operator fun invoke(clubId: String): NetworkResult<MslCompetition> {
        return mslRepository.getCompetition(clubId)
    }
}