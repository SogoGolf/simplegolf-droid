package com.sogo.golf.msl.domain.usecase.marker

import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.repository.remote.MslRepository
import javax.inject.Inject

class SelectMarkerUseCase @Inject constructor(
    private val mslRepository: MslRepository
) {
    suspend operator fun invoke(playerGolfLinkNumber: String): NetworkResult<Unit> {
        return mslRepository.selectMarker(playerGolfLinkNumber)
    }
}