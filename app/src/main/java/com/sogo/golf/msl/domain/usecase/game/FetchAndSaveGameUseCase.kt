package com.sogo.golf.msl.domain.usecase.game

import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.msl.MslGame
import com.sogo.golf.msl.domain.repository.MslGameLocalDbRepository
import javax.inject.Inject

class FetchAndSaveGameUseCase @Inject constructor(
    private val gameRepository: MslGameLocalDbRepository
) {
    suspend operator fun invoke(gameId: String): NetworkResult<MslGame> {
        return gameRepository.fetchAndSaveGame(gameId)
    }
}