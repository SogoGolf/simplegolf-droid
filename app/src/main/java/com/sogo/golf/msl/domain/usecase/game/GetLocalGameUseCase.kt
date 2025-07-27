package com.sogo.golf.msl.domain.usecase.game

import com.sogo.golf.msl.domain.model.msl.MslGame
import com.sogo.golf.msl.domain.repository.MslGameLocalDbRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLocalGameUseCase @Inject constructor(
    private val gameRepository: MslGameLocalDbRepository
) {
    operator fun invoke(): Flow<MslGame?> {
        return gameRepository.getGame()
    }
}