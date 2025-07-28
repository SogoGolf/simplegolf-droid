package com.sogo.golf.msl.domain.usecase.competition

import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.msl.MslCompetition
import com.sogo.golf.msl.domain.repository.MslCompetitionLocalDbRepository
import com.sogo.golf.msl.domain.usecase.date.SaveGameDataDateUseCase
import javax.inject.Inject

class FetchAndSaveCompetitionUseCase @Inject constructor(
    private val competitionRepository: MslCompetitionLocalDbRepository,
    private val saveGameDataDateUseCase: SaveGameDataDateUseCase // ✅ ADD THIS
) {
    suspend operator fun invoke(competitionId: String): NetworkResult<MslCompetition> {
        val result = competitionRepository.fetchAndSaveCompetition(competitionId)

        // ✅ If successful, save today's date
        if (result is NetworkResult.Success) {
            saveGameDataDateUseCase()
            android.util.Log.d("FetchAndSaveCompetitionUseCase", "✅ Competition data fetched and date saved")
        }

        return result
    }
}