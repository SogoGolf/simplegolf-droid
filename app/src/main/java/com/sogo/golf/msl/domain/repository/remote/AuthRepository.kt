package com.sogo.golf.msl.domain.repository.remote

import com.sogo.golf.msl.domain.model.AuthState
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>
    val isLoggedIn: StateFlow<Boolean>
    val hasActiveRound: StateFlow<Boolean>

    suspend fun login(): Result<Unit>
    suspend fun logout(): Result<Unit>
    suspend fun refreshActiveRoundState(): Result<Unit>

    // Legacy methods for compatibility during migration
    fun isUserLoggedIn(): Boolean
    fun hasActiveRound(): Boolean
}
