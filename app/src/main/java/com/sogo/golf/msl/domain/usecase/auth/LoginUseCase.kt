package com.sogo.golf.msl.domain.usecase.auth

import com.sogo.golf.msl.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.login()
    }
}