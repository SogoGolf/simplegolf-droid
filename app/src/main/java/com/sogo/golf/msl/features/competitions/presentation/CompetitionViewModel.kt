package com.sogo.golf.msl.features.competitions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.repository.MslCompetitionLocalDbRepository
import com.sogo.golf.msl.domain.repository.MslGameLocalDbRepository
import com.sogo.golf.msl.domain.usecase.fees.GetFeesUseCase
import com.sogo.golf.msl.domain.usecase.msl_golfer.GetMslGolferUseCase
import com.sogo.golf.msl.domain.usecase.sogo_golfer.GetSogoGolferUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompetitionViewModel @Inject constructor(
    private val competitionRepository: MslCompetitionLocalDbRepository,
    private val getMslGolferUseCase: GetMslGolferUseCase,
    private val gameRepository: MslGameLocalDbRepository,
    private val getSogoGolferUseCase: GetSogoGolferUseCase,
    private val getFeesUseCase: GetFeesUseCase,
    private val fetchAndSaveGameUseCase: com.sogo.golf.msl.domain.usecase.game.FetchAndSaveGameUseCase,
    private val fetchAndSaveCompetitionUseCase: com.sogo.golf.msl.domain.usecase.competition.FetchAndSaveCompetitionUseCase,
    private val fetchAndSaveSogoGolferUseCase: com.sogo.golf.msl.domain.usecase.sogo_golfer.FetchAndSaveSogoGolferUseCase,
    private val getMslClubAndTenantIdsUseCase: com.sogo.golf.msl.domain.usecase.club.GetMslClubAndTenantIdsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompetitionUiState())
    val uiState: StateFlow<CompetitionUiState> = _uiState.asStateFlow()

    // ✅ ADD: Store the selected round cost
    private val _selectedRoundCost = MutableStateFlow(0.0)
    val selectedRoundCost: StateFlow<Double> = _selectedRoundCost.asStateFlow()

    // Always observe local competition data (works offline)
    val currentCompetition = competitionRepository.getCompetition()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val localGame = gameRepository.getGame()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // ✅ ADD GOLFER STATEFLOW
    val currentGolfer = getMslGolferUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val sogoGolfer = currentGolfer
        .flatMapLatest { golfer ->
            if (golfer?.golfLinkNo != null) {
                getSogoGolferUseCase(golfer.golfLinkNo)
            } else {
                flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val mslFees = getFeesUseCase.getFeesByEntityName("msl")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    val allCompetitions = competitionRepository.getAllCompetitions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // ✅ DEBUG: Log fee data when it loads
        viewModelScope.launch {
            mslFees.collect { fees ->
                android.util.Log.d("CompetitionViewModel", "=== MSL FEES LOADED ===")
                android.util.Log.d("CompetitionViewModel", "Fees count: ${fees.size}")
                fees.forEach { fee ->
                    android.util.Log.d("CompetitionViewModel",
                        "Fee: ${fee.description} - ${fee.numberHoles} holes - $${fee.cost} - Waived: ${fee.isWaived}")
                }
            }
        }

        // ✅ DEBUG: Log SogoGolfer data when it loads
        viewModelScope.launch {
            currentGolfer.collect { golfer ->
                if (golfer != null) {
                    android.util.Log.d("CompetitionViewModel", "=== MSL GOLFER LOADED ===")
                    android.util.Log.d("CompetitionViewModel", "Golfer: ${golfer.firstName} ${golfer.surname} (${golfer.golfLinkNo})")

                    // Now observe the SogoGolfer for this golfLinkNo
                    getSogoGolferUseCase(golfer.golfLinkNo).collect { sogoGolfer ->
                        android.util.Log.d("CompetitionViewModel", "=== SOGO GOLFER LOADED ===")
                        android.util.Log.d("CompetitionViewModel", "SogoGolfer: ${sogoGolfer?.firstName} ${sogoGolfer?.lastName}")
                        android.util.Log.d("CompetitionViewModel", "Token Balance: ${sogoGolfer?.tokenBalance}")
                    }
                }
            }
        }
    }

    // Example method: Fetch competition data from server
    fun fetchCompetitionData(competitionId: String = "default-comp-2024") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            when (val result = competitionRepository.fetchAndSaveCompetition(competitionId)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "MSL Competition data updated! Found ${result.data.players.size} players."
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.error.toUserMessage()
                    )
                }
                is NetworkResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    private suspend fun fetchMslGameData(clubIdStr: String): Result<Unit> {
        return try {
            android.util.Log.d("CompetitionViewModel", "🎮 Fetching MSL game data...")
            when (val gameResult = fetchAndSaveGameUseCase(clubIdStr)) {
                is NetworkResult.Success -> {
                    android.util.Log.d("CompetitionViewModel", "✅ MSL game data fetched successfully: Competition ${gameResult.data.mainCompetitionId}")
                    Result.success(Unit)
                }
                is NetworkResult.Error -> {
                    val error = gameResult.error.toUserMessage()
                    android.util.Log.e("CompetitionViewModel", "❌ Failed to fetch MSL game data: $error")
                    Result.failure(Exception(error))
                }
                is NetworkResult.Loading -> {
                    Result.success(Unit) // Should not happen in suspend function
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CompetitionViewModel", "Error fetching MSL game data", e)
            Result.failure(e)
        }
    }

    // ✅ NEW: Fetch MSL competition data using the use case
    private suspend fun fetchMslCompetitionData(clubIdStr: String): Result<Unit> {
        return try {
            android.util.Log.d("CompetitionViewModel", "🏆 Fetching MSL competition data...")
            when (val competitionResult = fetchAndSaveCompetitionUseCase(clubIdStr)) {
                is NetworkResult.Success -> {
                    android.util.Log.d("CompetitionViewModel", "✅ MSL competition data fetched successfully: ${competitionResult.data.players.size} players")
                    Result.success(Unit)
                }
                is NetworkResult.Error -> {
                    val error = competitionResult.error.toUserMessage()
                    android.util.Log.e("CompetitionViewModel", "❌ Failed to fetch MSL competition data: $error")
                    Result.failure(Exception(error))
                }
                is NetworkResult.Loading -> {
                    Result.success(Unit) // Should not happen in suspend function
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CompetitionViewModel", "Error fetching MSL competition data", e)
            Result.failure(e)
        }
    }

    private suspend fun refreshSogoGolferData(): Result<Unit> {
        return try {
            val currentMslGolfer = currentGolfer.value
            if (currentMslGolfer?.golfLinkNo == null) {
                android.util.Log.w("CompetitionViewModel", "⚠️ No golf link number available - cannot refresh Sogo Golfer data")
                return Result.failure(Exception("No golf link number available"))
            }

            android.util.Log.d("CompetitionViewModel", "🏌️ Refreshing Sogo Golfer data for: ${currentMslGolfer.golfLinkNo}")

            when (val result = fetchAndSaveSogoGolferUseCase(currentMslGolfer.golfLinkNo)) {
                is NetworkResult.Success -> {
                    android.util.Log.d("CompetitionViewModel", "✅ Sogo Golfer data refreshed. Token balance: ${result.data.tokenBalance}")
                    Result.success(Unit)
                }
                is NetworkResult.Error -> {
                    val error = result.error.toUserMessage()
                    android.util.Log.e("CompetitionViewModel", "❌ Failed to refresh Sogo Golfer data: $error")
                    Result.failure(Exception(error))
                }
                is NetworkResult.Loading -> {
                    Result.success(Unit) // Should not happen in suspend function
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CompetitionViewModel", "Error refreshing Sogo Golfer data", e)
            Result.failure(e)
        }
    }

    suspend fun refreshMslData(): Result<Unit> {
        return try {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Get the selected club (same pattern as HomeViewModel)
            val selectedClub = getMslClubAndTenantIdsUseCase()
            if (selectedClub?.clubId == null) {
                android.util.Log.w("CompetitionViewModel", "⚠️ No club selected - cannot fetch MSL data")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No club selected. Please login again."
                )
                return Result.failure(Exception("No club selected"))
            }

            val clubIdStr = selectedClub.clubId.toString()
            android.util.Log.d("CompetitionViewModel", "Refreshing MSL data for club: $clubIdStr")

            // Refresh MSL game data first
            val gameResult = fetchMslGameData(clubIdStr)
            if (gameResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to refresh MSL game data: ${gameResult.exceptionOrNull()?.message}"
                )
                return gameResult
            }

            // Then refresh MSL competition data
            val competitionResult = fetchMslCompetitionData(clubIdStr)
            if (competitionResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to refresh MSL competition data: ${competitionResult.exceptionOrNull()?.message}"
                )
                return competitionResult
            }

            // ✅ NEW: Refresh Sogo Golfer data to ensure token balance is up-to-date
            val sogoGolferResult = refreshSogoGolferData()
            if (sogoGolferResult.isFailure) {
                android.util.Log.w("CompetitionViewModel", "⚠️ Failed to refresh Sogo Golfer data: ${sogoGolferResult.exceptionOrNull()?.message}")
                // Don't fail the entire refresh for Sogo Golfer issues, just log the warning
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                successMessage = "MSL data refreshed successfully"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "MSL refresh failed: ${e.message}"
            )
            Result.failure(e)
        }
    }

    // Example method: Refresh current competition
    fun refreshCompetition() {
        val competitionId = "weekend-tournament-2024"
        fetchCompetitionData(competitionId)
    }

    // Example method: Clear all competitions (useful for testing/logout)
    fun clearAllCompetitions() {
        viewModelScope.launch {
            competitionRepository.clearAllCompetitions()
            _uiState.value = _uiState.value.copy(
                successMessage = "All competitions cleared"
            )
        }
    }

    // ✅ NEW: Include round state
    private val _includeRound = MutableStateFlow(true)
    val includeRound: StateFlow<Boolean> = _includeRound.asStateFlow()

    // ✅ NEW: Token cost calculation as StateFlow
    val tokenCost = combine(
        localGame,
        mslFees,
        _includeRound
    ) { game, fees, include ->
        if (!include) {
            0.0
        } else {
            game?.numberOfHoles?.let { holes ->
                fees.find { fee ->
                    fee.numberHoles == holes
                }?.cost ?: 0.0
            } ?: 0.0
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    // ✅ NEW: Can proceed calculation as StateFlow
    val canProceed = combine(
        localGame,
        sogoGolfer,
        tokenCost,
        _includeRound
    ) { game, sogo, cost, include ->
        val hasCompetitions = game?.numberOfHoles != null && game.numberOfHoles > 0
        val hasSufficientTokens = !include || cost == 0.0 ||
                (sogo?.tokenBalance ?: 0) >= cost
        hasCompetitions && hasSufficientTokens
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    // ✅ NEW: Update include round state
    fun setIncludeRound(include: Boolean) {
        _includeRound.value = include
    }

    // ✅ NEW: Check if user has sufficient tokens
    fun hasSufficientTokens(requiredTokens: Double): Boolean {
        // This will need to be implemented when SogoGolfer flow is properly set up
        // For now, return true as a placeholder
        return true
    }

    // Example method: Get competition summary
    fun getCompetitionSummary(): String {
        val competition = currentCompetition.value
        return when {
            competition == null -> "No competition data available"
            competition.players.isEmpty() -> "Competition loaded but no players found"
            else -> {
                val playerCount = competition.players.size
                val competitionName = competition.players.firstOrNull()?.competitionName ?: "Unknown"
                val competitionType = competition.players.firstOrNull()?.competitionType ?: "Unknown"
                "Competition: $competitionName ($competitionType) with $playerCount players"
            }
        }
    }

    // Example method: Get player names
    fun getPlayerNames(): List<String> {
        return currentCompetition.value?.players?.map { player ->
            "${player.firstName ?: ""} ${player.lastName ?: ""}".trim()
        } ?: emptyList()
    }

    // Clear messages
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    // Debug function to check what's in the database
    fun debugDatabaseContents() {
        viewModelScope.launch {
            try {
                val unsynced = competitionRepository.getUnsyncedCompetitions()
                android.util.Log.d("CompetitionViewModel", "Unsynced competitions: ${unsynced.size}")
                unsynced.forEachIndexed { index, comp ->
                    android.util.Log.d("CompetitionViewModel", "Competition $index: ${comp.players.size} players")
                }

                _uiState.value = _uiState.value.copy(
                    successMessage = "Database contains ${unsynced.size} competitions"
                )
            } catch (e: Exception) {
                android.util.Log.e("CompetitionViewModel", "Error checking database", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error checking database: ${e.message}"
                )
            }
        }
    }
}