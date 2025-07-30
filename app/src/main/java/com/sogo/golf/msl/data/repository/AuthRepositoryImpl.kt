// Update to app/src/main/java/com/sogo/golf/msl/data/repository/AuthRepositoryImpl.kt
package com.sogo.golf.msl.data.repository

import com.sogo.golf.msl.data.local.preferences.AuthPreferences
import com.sogo.golf.msl.data.local.preferences.ClubPreferences
import com.sogo.golf.msl.domain.model.AuthState
import com.sogo.golf.msl.domain.repository.MslCompetitionLocalDbRepository
import com.sogo.golf.msl.domain.repository.MslGameLocalDbRepository
import com.sogo.golf.msl.domain.repository.MslGolferLocalDbRepository
import com.sogo.golf.msl.domain.repository.remote.AuthRepository
import com.sogo.golf.msl.domain.usecase.round.CheckActiveTodayRoundUseCase
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
    private val authPreferences: AuthPreferences,
    private val clubPreferences: ClubPreferences,
    private val mslGolferLocalDbRepository: MslGolferLocalDbRepository,
    private val mslCompetitionLocalDbRepository: MslCompetitionLocalDbRepository,
    private val mslGameLocalDbRepository: MslGameLocalDbRepository,
    private val checkActiveTodayRoundUseCase: CheckActiveTodayRoundUseCase
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

    override val hasActiveRound: StateFlow<Boolean> = authState
        .map { it.hasActiveRound }
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    init {
        // Initialize state from preferences and database
        repositoryScope.launch {
            val isLoggedIn = authPreferences.isLoggedIn()
            val hasActiveRound = if (isLoggedIn) {
                checkActiveTodayRoundUseCase()
            } else {
                false
            }
            
            _authState.value = AuthState(
                isLoggedIn = isLoggedIn,
                hasActiveRound = hasActiveRound
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
            // ✅ CLEAR GOLFER DATA ON LOGOUT
            mslGolferLocalDbRepository.clearGolfer()
            mslCompetitionLocalDbRepository.clearAllCompetitions()
            mslGameLocalDbRepository.clearAllGames()

            // NEW: ✅ CLEAR CLUB SELECTION ON LOGOUT
            clubPreferences.clearSelectedClub()

            _authState.value = AuthState(isLoggedIn = false, hasActiveRound = false)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshActiveRoundState(): Result<Unit> {
        return try {
            val hasActiveRound = checkActiveTodayRoundUseCase()
            _authState.value = _authState.value.copy(hasActiveRound = hasActiveRound)
            android.util.Log.d("AuthRepository", "Refreshed active round state: $hasActiveRound")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to refresh active round state", e)
            Result.failure(e)
        }
    }

    // Legacy methods for compatibility during migration
    override fun isUserLoggedIn(): Boolean {
        return _authState.value.isLoggedIn
    }

    override fun hasActiveRound(): Boolean {
        return _authState.value.hasActiveRound
    }
}
