package com.sogo.golf.msl.domain.usecase.competition

import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.msl.MslCompetition
import com.sogo.golf.msl.domain.repository.MslCompetitionLocalDbRepository
import javax.inject.Inject

class FetchAndSaveCompetitionUseCase @Inject constructor(
    private val competitionRepository: MslCompetitionLocalDbRepository
) {
    suspend operator fun invoke(competitionId: String): NetworkResult<MslCompetition> {
        return competitionRepository.fetchAndSaveCompetition(competitionId)  // ← API + Database - EXACT same as game
    }
}