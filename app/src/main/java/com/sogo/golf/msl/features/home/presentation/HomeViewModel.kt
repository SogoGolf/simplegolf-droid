    // app/src/main/java/com/sogo/golf/msl/features/home/presentation/HomeViewModel.kt
    package com.sogo.golf.msl.features.home.presentation

    import androidx.activity.result.ActivityResult
    import androidx.activity.result.ActivityResultLauncher
    import androidx.activity.result.IntentSenderRequest
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import com.sogo.golf.msl.domain.model.NetworkResult
    import com.sogo.golf.msl.domain.usecase.club.GetMslClubAndTenantIdsUseCase
    import com.sogo.golf.msl.domain.usecase.competition.FetchAndSaveCompetitionUseCase
    import com.sogo.golf.msl.domain.usecase.competition.GetLocalCompetitionUseCase
    import com.sogo.golf.msl.domain.usecase.fees.FetchAndSaveFeesUseCase
    import com.sogo.golf.msl.domain.usecase.game.FetchAndSaveGameUseCase
    import com.sogo.golf.msl.domain.usecase.game.GetLocalGameUseCase
    import com.sogo.golf.msl.domain.usecase.msl_golfer.GetMslGolferUseCase
    import dagger.hilt.android.lifecycle.HiltViewModel
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.SharingStarted
    import kotlinx.coroutines.flow.StateFlow
    import kotlinx.coroutines.flow.asStateFlow
    import kotlinx.coroutines.flow.stateIn
    import kotlinx.coroutines.launch
    import javax.inject.Inject

    @HiltViewModel
    class HomeViewModel @Inject constructor(
        val getMslGolferUseCase: GetMslGolferUseCase,
        private val getLocalGameUseCase: GetLocalGameUseCase,
        private val getLocalCompetitionUseCase: GetLocalCompetitionUseCase,
        // NEW: Add fetching use cases
        private val fetchAndSaveGameUseCase: FetchAndSaveGameUseCase,
        private val fetchAndSaveCompetitionUseCase: FetchAndSaveCompetitionUseCase,
        private val getMslClubAndTenantIdsUseCase: GetMslClubAndTenantIdsUseCase,
        private val appUpdateManager: com.sogo.golf.msl.app.update.AppUpdateManager,
        private val fetchAndSaveFeesUseCase: FetchAndSaveFeesUseCase, // ✅ ADD THIS
    ) : ViewModel() {

        companion object {
            private const val TAG = "HomeViewModel"
        }

        // NEW: UI State for loading and error handling
        private val _uiState = MutableStateFlow(HomeUiState())
        val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

        // ✅ GLOBAL GOLFER ACCESS - Available everywhere!
        val currentGolfer = getMslGolferUseCase()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

        // ✅ GLOBAL GAME ACCESS FROM LOCAL DATABASE
        val localGame = getLocalGameUseCase()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

        // StateFlow for local competition data
        val localCompetition = getLocalCompetitionUseCase()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

        val updateState = appUpdateManager.updateState

        init {
            // ✅ NEW: Automatically fetch data when HomeViewModel is created
            android.util.Log.d(TAG, "=== HOME SCREEN INIT - FETCHING TODAY'S DATA ===")
            fetchTodaysData()
        }

        // NEW: Fetch today's game and competition data
        private fun fetchTodaysData() {
            viewModelScope.launch {
                try {
                    android.util.Log.d(TAG, "Starting to fetch today's data...")

                    // Set loading state
                    _uiState.value = _uiState.value.copy(
                        isLoading = true,
                        errorMessage = null
                    )

                    // Get the selected club
                    val selectedClub = getMslClubAndTenantIdsUseCase()
                    if (selectedClub?.clubId == null) {
                        android.util.Log.w(TAG, "⚠️ No club selected - cannot fetch data")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "No club selected. Please login again."
                        )
                        return@launch
                    }

                    val clubIdStr = selectedClub.clubId.toString()
                    android.util.Log.d(TAG, "Fetching data for club: $clubIdStr")

                    // Fetch both game and competition data in parallel
                    // Declare all success/error variables
                    var gameSuccess = false
                    var competitionSuccess = false
                    var feesSuccess = false
                    var gameError: String? = null
                    var competitionError: String? = null
                    var feesError: String? = null

                    // Fetch Game Data
                    android.util.Log.d(TAG, "🎮 Fetching game data...")
                    when (val gameResult = fetchAndSaveGameUseCase(clubIdStr)) {
                        is NetworkResult.Success -> {
                            android.util.Log.d(TAG, "✅ Game data fetched successfully: Competition ${gameResult.data.mainCompetitionId}")
                            gameSuccess = true
                        }
                        is NetworkResult.Error -> {
                            gameError = gameResult.error.toUserMessage()
                            android.util.Log.e(TAG, "❌ Failed to fetch game data: $gameError")
                        }
                        is NetworkResult.Loading -> { /* Already handled */ }
                    }

                    // Fetch Competition Data
                    android.util.Log.d(TAG, "🏆 Fetching competition data...")
                    when (val competitionResult = fetchAndSaveCompetitionUseCase(clubIdStr)) {
                        is NetworkResult.Success -> {
                            android.util.Log.d(TAG, "✅ Competition data fetched successfully: ${competitionResult.data.players.size} players")
                            competitionSuccess = true
                        }
                        is NetworkResult.Error -> {
                            competitionError = competitionResult.error.toUserMessage()
                            android.util.Log.e(TAG, "❌ Failed to fetch competition data: $competitionError")
                        }
                        is NetworkResult.Loading -> { /* Already handled */ }
                    }

                    // ✅ NEW: Fetch Fees Data
                    android.util.Log.d(TAG, "💰 Fetching fees data...")
                    when (val feesResult = fetchAndSaveFeesUseCase()) {
                        is NetworkResult.Success -> {
                            android.util.Log.d(TAG, "✅ Fees data fetched successfully: ${feesResult.data.size} fees")
                            feesSuccess = true
                        }
                        is NetworkResult.Error -> {
                            feesError = feesResult.error.toUserMessage()
                            android.util.Log.e(TAG, "❌ Failed to fetch fees data: $feesError")
                        }
                        is NetworkResult.Loading -> { /* Already handled */ }
                    }

                    // Update UI state based on results
                    when {
                        gameSuccess && competitionSuccess -> {
                            android.util.Log.d(TAG, "✅ All data fetched successfully!")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "Today's golf data loaded successfully!"
                            )
                        }
                        gameSuccess && !competitionSuccess -> {
                            android.util.Log.w(TAG, "⚠️ Game data fetched but competition failed")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "Game data loaded successfully",
                                errorMessage = "Competition data failed: $competitionError"
                            )
                        }
                        !gameSuccess && competitionSuccess -> {
                            android.util.Log.w(TAG, "⚠️ Competition data fetched but game failed")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "Competition data loaded successfully",
                                errorMessage = "Game data failed: $gameError"
                            )
                        }
                        else -> {
                            android.util.Log.e(TAG, "❌ Both data fetches failed")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "Failed to load today's data. Game: $gameError, Competition: $competitionError"
                            )
                        }
                    }

                } catch (e: Exception) {
                    android.util.Log.e(TAG, "❌ Exception while fetching today's data", e)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error loading today's data: ${e.message}"
                    )
                }
            }
        }

        // NEW: Method to manually retry fetching data
        fun retryFetchData() {
            android.util.Log.d(TAG, "🔄 Manual retry requested")
            clearMessages()
            fetchTodaysData()
        }

        // NEW: Method to clear messages
        fun clearMessages() {
            _uiState.value = _uiState.value.copy(
                errorMessage = null,
                successMessage = null
            )
        }

        // Example method showing how to use golfer data
        fun getGolferSummary(): String {
            val golfer = currentGolfer.value
            return when {
                golfer == null -> "No golfer data available"
                else -> "Welcome ${golfer.firstName} ${golfer.surname} (Handicap: ${golfer.primary})"
            }
        }

        // Example method showing how to use competition data
        fun getCompetitionSummary(): String {
            val competition = localCompetition.value
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

        // Example method showing how to use game data
        fun getGameSummary(): String {
            val game = localGame.value
            return when {
                game == null -> "No game data available"
                else -> {
                    val partnersCount = game.playingPartners.size
                    val competitionsCount = game.competitions.size
                    "Game: Competition ${game.mainCompetitionId}, Hole ${game.startingHoleNumber}, $partnersCount partners, $competitionsCount competitions"
                }
            }
        }

        fun checkForUpdatesAndStartCompetition(
            activity: android.app.Activity,
            activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
            onUpdateCheckComplete: () -> Unit,
            onNoUpdateRequired: () -> Unit
        ) {
            android.util.Log.d(TAG, "=== CHECKING FOR UPDATES BEFORE STARTING COMPETITION ===")

            // Simply trigger the update check
            // The UI will observe updateState and handle the flow
            appUpdateManager.checkForUpdates(activity, activityResultLauncher)
        }

        // NEW: Handle update result
        fun handleUpdateResult(result: ActivityResult) {
            appUpdateManager.handleUpdateResult(result)
        }

        // NEW: Clear update error
        fun clearUpdateError() {
            appUpdateManager.clearError()
        }

        override fun onCleared() {
            super.onCleared()
            // Clean up update manager resources
            appUpdateManager.cleanup()
        }

        // NEW: Method to check if we have all required data
        fun hasRequiredData(): Boolean {
            val hasGolfer = currentGolfer.value != null
            val hasGame = localGame.value != null
            val hasCompetition = localCompetition.value != null

            android.util.Log.d(TAG, "Data status - Golfer: $hasGolfer, Game: $hasGame, Competition: $hasCompetition")
            return hasGolfer && hasGame && hasCompetition
        }

        // NEW: Method to get data status summary
        fun getDataStatusSummary(): String {
            val golfer = currentGolfer.value
            val game = localGame.value
            val competition = localCompetition.value

            return buildString {
                appendLine("📊 Data Status:")
                appendLine("👤 Golfer: ${if (golfer != null) "✅ ${golfer.firstName} ${golfer.surname}" else "❌ Not loaded"}")
                appendLine("🎮 Game: ${if (game != null) "✅ Competition ${game.mainCompetitionId}" else "❌ Not loaded"}")
                appendLine("🏆 Competition: ${if (competition != null) "✅ ${competition.players.size} players" else "❌ Not loaded"}")
            }
        }
    }