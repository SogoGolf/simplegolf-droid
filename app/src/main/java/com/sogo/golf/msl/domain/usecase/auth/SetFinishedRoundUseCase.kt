package com.sogo.golf.msl.domain.usecase.auth

import com.sogo.golf.msl.domain.repository.remote.AuthRepository
import javax.inject.Inject

class SetFinishedRoundUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(finished: Boolean): Result<Unit> {
        // Legacy method - now refreshes active round state from database
        return authRepository.refreshActiveRoundState()
    }
}
