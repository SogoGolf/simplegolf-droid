package com.sogo.golf.msl.features.debug.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sogo.golf.msl.data.local.preferencesdata.GameDataTimestampPreferences
import com.sogo.golf.msl.domain.model.NetworkError
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.repository.MslGolferLocalDbRepository
import com.sogo.golf.msl.domain.usecase.club.GetMslClubAndTenantIdsUseCase
import com.sogo.golf.msl.domain.usecase.club.SetSelectedClubUseCase
import com.sogo.golf.msl.domain.usecase.competition.FetchAndSaveCompetitionUseCase
import com.sogo.golf.msl.domain.usecase.competition.GetCompetitionUseCase
import com.sogo.golf.msl.domain.usecase.competition.GetLocalCompetitionUseCase
import com.sogo.golf.msl.domain.usecase.date.ResetStaleDataUseCase
import com.sogo.golf.msl.domain.usecase.date.ValidateGameDataFreshnessUseCase
import com.sogo.golf.msl.domain.usecase.fees.FetchAndSaveFeesUseCase
import com.sogo.golf.msl.domain.usecase.game.FetchAndSaveGameUseCase
import com.sogo.golf.msl.domain.usecase.game.GetGameUseCase
import com.sogo.golf.msl.domain.usecase.game.GetLocalGameUseCase
import com.sogo.golf.msl.domain.usecase.msl_golfer.GetMslGolferUseCase
import com.sogo.golf.msl.shared.utils.DateUtils
import com.sogo.golf.msl.domain.repository.remote.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val getGameUseCase: GetGameUseCase,
    val getMslGolferUseCase: GetMslGolferUseCase,
    private val getCompetitionUseCase: GetCompetitionUseCase,
    private val mslGolferLocalDbRepository: MslGolferLocalDbRepository,
    // Game storage use cases
    private val getLocalGameUseCase: GetLocalGameUseCase,
    private val fetchAndSaveGameUseCase: FetchAndSaveGameUseCase,
    private val getMslClubAndTenantIdsUseCase: GetMslClubAndTenantIdsUseCase,
    private val setSelectedClubUseCase: SetSelectedClubUseCase,
    private val getLocalCompetitionUseCase: GetLocalCompetitionUseCase,
    private val fetchAndSaveCompetitionUseCase: FetchAndSaveCompetitionUseCase,
    private val gameDataTimestampPreferences: GameDataTimestampPreferences,
    private val validateGameDataFreshnessUseCase: ValidateGameDataFreshnessUseCase,
    private val resetStaleDataUseCase: ResetStaleDataUseCase,
    private val fetchAndSaveFeesUseCase: FetchAndSaveFeesUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DebugUiState())
    val uiState: StateFlow<DebugUiState> = _uiState.asStateFlow()

    // ✅ GLOBAL GOLFER ACCESS - Available everywhere!
    val currentGolfer = getMslGolferUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // NEW: ✅ GLOBAL GAME ACCESS FROM LOCAL DATABASE
    val localGame = getLocalGameUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // StateFlow for local competition data - EXACT same pattern as game
    val localCompetition = getLocalCompetitionUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private suspend fun getUserClubId(): String? {
        return try {
            val selectedClub = getMslClubAndTenantIdsUseCase()
            val clubId = selectedClub?.clubId?.toString()
            android.util.Log.d("DebugViewModel", "=== USER'S CLUB ID ===")
            android.util.Log.d("DebugViewModel", "Club ID: $clubId")
            android.util.Log.d("DebugViewModel", "Tenant ID: ${selectedClub?.tenantId}")
            clubId
        } catch (e: Exception) {
            android.util.Log.e("DebugViewModel", "Error getting user's club ID", e)
            null
        }
    }

    fun getGame() {
        viewModelScope.launch {
            val clubId = getUserClubId()
            if (clubId == null) {
                _uiState.value = _uiState.value.copy(
                    gameErrorMessage = "No club selected. Please login and select a club first."
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isLoadingGame = true,
                gameErrorMessage = null,
                gameSuccessMessage = null
            )

            android.util.Log.d("DebugViewModel", "Getting game data for user's club: $clubId")

            when (val result = getGameUseCase(clubId)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingGame = false,
                        gameData = result.data,
                        gameSuccessMessage = "Game data loaded from API successfully for club $clubId!"
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingGame = false,
                        gameErrorMessage = result.error.toUserMessage()
                    )
                }
                is NetworkResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    // NEW: ✅ GET GAME AND SAVE TO LOCAL DATABASE
    fun getGameAndSaveToDatabase() {
        viewModelScope.launch {
            val clubId = getUserClubId()
            if (clubId == null) {
                _uiState.value = _uiState.value.copy(
                    gameErrorMessage = "No club selected. Please login and select a club first."
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isLoadingGame = true,
                gameErrorMessage = null,
                gameSuccessMessage = null
            )

            android.util.Log.d("DebugViewModel", "=== FETCHING GAME AND SAVING TO DATABASE ===")
            android.util.Log.d("DebugViewModel", "User's Club ID: $clubId")

            when (val result = fetchAndSaveGameUseCase(clubId)) {
                is NetworkResult.Success -> {
                    android.util.Log.d("DebugViewModel", "✅ SUCCESS: Game fetched from API and saved to database")
                    android.util.Log.d("DebugViewModel", "  Competition ID: ${result.data.mainCompetitionId}")
                    android.util.Log.d("DebugViewModel", "  Starting Hole: ${result.data.startingHoleNumber}")
                    android.util.Log.d("DebugViewModel", "  Playing Partners: ${result.data.playingPartners.size}")
                    android.util.Log.d("DebugViewModel", "  Competitions: ${result.data.competitions.size}")

                    _uiState.value = _uiState.value.copy(
                        isLoadingGame = false,
                        gameData = result.data,
                        gameSuccessMessage = "✅ Game fetched from API and saved to database! Competition ID: ${result.data.mainCompetitionId}"
                    )
                }
                is NetworkResult.Error -> {
                    android.util.Log.e("DebugViewModel", "❌ ERROR: Failed to fetch and save game: ${result.error}")
                    
                    if (result.error is NetworkError.HttpError && result.error.code == 401 && result.error.isRefreshFailure) {
                        handleAuthenticationFailure()
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoadingGame = false,
                        gameErrorMessage = result.error.toUserMessage()
                    )
                }
                is NetworkResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun getCompetitionAndSaveToDatabase() {
        viewModelScope.launch {
            val clubId = getUserClubId()
            if (clubId == null) {
                _uiState.value = _uiState.value.copy(
                    competitionErrorMessage = "No club selected. Please login and select a club first."
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isLoadingCompetition = true,
                competitionErrorMessage = null,
                competitionSuccessMessage = null
            )

            android.util.Log.d("DebugViewModel", "=== FETCHING COMPETITION AND SAVING TO DATABASE ===")
            android.util.Log.d("DebugViewModel", "User's Club ID: $clubId")

            when (val result = fetchAndSaveCompetitionUseCase(clubId)) {
                is NetworkResult.Success -> {
                    android.util.Log.d("DebugViewModel", "✅ SUCCESS: Competition fetched from API and saved to database")
                    android.util.Log.d("DebugViewModel", "  Players count: ${result.data.players.size}")

                    _uiState.value = _uiState.value.copy(
                        isLoadingCompetition = false,
                        competitionData = result.data,
                        competitionSuccessMessage = "✅ Competition fetched from API and saved to database! Found ${result.data.players.size} players"
                    )
                }
                is NetworkResult.Error -> {
                    android.util.Log.e("DebugViewModel", "❌ ERROR: Failed to fetch and save competition: ${result.error}")
                    
                    if (result.error is NetworkError.HttpError && result.error.code == 401 && result.error.isRefreshFailure) {
                        handleAuthenticationFailure()
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoadingCompetition = false,
                        competitionErrorMessage = result.error.toUserMessage()
                    )
                }
                is NetworkResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun getFees() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingFees = true,
                feesErrorMessage = null,
                feesSuccessMessage = null
            )

            android.util.Log.d("DebugViewModel", "=== FETCHING FEES AND SAVING TO DATABASE ===")

            when (val result = fetchAndSaveFeesUseCase()) {
                is NetworkResult.Success -> {
                    android.util.Log.d("DebugViewModel", "✅ SUCCESS: Fees fetched from API and saved to database")
                    android.util.Log.d("DebugViewModel", "  Fees count: ${result.data.size}")

                    result.data.forEach { fee ->
                        android.util.Log.d("DebugViewModel", "  Fee: ${fee.description} - ${fee.numberHoles} holes - $${fee.cost}")
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoadingFees = false,
                        feesData = result.data,
                        feesSuccessMessage = "✅ Fees fetched from API and saved to database! Found ${result.data.size} fees"
                    )
                }
                is NetworkResult.Error -> {
                    android.util.Log.e("DebugViewModel", "❌ ERROR: Failed to fetch and save fees: ${result.error}")
                    _uiState.value = _uiState.value.copy(
                        isLoadingFees = false,
                        feesErrorMessage = result.error.toUserMessage()
                    )
                }
                is NetworkResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    // NEW: Debug method to show current club info
    fun debugClubInfo() {
        viewModelScope.launch {
            try {
                android.util.Log.d("DebugViewModel", "=== CURRENT CLUB INFO ===")

                val selectedClub = getMslClubAndTenantIdsUseCase()
                if (selectedClub != null) {
                    android.util.Log.d("DebugViewModel", "Club ID: ${selectedClub.clubId}")
                    android.util.Log.d("DebugViewModel", "Tenant ID: ${selectedClub.tenantId}")

                    _uiState.value = _uiState.value.copy(
                        gameSuccessMessage = "✅ Current club: ID=${selectedClub.clubId}, TenantID=${selectedClub.tenantId}"
                    )
                } else {
                    android.util.Log.d("DebugViewModel", "No club selected")
                    _uiState.value = _uiState.value.copy(
                        gameErrorMessage = "❌ No club selected. Please login first."
                    )
                }

                // Also check if we have a golfer
                val golfer = currentGolfer.value
                if (golfer != null) {
                    android.util.Log.d("DebugViewModel", "Current golfer: ${golfer.firstName} ${golfer.surname} (${golfer.golfLinkNo})")
                } else {
                    android.util.Log.d("DebugViewModel", "No golfer data available")
                }

            } catch (e: Exception) {
                android.util.Log.e("DebugViewModel", "Error getting club info", e)
                _uiState.value = _uiState.value.copy(
                    gameErrorMessage = "Error getting club info: ${e.message}"
                )
            }
        }
    }

    // Method: Database only - EXACT same pattern as game
    fun getCompetitionFromLocalDatabase() {
        viewModelScope.launch {
            try {
                android.util.Log.d("DebugViewModel", "=== TESTING LOCAL DATABASE COMPETITION ===")

                val competitionFromDb = getLocalCompetitionUseCase().first()

                if (competitionFromDb != null) {
                    android.util.Log.d("DebugViewModel", "✅ SUCCESS: Retrieved competition from LOCAL DATABASE:")
                    android.util.Log.d("DebugViewModel", "  Players count: ${competitionFromDb.players.size}")

                    _uiState.value = _uiState.value.copy(
                        competitionData = competitionFromDb,
                        competitionSuccessMessage = "✅ Competition retrieved from LOCAL DATABASE: ${competitionFromDb.players.size} players"
                    )
                } else {
                    android.util.Log.w("DebugViewModel", "❌ No competition found in local database!")
                    _uiState.value = _uiState.value.copy(
                        competitionErrorMessage = "No competition found in local database. Please fetch competition data first."
                    )
                }

            } catch (e: Exception) {
                android.util.Log.e("DebugViewModel", "❌ ERROR retrieving competition from local database", e)
                _uiState.value = _uiState.value.copy(
                    competitionErrorMessage = "Error retrieving competition from local database: ${e.message}"
                )
            }
        }
    }

    // ✅ TEST METHOD: Get golfer from LOCAL DATABASE ONLY
    fun getGolferFromLocalDatabase() {
        viewModelScope.launch {
            try {
                android.util.Log.d("DebugViewModel", "=== TESTING LOCAL DATABASE GOLFER ===")

                // Check if we have any golfer in database
                val hasGolfer = mslGolferLocalDbRepository.hasGolfer()
                android.util.Log.d("DebugViewModel", "Has golfer in database: $hasGolfer")

                if (!hasGolfer) {
                    android.util.Log.w("DebugViewModel", "❌ No golfer found in local database!")
                    _uiState.value = _uiState.value.copy(
                        gameErrorMessage = "No golfer found in local database. Please login first."
                    )
                    return@launch
                }

                // Get golfer from local database
                val golferFromDb = mslGolferLocalDbRepository.getCurrentGolfer().first()

                if (golferFromDb != null) {
                    android.util.Log.d("DebugViewModel", "✅ SUCCESS: Retrieved golfer from LOCAL DATABASE:")
                    android.util.Log.d("DebugViewModel", "  First Name: ${golferFromDb.firstName}")
                    android.util.Log.d("DebugViewModel", "  Last Name: ${golferFromDb.surname}")
                    android.util.Log.d("DebugViewModel", "  Golf Link No: ${golferFromDb.golfLinkNo}")
                    android.util.Log.d("DebugViewModel", "  Email: ${golferFromDb.email}")
                    android.util.Log.d("DebugViewModel", "  Handicap: ${golferFromDb.primary}")
                    android.util.Log.d("DebugViewModel", "  Country: ${golferFromDb.country}")
                    android.util.Log.d("DebugViewModel", "  Date of Birth: ${golferFromDb.dateOfBirth}")
                    android.util.Log.d("DebugViewModel", "  Mobile: ${golferFromDb.mobileNo}")
                    android.util.Log.d("DebugViewModel", "  State: ${golferFromDb.state}")
                    android.util.Log.d("DebugViewModel", "  Post Code: ${golferFromDb.postCode}")
                    android.util.Log.d("DebugViewModel", "  Gender: ${golferFromDb.gender}")

                    _uiState.value = _uiState.value.copy(
                        gameSuccessMessage = "✅ Golfer retrieved from LOCAL DATABASE: ${golferFromDb.firstName} ${golferFromDb.surname} (Check logs for details)"
                    )
                } else {
                    android.util.Log.w("DebugViewModel", "❌ Golfer exists in database but returned null from getCurrentGolfer()")
                    _uiState.value = _uiState.value.copy(
                        gameErrorMessage = "Golfer exists but returned null. Check database setup."
                    )
                }

            } catch (e: Exception) {
                android.util.Log.e("DebugViewModel", "❌ ERROR retrieving golfer from local database", e)
                _uiState.value = _uiState.value.copy(
                    gameErrorMessage = "Error retrieving golfer from local database: ${e.message}"
                )
            }
        }
    }

    // NEW: ✅ TEST METHOD: Get game from LOCAL DATABASE ONLY
    fun getGameFromLocalDatabase() {
        viewModelScope.launch {
            try {
                android.util.Log.d("DebugViewModel", "=== TESTING LOCAL DATABASE GAME ===")

                // Get game from local database
                val gameFromDb = getLocalGameUseCase().first()

                if (gameFromDb != null) {
                    android.util.Log.d("DebugViewModel", "✅ SUCCESS: Retrieved game from LOCAL DATABASE:")
                    android.util.Log.d("DebugViewModel", "  Competition ID: ${gameFromDb.mainCompetitionId}")
                    android.util.Log.d("DebugViewModel", "  Starting Hole: ${gameFromDb.startingHoleNumber}")
                    android.util.Log.d("DebugViewModel", "  Golfer Link No: ${gameFromDb.golflinkNumber}")
                    android.util.Log.d("DebugViewModel", "  Tee Name: ${gameFromDb.teeName}")
                    android.util.Log.d("DebugViewModel", "  Tee Colour: ${gameFromDb.teeColour}")
                    android.util.Log.d("DebugViewModel", "  Daily Handicap: ${gameFromDb.dailyHandicap}")
                    android.util.Log.d("DebugViewModel", "  GA Handicap: ${gameFromDb.gaHandicap}")
                    android.util.Log.d("DebugViewModel", "  Number of Holes: ${gameFromDb.numberOfHoles}")
                    android.util.Log.d("DebugViewModel", "  Playing Partners: ${gameFromDb.playingPartners.size}")
                    android.util.Log.d("DebugViewModel", "  Competitions: ${gameFromDb.competitions.size}")

                    gameFromDb.playingPartners.forEach { partner ->
                        android.util.Log.d("DebugViewModel", "    Partner: ${partner.firstName} ${partner.lastName} (${partner.golfLinkNumber})")
                    }

                    gameFromDb.competitions.forEach { competition ->
                        android.util.Log.d("DebugViewModel", "    Competition: ${competition.name} (${competition.type})")
                    }

                    _uiState.value = _uiState.value.copy(
                        gameData = gameFromDb,
                        gameSuccessMessage = "✅ Game retrieved from LOCAL DATABASE: Competition ${gameFromDb.mainCompetitionId} (Check logs for details)"
                    )
                } else {
                    android.util.Log.w("DebugViewModel", "❌ No game found in local database!")
                    _uiState.value = _uiState.value.copy(
                        gameErrorMessage = "No game found in local database. Please fetch game data first."
                    )
                }

            } catch (e: Exception) {
                android.util.Log.e("DebugViewModel", "❌ ERROR retrieving game from local database", e)
                _uiState.value = _uiState.value.copy(
                    gameErrorMessage = "Error retrieving game from local database: ${e.message}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            gameErrorMessage = null,
            gameSuccessMessage = null,
            competitionErrorMessage = null,
            competitionSuccessMessage = null,
            feesErrorMessage = null,
            feesSuccessMessage = null
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
        val competition = _uiState.value.competitionData
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

    // NEW: Example method showing how to use game data
    fun getGameSummary(): String {
        val game = _uiState.value.gameData ?: localGame.value
        return when {
            game == null -> "No game data available"
            else -> {
                val partnersCount = game.playingPartners.size
                val competitionsCount = game.competitions.size
                "Game: Competition ${game.mainCompetitionId}, Hole ${game.startingHoleNumber}, $partnersCount partners, $competitionsCount competitions"
            }
        }
    }

    fun testClubStorage() {
        viewModelScope.launch {
            try {
                android.util.Log.d("DebugViewModel", "=== TESTING CLUB STORAGE ===")

                // Test 1: Check current stored club
                android.util.Log.d("DebugViewModel", "1. Checking current stored club...")
                val currentClub = getMslClubAndTenantIdsUseCase()
                android.util.Log.d("DebugViewModel", "Current stored club: $currentClub")

                // Test 2: Check if we have a club
                val hasClub = getMslClubAndTenantIdsUseCase.hasSelectedClub()
                android.util.Log.d("DebugViewModel", "Has selected club: $hasClub")

                // Test 3: Get individual values
                val clubId = getMslClubAndTenantIdsUseCase.getClubId()
                val tenantId = getMslClubAndTenantIdsUseCase.getTenantId()
                android.util.Log.d("DebugViewModel", "Individual - Club ID: $clubId, Tenant ID: $tenantId")

                // Test 4: Try to store a test club
                android.util.Log.d("DebugViewModel", "2. Storing a test club...")
                val testClub = com.sogo.golf.msl.domain.model.msl.MslClub(
                    clubId = 670229,
                    name = "Test Golf Club",
                    logoUrl = null,
                    tenantId = "testgolfclub",
                    latitude = 0,
                    longitude = 0,
                    isGuestRegistrationEnabled = false,
                    isChappGuestRegistrationEnabled = false,
                    posLocationId = "",
                    posTerminalId = "",
                    resourceId = ""
                )

                val storeResult = setSelectedClubUseCase(testClub)
                android.util.Log.d("DebugViewModel", "Store result: ${storeResult.isSuccess}")
                if (storeResult.isFailure) {
                    android.util.Log.e("DebugViewModel", "Store failed", storeResult.exceptionOrNull())
                }

                // Test 5: Check if the store worked
                android.util.Log.d("DebugViewModel", "3. Checking after store...")
                val afterStoreClub = getMslClubAndTenantIdsUseCase()
                android.util.Log.d("DebugViewModel", "After store club: $afterStoreClub")

                val afterStoreHasClub = getMslClubAndTenantIdsUseCase.hasSelectedClub()
                android.util.Log.d("DebugViewModel", "After store has club: $afterStoreHasClub")

                // Test 6: Check individual values again
                val afterStoreClubId = getMslClubAndTenantIdsUseCase.getClubId()
                val afterStoreTenantId = getMslClubAndTenantIdsUseCase.getTenantId()
                android.util.Log.d("DebugViewModel", "After store - Club ID: $afterStoreClubId, Tenant ID: $afterStoreTenantId")

                // Update UI with test results
                _uiState.value = _uiState.value.copy(
                    gameSuccessMessage = if (afterStoreClub != null) {
                        "✅ Club storage test PASSED: ${afterStoreClub.clubId} - ${afterStoreClub.tenantId}"
                    } else {
                        "❌ Club storage test FAILED: Still returning null"
                    }
                )

            } catch (e: Exception) {
                android.util.Log.e("DebugViewModel", "❌ Exception during club storage test", e)
                _uiState.value = _uiState.value.copy(
                    gameErrorMessage = "Club storage test failed: ${e.message}"
                )
            }
        }
    }

    // In DebugViewModel.kt - add this test method
    fun testDateValidation() {
        viewModelScope.launch {
            try {
                android.util.Log.d("DebugViewModel", "=== TESTING DATE VALIDATION ===")

                // Get current stored date
                val storedDate = gameDataTimestampPreferences.getGameDataDate()
                val todayDate = DateUtils.getTodayDateString()

                android.util.Log.d("DebugViewModel", "Stored date: $storedDate")
                android.util.Log.d("DebugViewModel", "Today's date: $todayDate")

                // Test validation
                val isDataFresh = validateGameDataFreshnessUseCase()
                android.util.Log.d("DebugViewModel", "Is data fresh: $isDataFresh")

                if (isDataFresh) {
                    _uiState.value = _uiState.value.copy(
                        gameSuccessMessage = "✅ Data is FRESH for today ($todayDate)"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        gameErrorMessage = "📅 Data is STALE - stored: $storedDate, today: $todayDate"
                    )

                    // Trigger reset
                    android.util.Log.d("DebugViewModel", "Triggering stale data reset...")
                    when (val result = resetStaleDataUseCase()) {
                        is Result -> {
                            if (result.isSuccess) {
                                _uiState.value = _uiState.value.copy(
                                    gameSuccessMessage = "✅ Stale data reset completed successfully!"
                                )
                            } else {
                                _uiState.value = _uiState.value.copy(
                                    gameErrorMessage = "❌ Stale data reset failed: ${result.exceptionOrNull()?.message}"
                                )
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                android.util.Log.e("DebugViewModel", "Error testing date validation", e)
                _uiState.value = _uiState.value.copy(
                    gameErrorMessage = "Error testing date validation: ${e.message}"
                )
            }
        }
    }

    fun setDebugStoredDate(date: String) {
        viewModelScope.launch {
            gameDataTimestampPreferences.saveGameDataDate(date)
            android.util.Log.d("DebugViewModel", "🔧 DEBUG: Set stored game data date to: $date")
        }
    }

    private fun handleAuthenticationFailure() {
        viewModelScope.launch {
            try {
                authRepository.logout()
                android.util.Log.d("DebugViewModel", "🔓 User logged out due to authentication failure")
            } catch (e: Exception) {
                android.util.Log.e("DebugViewModel", "❌ Failed to logout after auth failure", e)
            }
        }
    }
}
