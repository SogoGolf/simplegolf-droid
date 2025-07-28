package com.sogo.golf.msl.domain.usecase.game

import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.msl.MslGame
import com.sogo.golf.msl.domain.repository.MslGameLocalDbRepository
import com.sogo.golf.msl.domain.usecase.date.SaveGameDataDateUseCase
import javax.inject.Inject

class FetchAndSaveGameUseCase @Inject constructor(
    private val gameRepository: MslGameLocalDbRepository,
    private val saveGameDataDateUseCase: SaveGameDataDateUseCase // ✅ ADD THIS
) {
    suspend operator fun invoke(gameId: String): NetworkResult<MslGame> {
        val result = gameRepository.fetchAndSaveGame(gameId)

        // ✅ If successful, save today's date
        if (result is NetworkResult.Success) {
            saveGameDataDateUseCase()
            android.util.Log.d("FetchAndSaveGameUseCase", "✅ Game data fetched and date saved")
        }

        return result
    }
}