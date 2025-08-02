package com.sogo.golf.msl.domain.usecase.round

import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.repository.RoundLocalDbRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetRoundUseCase @Inject constructor(
    private val roundRepository: RoundLocalDbRepository
) {
    operator fun invoke(roundId: String): Flow<Round?> {
        return roundRepository.getAllRounds()
            .map { rounds ->
                rounds.find { it.id == roundId }
            }
    }
    
    suspend fun getRoundById(roundId: String): Round? {
        return roundRepository.getRoundById(roundId)
    }
}
