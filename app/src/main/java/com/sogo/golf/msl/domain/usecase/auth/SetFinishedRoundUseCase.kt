package com.sogo.golf.msl.domain.usecase.auth

import com.sogo.golf.msl.domain.repository.AuthRepository
import javax.inject.Inject

class SetFinishedRoundUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(finished: Boolean): Result<Unit> {
        return authRepository.setFinishedRound(finished)
    }
}