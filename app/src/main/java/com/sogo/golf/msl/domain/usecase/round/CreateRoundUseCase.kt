package com.sogo.golf.msl.domain.usecase.round

import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.repository.remote.SogoMongoRepository
import javax.inject.Inject

class CreateRoundUseCase @Inject constructor(
    private val sogoMongoRepository: SogoMongoRepository
) {
    suspend operator fun invoke(round: Round): NetworkResult<Round> {
        return sogoMongoRepository.createRound(round)
    }
}
