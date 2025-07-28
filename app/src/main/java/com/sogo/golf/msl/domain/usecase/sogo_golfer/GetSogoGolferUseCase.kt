package com.sogo.golf.msl.domain.usecase.sogo_golfer

import com.sogo.golf.msl.domain.model.mongodb.SogoGolfer
import com.sogo.golf.msl.domain.repository.SogoGolferLocalDbRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSogoGolferUseCase @Inject constructor(
    private val sogoGolferRepository: SogoGolferLocalDbRepository
) {
    operator fun invoke(golfLinkNo: String): Flow<SogoGolfer?> {
        return sogoGolferRepository.getSogoGolferByGolfLinkNo(golfLinkNo)
    }

    suspend fun getOnce(golfLinkNo: String): SogoGolfer? {
        return sogoGolferRepository.getSogoGolferByGolfLinkNoOnce(golfLinkNo)
    }

    fun getAllActive(): Flow<List<SogoGolfer>> {
        return sogoGolferRepository.getActiveSogoGolfers()
    }

    fun getAll(): Flow<List<SogoGolfer>> {
        return sogoGolferRepository.getAllSogoGolfers()
    }
}