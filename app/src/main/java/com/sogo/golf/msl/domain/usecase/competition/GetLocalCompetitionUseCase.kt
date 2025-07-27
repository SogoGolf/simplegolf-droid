package com.sogo.golf.msl.domain.usecase.competition

import com.sogo.golf.msl.domain.model.msl.MslCompetition
import com.sogo.golf.msl.domain.repository.MslCompetitionLocalDbRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLocalCompetitionUseCase @Inject constructor(
    private val competitionRepository: MslCompetitionLocalDbRepository
) {
    operator fun invoke(): Flow<MslCompetition?> {
        return competitionRepository.getCompetition()  // ← From database only - EXACT same as game
    }
}