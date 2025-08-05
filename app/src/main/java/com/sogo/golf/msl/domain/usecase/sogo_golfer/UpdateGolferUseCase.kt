package com.sogo.golf.msl.domain.usecase.sogo_golfer

import com.sogo.golf.msl.data.network.api.UpdateGolferRequestDto
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.mongodb.SogoGolfer
import com.sogo.golf.msl.domain.repository.SogoGolferLocalDbRepository
import javax.inject.Inject

class UpdateGolferUseCase @Inject constructor(
    private val sogoGolferRepository: SogoGolferLocalDbRepository
) {
    suspend operator fun invoke(golflinkNo: String, request: UpdateGolferRequestDto): NetworkResult<SogoGolfer> {
        return sogoGolferRepository.updateAndSaveGolfer(golflinkNo, request)
    }
}
