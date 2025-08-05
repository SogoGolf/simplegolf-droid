package com.sogo.golf.msl.domain.usecase.sogo_golfer

import com.sogo.golf.msl.data.network.api.CreateGolferRequestDto
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.mongodb.SogoGolfer
import com.sogo.golf.msl.domain.repository.SogoGolferLocalDbRepository
import javax.inject.Inject

class CreateGolferUseCase @Inject constructor(
    private val sogoGolferRepository: SogoGolferLocalDbRepository
) {
    suspend operator fun invoke(request: CreateGolferRequestDto): NetworkResult<SogoGolfer> {
        return sogoGolferRepository.createAndSaveGolfer(request)
    }
}
