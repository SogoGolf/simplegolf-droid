package com.sogo.golf.msl.domain.usecase.competition

import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.mongodb.Competition
import com.sogo.golf.msl.domain.repository.remote.SogoMongoRepository
import javax.inject.Inject

class GetCompetitionsUseCase @Inject constructor(
    private val sogoMongoRepository: SogoMongoRepository
) {
    suspend operator fun invoke(): NetworkResult<List<Competition>> {
        return sogoMongoRepository.getCompetitions()
    }
}