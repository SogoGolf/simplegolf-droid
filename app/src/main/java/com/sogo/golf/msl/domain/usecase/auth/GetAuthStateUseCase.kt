package com.sogo.golf.msl.domain.usecase.auth

import com.sogo.golf.msl.domain.model.AuthState
import com.sogo.golf.msl.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetAuthStateUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): StateFlow<AuthState> {
        return authRepository.authState
    }
}