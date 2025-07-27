package com.sogo.golf.msl.domain.usecase.auth

import com.sogo.golf.msl.domain.repository.remote.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.logout()
    }
}