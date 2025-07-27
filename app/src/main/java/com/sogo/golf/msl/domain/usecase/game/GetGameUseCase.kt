package com.sogo.golf.msl.domain.usecase.game

import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.msl.MslGame
import com.sogo.golf.msl.domain.repository.remote.MslRepository
import javax.inject.Inject

class GetGameUseCase @Inject constructor(
    private val mslRepository: MslRepository
) {
    suspend operator fun invoke(clubId: String): NetworkResult<MslGame> {
        return mslRepository.getGame(clubId)
    }
}