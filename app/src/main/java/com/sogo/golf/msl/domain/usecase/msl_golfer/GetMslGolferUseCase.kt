package com.sogo.golf.msl.domain.usecase.msl_golfer

import com.sogo.golf.msl.domain.model.msl.MslGolfer
import com.sogo.golf.msl.domain.repository.MslGolferLocalDbRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMslGolferUseCase @Inject constructor(
    private val mslGolferLocalDbRepository: MslGolferLocalDbRepository
) {
    operator fun invoke(): Flow<MslGolfer?> {
        return mslGolferLocalDbRepository.getCurrentGolfer()
    }
}