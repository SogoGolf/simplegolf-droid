package com.sogo.golf.msl.features.competitions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sogo.golf.msl.data.network.NetworkChecker
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.repository.MslCompetitionLocalDbRepository
import com.sogo.golf.msl.domain.repository.MslGameLocalDbRepository
import com.sogo.golf.msl.domain.usecase.fees.GetFeesUseCase
import com.sogo.golf.msl.domain.usecase.msl_golfer.GetMslGolferUseCase
import com.sogo.golf.msl.domain.usecase.sogo_golfer.GetSogoGolferUseCase
import com.sogo.golf.msl.domain.usecase.sogo_golfer.FetchAndSaveSogoGolferUseCase
import com.sogo.golf.msl.domain.usecase.sogo_golfer.UpdateTokenBalanceUseCase
import com.sogo.golf.msl.domain.usecase.transaction.CreateTransactionUseCase
import com.revenuecat.purchases.models.StoreTransaction
import android.util.Log
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
import kotlinx.coroutines.yield
import javax.inject.Inject

@HiltViewModel
class CompetitionViewModel @Inject constructor(
    private val networkChecker: NetworkChecker,
    private val competitionRepository: MslCompetitionLocalDbRepository,
    private val getMslGolferUseCase: GetMslGolferUseCase,
    private val gameRepository: MslGameLocalDbRepository,
    private val getSogoGolferUseCase: GetSogoGolferUseCase,
    private val getFeesUseCase: GetFeesUseCase,
    private val fetchAndSaveGameUseCase: com.sogo.golf.msl.domain.usecase.game.FetchAndSaveGameUseCase,
    private val fetchAndSaveCompetitionUseCase: com.sogo.golf.msl.domain.usecase.competition.FetchAndSaveCompetitionUseCase,
    private val fetchAndSaveSogoGolferUseCase: com.sogo.golf.msl.domain.usecase.sogo_golfer.FetchAndSaveSogoGolferUseCase,
    private val getMslClubAndTenantIdsUseCase: com.sogo.golf.msl.domain.usecase.club.GetMslClubAndTenantIdsUseCase,
    private val updateTokenBalanceUseCase: UpdateTokenBalanceUseCase,
    private val createTransactionUseCase: CreateTransactionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompetitionUiState())
    val uiState: StateFlow<CompetitionUiState> = _uiState.asStateFlow()

    private val _purchaseTokensState = MutableStateFlow(PurchaseTokensState())
    val purchaseTokensState: StateFlow<PurchaseTokensState> = _purchaseTokensState.asStateFlow()

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

    private suspend fun fetchMslGameData(clubIdStr: String) {
        android.util.Log.d("CompetitionViewModel", "🎮 Fetching MSL game data...")
        when (val gameResult = fetchAndSaveGameUseCase(clubIdStr)) {
            is NetworkResult.Success -> {
                android.util.Log.d("CompetitionViewModel", "✅ MSL game data fetched successfully.")
                // Success, do nothing
            }
            is NetworkResult.Error -> {
                val error = gameResult.error.toUserMessage()
                android.util.Log.e("CompetitionViewModel", "❌ Failed to fetch MSL game data: $error")
                throw Exception(error)
            }
            is NetworkResult.Loading -> { /* No-op */ }
        }
    }

    // ✅ NEW: Fetch MSL competition data using the use case
    private suspend fun fetchMslCompetitionData(clubIdStr: String) {
        android.util.Log.d("CompetitionViewModel", "🏆 Fetching MSL competition data...")
        when (val competitionResult = fetchAndSaveCompetitionUseCase(clubIdStr)) {
            is NetworkResult.Success -> {
                android.util.Log.d("CompetitionViewModel", "✅ MSL competition data fetched successfully.")
                // Success, do nothing
            }
            is NetworkResult.Error -> {
                val error = competitionResult.error.toUserMessage()
                android.util.Log.e("CompetitionViewModel", "❌ Failed to fetch MSL competition data: $error")
                throw Exception(error)
            }
            is NetworkResult.Loading -> { /* No-op */ }
        }
    }

    private suspend fun refreshSogoGolferData() {
        val currentMslGolfer = currentGolfer.value
        if (currentMslGolfer?.golfLinkNo == null) {
            android.util.Log.w("CompetitionViewModel", "⚠️ No golf link number available - cannot refresh Sogo Golfer data")
            // Not a fatal error, just skip
            return
        }

        android.util.Log.d("CompetitionViewModel", "🏌️ Refreshing Sogo Golfer data for: ${currentMslGolfer.golfLinkNo}")
        when (val result = fetchAndSaveSogoGolferUseCase(currentMslGolfer.golfLinkNo)) {
            is NetworkResult.Success -> {
                android.util.Log.d("CompetitionViewModel", "✅ Sogo Golfer data refreshed.")
                // Success, do nothing
            }
            is NetworkResult.Error -> {
                val error = result.error.toUserMessage()
                android.util.Log.w("CompetitionViewModel", "⚠️ Failed to refresh Sogo Golfer data: $error")
                // Not considered a fatal error for the refresh, so just log a warning
            }
            is NetworkResult.Loading -> { /* No-op */ }
        }
    }

    fun triggerRefresh() {
        viewModelScope.launch {
            // First, check for network availability.
            if (!networkChecker.isNetworkAvailable()) {
                // Start with loading state to acknowledge the gesture
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                // Give the UI time to register the refresh gesture
                kotlinx.coroutines.delay(100) // Small delay

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No internet connection"
                )
                return@launch
            }

            // Network is available, so show the spinner and then yield.
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            yield() // Ensures the spinner animation starts before doing more work.

            try {
                val selectedClub = getMslClubAndTenantIdsUseCase()
                    ?: throw Exception("No club selected. Please login again.")

                val clubIdStr = selectedClub.clubId.toString()

                // These will now throw on failure, which will be caught below
                fetchMslGameData(clubIdStr)
                fetchMslCompetitionData(clubIdStr)
                refreshSogoGolferData()

                _uiState.value = _uiState.value.copy(successMessage = "MSL data refreshed successfully")

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Refresh failed: ${e.message}")
            } finally {
                // This will always be executed, ensuring the spinner is hidden.
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
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

    fun purchaseTokens() {
        viewModelScope.launch {
            _purchaseTokensState.value = _purchaseTokensState.value.copy(isInProgress = true)
            try {
                Log.d("CompetitionViewModel", "Starting token purchase process")
            } catch (e: Exception) {
                _purchaseTokensState.value = _purchaseTokensState.value.copy(isInProgress = false)
                Log.e("CompetitionViewModel", "Error during purchase: ${e.message}")
            }
        }
    }

    fun setPurchaseTokensState(isInProgress: Boolean) {
        _purchaseTokensState.value = _purchaseTokensState.value.copy(isInProgress = isInProgress)
    }

    suspend fun updateTokenBalanceForGolfer(
        storeTransaction: StoreTransaction,
        onNavigateToNext: () -> Unit
    ) {
        setPurchaseTokensState(isInProgress = true)

        val currentBalance = sogoGolfer.value?.tokenBalance ?: 0

        val updateResult = updateTokenBalanceUseCase(currentBalance, storeTransaction, sogoGolfer.value)
        updateResult.fold(
            onSuccess = { updatedGolfer ->
                val newBalance = updatedGolfer.tokenBalance
                Log.d("PaywallDialog", "Successfully updated token balance to $newBalance")

                val transactionResult = createTransactionUseCase(
                    tokens = updatedGolfer.tokenBalance - currentBalance,
                    entityIdVal = updatedGolfer.entityId,
                    transId = storeTransaction.orderId ?: "",
                    sogoGolfer = updatedGolfer,
                    transactionTypeVal = "PURCHASE",
                    debitCreditTypeVal = "CREDIT",
                    commentVal = "Purchase tokens",
                    statusVal = "completed"
                )
                transactionResult.fold(
                    onSuccess = {
                        Log.d("PaywallDialog", "Transaction record created successfully.")
                        onNavigateToNext()
                    },
                    onFailure = { txError ->
                        Log.e("PaywallDialog", "Failed to create transaction record", txError)
                        onNavigateToNext()
                    }
                )
            },
            onFailure = { updateError ->
                Log.e("PaywallDialog", "Failed to update token balance", updateError)
            }
        )

        setPurchaseTokensState(isInProgress = false)
    }
}
