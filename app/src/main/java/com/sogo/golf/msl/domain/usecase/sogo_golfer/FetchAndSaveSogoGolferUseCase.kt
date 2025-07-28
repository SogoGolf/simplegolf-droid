package com.sogo.golf.msl.domain.usecase.sogo_golfer

import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.mongodb.SogoGolfer
import com.sogo.golf.msl.domain.repository.SogoGolferLocalDbRepository
import javax.inject.Inject

class FetchAndSaveSogoGolferUseCase @Inject constructor(
    private val sogoGolferRepository: SogoGolferLocalDbRepository
) {
    suspend operator fun invoke(golfLinkNo: String): NetworkResult<SogoGolfer> {
        return sogoGolferRepository.fetchAndSaveSogoGolfer(golfLinkNo)
    }
}
