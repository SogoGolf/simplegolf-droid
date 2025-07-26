package com.sogo.golf.msl.data.repository

import com.sogo.golf.msl.data.local.preferences.AuthPreferences
import com.sogo.golf.msl.domain.model.AuthState
import com.sogo.golf.msl.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authPreferences: AuthPreferences
) : AuthRepository {

    // Repository scope for initialization
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Same StateFlow pattern as your AuthManager!
    private val _authState = MutableStateFlow(AuthState())
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Convert Flow to StateFlow using stateIn
    override val isLoggedIn: StateFlow<Boolean> = authState
        .map { it.isLoggedIn }
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    override val finishedRound: StateFlow<Boolean> = authState
        .map { it.hasFinishedRound }
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    init {
        // Initialize state from preferences
        repositoryScope.launch {
            _authState.value = AuthState(
                isLoggedIn = authPreferences.isLoggedIn(),
                hasFinishedRound = authPreferences.hasFinishedRound()
            )
        }
    }

    override suspend fun login(): Result<Unit> {
        return try {
            authPreferences.setLoggedIn(true)
            _authState.value = _authState.value.copy(isLoggedIn = true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            authPreferences.setLoggedIn(false)
            authPreferences.setFinishedRound(false)
            _authState.value = AuthState(isLoggedIn = false, hasFinishedRound = false)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setFinishedRound(finished: Boolean): Result<Unit> {
        return try {
            authPreferences.setFinishedRound(finished)
            _authState.value = _authState.value.copy(hasFinishedRound = finished)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Legacy methods for compatibility during migration
    override fun isUserLoggedIn(): Boolean {
        return _authState.value.isLoggedIn
    }

    override fun isFinishedRound(): Boolean {
        return _authState.value.hasFinishedRound
    }
}