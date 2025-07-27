// Update to app/src/main/java/com/sogo/golf/msl/features/home/presentation/HomeViewModel.kt
package com.sogo.golf.msl.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.repository.MslGolferLocalDbRepository
import com.sogo.golf.msl.domain.usecase.club.GetMslClubAndTenantIdsUseCase
import com.sogo.golf.msl.domain.usecase.club.SetSelectedClubUseCase
import com.sogo.golf.msl.domain.usecase.competition.GetCompetitionUseCase
import com.sogo.golf.msl.domain.usecase.game.FetchAndSaveGameUseCase
import com.sogo.golf.msl.domain.usecase.game.GetGameUseCase
import com.sogo.golf.msl.domain.usecase.game.GetLocalGameUseCase
import com.sogo.golf.msl.domain.usecase.msl_golfer.GetMslGolferUseCase
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
class HomeViewModel @Inject constructor(
    private val getGameUseCase: GetGameUseCase,
    val getMslGolferUseCase: GetMslGolferUseCase,
    private val getCompetitionUseCase: GetCompetitionUseCase,
    private val mslGolferLocalDbRepository: MslGolferLocalDbRepository,
    // NEW: Add game storage use cases
    private val getLocalGameUseCase: GetLocalGameUseCase,
    private val fetchAndSaveGameUseCase: FetchAndSaveGameUseCase,
    private val getMslClubAndTenantIdsUseCase: GetMslClubAndTenantIdsUseCase,
    private val setSelectedClubUseCase: SetSelectedClubUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

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

    fun getGame(clubId: String = "670229") { // Default game ID for testing
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingGame = true,
                gameErrorMessage = null,
                gameSuccessMessage = null
            )

            when (val result = getGameUseCase(clubId)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingGame = false,
                        gameData = result.data,
                        gameSuccessMessage = "Game data loaded from API successfully!"
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
    fun getGameAndSaveToDatabase(clubId: String = "670229") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingGame = true,
                gameErrorMessage = null,
                gameSuccessMessage = null
            )

            android.util.Log.d("HomeViewModel", "=== FETCHING GAME AND SAVING TO DATABASE ===")
            android.util.Log.d("HomeViewModel", "Club ID: $clubId")

            when (val result = fetchAndSaveGameUseCase(clubId)) {
                is NetworkResult.Success -> {
                    android.util.Log.d("HomeViewModel", "✅ SUCCESS: Game fetched from API and saved to database")
                    android.util.Log.d("HomeViewModel", "  Competition ID: ${result.data.mainCompetitionId}")
                    android.util.Log.d("HomeViewModel", "  Starting Hole: ${result.data.startingHoleNumber}")
                    android.util.Log.d("HomeViewModel", "  Playing Partners: ${result.data.playingPartners.size}")
                    android.util.Log.d("HomeViewModel", "  Competitions: ${result.data.competitions.size}")

                    _uiState.value = _uiState.value.copy(
                        isLoadingGame = false,
                        gameData = result.data,
                        gameSuccessMessage = "✅ Game fetched from API and saved to database! Competition ID: ${result.data.mainCompetitionId}"
                    )
                }
                is NetworkResult.Error -> {
                    android.util.Log.e("HomeViewModel", "❌ ERROR: Failed to fetch and save game: ${result.error}")
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

    fun getCompetition(clubId: String = "670229") { // Default club ID for testing (same as game)
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingCompetition = true,
                competitionErrorMessage = null,
                competitionSuccessMessage = null
            )

            android.util.Log.d("HomeViewModel", "=== GETTING COMPETITION FROM API ===")
            android.util.Log.d("HomeViewModel", "Club ID: $clubId")

            when (val result = getCompetitionUseCase(clubId)) {
                is NetworkResult.Success -> {
                    android.util.Log.d("HomeViewModel", "✅ SUCCESS: Retrieved competition from API:")
                    android.util.Log.d("HomeViewModel", "  Players count: ${result.data.players.size}")
                    result.data.players.forEach { player ->
                        android.util.Log.d("HomeViewModel", "  Player: ${player.firstName} ${player.lastName} - ${player.competitionName}")
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoadingCompetition = false,
                        competitionData = result.data,
                        competitionSuccessMessage = "✅ Competition loaded from API! Found ${result.data.players.size} players"
                    )
                }
                is NetworkResult.Error -> {
                    android.util.Log.e("HomeViewModel", "❌ ERROR: Failed to get competition from API: ${result.error}")
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

    // ✅ TEST METHOD: Get golfer from LOCAL DATABASE ONLY
    fun getGolferFromLocalDatabase() {
        viewModelScope.launch {
            try {
                android.util.Log.d("HomeViewModel", "=== TESTING LOCAL DATABASE GOLFER ===")

                // Check if we have any golfer in database
                val hasGolfer = mslGolferLocalDbRepository.hasGolfer()
                android.util.Log.d("HomeViewModel", "Has golfer in database: $hasGolfer")

                if (!hasGolfer) {
                    android.util.Log.w("HomeViewModel", "❌ No golfer found in local database!")
                    _uiState.value = _uiState.value.copy(
                        gameErrorMessage = "No golfer found in local database. Please login first."
                    )
                    return@launch
                }

                // Get golfer from local database
                val golferFromDb = mslGolferLocalDbRepository.getCurrentGolfer().first()

                if (golferFromDb != null) {
                    android.util.Log.d("HomeViewModel", "✅ SUCCESS: Retrieved golfer from LOCAL DATABASE:")
                    android.util.Log.d("HomeViewModel", "  First Name: ${golferFromDb.firstName}")
                    android.util.Log.d("HomeViewModel", "  Last Name: ${golferFromDb.surname}")
                    android.util.Log.d("HomeViewModel", "  Golf Link No: ${golferFromDb.golfLinkNo}")
                    android.util.Log.d("HomeViewModel", "  Email: ${golferFromDb.email}")
                    android.util.Log.d("HomeViewModel", "  Handicap: ${golferFromDb.primary}")
                    android.util.Log.d("HomeViewModel", "  Country: ${golferFromDb.country}")
                    android.util.Log.d("HomeViewModel", "  Date of Birth: ${golferFromDb.dateOfBirth}")
                    android.util.Log.d("HomeViewModel", "  Mobile: ${golferFromDb.mobileNo}")
                    android.util.Log.d("HomeViewModel", "  State: ${golferFromDb.state}")
                    android.util.Log.d("HomeViewModel", "  Post Code: ${golferFromDb.postCode}")
                    android.util.Log.d("HomeViewModel", "  Gender: ${golferFromDb.gender}")

                    _uiState.value = _uiState.value.copy(
                        gameSuccessMessage = "✅ Golfer retrieved from LOCAL DATABASE: ${golferFromDb.firstName} ${golferFromDb.surname} (Check logs for details)"
                    )
                } else {
                    android.util.Log.w("HomeViewModel", "❌ Golfer exists in database but returned null from getCurrentGolfer()")
                    _uiState.value = _uiState.value.copy(
                        gameErrorMessage = "Golfer exists but returned null. Check database setup."
                    )
                }

            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "❌ ERROR retrieving golfer from local database", e)
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
                android.util.Log.d("HomeViewModel", "=== TESTING LOCAL DATABASE GAME ===")

                // Get game from local database
                val gameFromDb = getLocalGameUseCase().first()

                if (gameFromDb != null) {
                    android.util.Log.d("HomeViewModel", "✅ SUCCESS: Retrieved game from LOCAL DATABASE:")
                    android.util.Log.d("HomeViewModel", "  Competition ID: ${gameFromDb.mainCompetitionId}")
                    android.util.Log.d("HomeViewModel", "  Starting Hole: ${gameFromDb.startingHoleNumber}")
                    android.util.Log.d("HomeViewModel", "  Golfer Link No: ${gameFromDb.golflinkNumber}")
                    android.util.Log.d("HomeViewModel", "  Tee Name: ${gameFromDb.teeName}")
                    android.util.Log.d("HomeViewModel", "  Tee Colour: ${gameFromDb.teeColour}")
                    android.util.Log.d("HomeViewModel", "  Daily Handicap: ${gameFromDb.dailyHandicap}")
                    android.util.Log.d("HomeViewModel", "  GA Handicap: ${gameFromDb.gaHandicap}")
                    android.util.Log.d("HomeViewModel", "  Number of Holes: ${gameFromDb.numberOfHoles}")
                    android.util.Log.d("HomeViewModel", "  Playing Partners: ${gameFromDb.playingPartners.size}")
                    android.util.Log.d("HomeViewModel", "  Competitions: ${gameFromDb.competitions.size}")

                    gameFromDb.playingPartners.forEach { partner ->
                        android.util.Log.d("HomeViewModel", "    Partner: ${partner.firstName} ${partner.lastName} (${partner.golfLinkNumber})")
                    }

                    gameFromDb.competitions.forEach { competition ->
                        android.util.Log.d("HomeViewModel", "    Competition: ${competition.name} (${competition.type})")
                    }

                    _uiState.value = _uiState.value.copy(
                        gameData = gameFromDb,
                        gameSuccessMessage = "✅ Game retrieved from LOCAL DATABASE: Competition ${gameFromDb.mainCompetitionId} (Check logs for details)"
                    )
                } else {
                    android.util.Log.w("HomeViewModel", "❌ No game found in local database!")
                    _uiState.value = _uiState.value.copy(
                        gameErrorMessage = "No game found in local database. Please fetch game data first."
                    )
                }

            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "❌ ERROR retrieving game from local database", e)
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
            competitionSuccessMessage = null
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
                android.util.Log.d("HomeViewModel", "=== TESTING CLUB STORAGE ===")

                // Test 1: Check current stored club
                android.util.Log.d("HomeViewModel", "1. Checking current stored club...")
                val currentClub = getMslClubAndTenantIdsUseCase()
                android.util.Log.d("HomeViewModel", "Current stored club: $currentClub")

                // Test 2: Check if we have a club
                val hasClub = getMslClubAndTenantIdsUseCase.hasSelectedClub()
                android.util.Log.d("HomeViewModel", "Has selected club: $hasClub")

                // Test 3: Get individual values
                val clubId = getMslClubAndTenantIdsUseCase.getClubId()
                val tenantId = getMslClubAndTenantIdsUseCase.getTenantId()
                android.util.Log.d("HomeViewModel", "Individual - Club ID: $clubId, Tenant ID: $tenantId")

                // Test 4: Try to store a test club
                android.util.Log.d("HomeViewModel", "2. Storing a test club...")
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
                android.util.Log.d("HomeViewModel", "Store result: ${storeResult.isSuccess}")
                if (storeResult.isFailure) {
                    android.util.Log.e("HomeViewModel", "Store failed", storeResult.exceptionOrNull())
                }

                // Test 5: Check if the store worked
                android.util.Log.d("HomeViewModel", "3. Checking after store...")
                val afterStoreClub = getMslClubAndTenantIdsUseCase()
                android.util.Log.d("HomeViewModel", "After store club: $afterStoreClub")

                val afterStoreHasClub = getMslClubAndTenantIdsUseCase.hasSelectedClub()
                android.util.Log.d("HomeViewModel", "After store has club: $afterStoreHasClub")

                // Test 6: Check individual values again
                val afterStoreClubId = getMslClubAndTenantIdsUseCase.getClubId()
                val afterStoreTenantId = getMslClubAndTenantIdsUseCase.getTenantId()
                android.util.Log.d("HomeViewModel", "After store - Club ID: $afterStoreClubId, Tenant ID: $afterStoreTenantId")

                // Update UI with test results
                _uiState.value = _uiState.value.copy(
                    gameSuccessMessage = if (afterStoreClub != null) {
                        "✅ Club storage test PASSED: ${afterStoreClub.clubId} - ${afterStoreClub.tenantId}"
                    } else {
                        "❌ Club storage test FAILED: Still returning null"
                    }
                )

            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "❌ Exception during club storage test", e)
                _uiState.value = _uiState.value.copy(
                    gameErrorMessage = "Club storage test failed: ${e.message}"
                )
            }
        }
    }
}