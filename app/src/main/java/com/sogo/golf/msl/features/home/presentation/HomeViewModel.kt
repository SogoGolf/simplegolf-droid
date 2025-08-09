// app/src/main/java/com/sogo/golf/msl/features/home/presentation/HomeViewModel.kt
package com.sogo.golf.msl.features.home.presentation

import android.util.Log
import android.util.Patterns
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
import com.sogo.golf.msl.data.network.api.CreateGolferRequestDto
import com.sogo.golf.msl.data.network.api.UpdateGolferRequestDto
import com.sogo.golf.msl.domain.model.mongodb.SogoGolfer
import com.sogo.golf.msl.domain.usecase.msl_golfer.GetMslGolferUseCase
import com.sogo.golf.msl.domain.usecase.sogo_golfer.CreateGolferUseCase
import com.sogo.golf.msl.domain.usecase.sogo_golfer.FetchAndSaveSogoGolferUseCase
import com.sogo.golf.msl.domain.usecase.sogo_golfer.GetSogoGolferUseCase
import com.sogo.golf.msl.domain.usecase.sogo_golfer.UpdateGolferUseCase
import com.sogo.golf.msl.features.sogo_home.presentation.state.CountryDataState
import com.sogo.golf.msl.features.sogo_home.presentation.state.SogoGolferDataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.combine
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
        private val fetchAndSaveSogoGolferUseCase: FetchAndSaveSogoGolferUseCase,
        private val getSogoGolferUseCase: GetSogoGolferUseCase,
        private val createGolferUseCase: CreateGolferUseCase,
        private val updateGolferUseCase: UpdateGolferUseCase,
    ) : ViewModel() {

        companion object {
            private const val TAG = "HomeViewModel"
        }

        // ✅ ADD REQUIRED STATE FOR GOLFER DATA CONFIRMATION SHEET
        private val _sogoGolferDataState = MutableStateFlow(SogoGolferDataState())
        val sogoGolferDataState: StateFlow<SogoGolferDataState> = _sogoGolferDataState.asStateFlow()

        private val _countryDataState = MutableStateFlow(CountryDataState())
        val countryDataState: StateFlow<CountryDataState> = _countryDataState.asStateFlow()

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

        // ✅ SOGO GOLFER ACCESS - Combine both currentGolfer and localGame flows
        val sogoGolfer = kotlinx.coroutines.flow.combine(currentGolfer, localGame) { golfer, game ->
            Log.d(TAG, "=== SOGO GOLFER FLOW DEBUG ===")
            Log.d(TAG, "golfer: $golfer")
            Log.d(TAG, "golfer.golfLinkNo: ${golfer?.golfLinkNo}")
            Log.d(TAG, "game: $game")
            Log.d(TAG, "game.golflinkNumber: ${game?.golflinkNumber}")
            
            // Use game data as fallback if golfer's golfLinkNo is empty
            val golfLinkNo = golfer?.golfLinkNo?.takeIf { it.isNotBlank() }
                ?: game?.golflinkNumber
            
            Log.d(TAG, "Final golfLinkNo to use: $golfLinkNo")
            golfLinkNo
        }.flatMapLatest { golfLinkNo ->
            if (!golfLinkNo.isNullOrBlank()) {
                Log.d(TAG, "Calling getSogoGolferUseCase with golfLinkNo: $golfLinkNo")
                getSogoGolferUseCase(golfLinkNo)
            } else {
                Log.d(TAG, "No valid golfLinkNo available - returning null")
                flowOf(null)
            }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

        init {
            // ✅ NEW: Automatically fetch data when HomeViewModel is created
            Log.d(TAG, "=== HOME SCREEN INIT - FETCHING TODAY'S DATA ===")
            fetchTodaysData()
        }

// NEW: Fetch today's game and competition data
        private fun fetchTodaysData() {
            viewModelScope.launch {
                try {
                    Log.d(TAG, "Starting to fetch today's data...")

                    // Set loading state
                    _uiState.value = _uiState.value.copy(
                        isLoading = true,
                        errorMessage = null,
                        successMessage = null,
                        progressMessage = "Preparing…",
                        progressPercent = 0
                    )

                    // Get the selected club
                    val selectedClub = getMslClubAndTenantIdsUseCase()
                    if (selectedClub?.clubId == null) {
                        Log.w(TAG, "⚠️ No club selected - cannot fetch data")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "No club selected. Please login again."
                        )
                        return@launch
                    }

                    val clubIdStr = selectedClub.clubId.toString()
                    Log.d(TAG, "Fetching data for MSL club: $clubIdStr")

                    // Fetch both game and competition data in parallel
                    // Declare all success/error variables
                    var gameSuccess = false
                    var competitionSuccess = false
                    var feesSuccess = false
                    var sogoGolfSuccess = false
                    var gameError: String? = null
                    var competitionError: String? = null
                    var feesError: String? = null
                    var sogoGolferError: String? = null

                    // Fetch Game Data
                    Log.d(TAG, "🎮 Fetching game data...")
                    _uiState.value = _uiState.value.copy(progressMessage = "Downloading game…", progressPercent = 25)
                    when (val gameResult = fetchAndSaveGameUseCase(clubIdStr)) {
                        is NetworkResult.Success -> {
                            Log.d(TAG, "✅ MSL Game data fetched successfully: Competition ${gameResult.data.mainCompetitionId}")
                            Log.d(TAG, "🔍 DEBUG: Game startingHoleNumber: ${gameResult.data.startingHoleNumber}")
                            Log.d(TAG, "🔍 DEBUG: Game numberOfHoles: ${gameResult.data.numberOfHoles}")
                            gameSuccess = true
                        }
                        is NetworkResult.Error -> {
                            gameError = gameResult.error.toUserMessage()
                            Log.e(TAG, "❌ Failed to fetch MSL game data: $gameError")
                        }
                        is NetworkResult.Loading -> { /* Already handled */ }
                    }

                    // Fetch Competition Data
                    Log.d(TAG, "🏆 Fetching competition data...")
                    _uiState.value = _uiState.value.copy(progressMessage = "Downloading competition…", progressPercent = 55)
                    when (val competitionResult = fetchAndSaveCompetitionUseCase(clubIdStr)) {
                        is NetworkResult.Success -> {
                            Log.d(TAG, "✅ MSL Competition data fetched successfully: ${competitionResult.data.players.size} players")
                            competitionSuccess = true
                        }
                        is NetworkResult.Error -> {
                            competitionError = competitionResult.error.toUserMessage()
                            Log.e(TAG, "❌ Failed to fetch MSL competition data: $competitionError")
                        }
                        is NetworkResult.Loading -> { /* Already handled */ }
                    }

                    // ✅ NEW: Fetch Fees Data
                    Log.d(TAG, "💰 Fetching SOGO fees data...")
                    _uiState.value = _uiState.value.copy(progressMessage = "Downloading fees…", progressPercent = 75)
                    when (val feesResult = fetchAndSaveFeesUseCase()) {
                        is NetworkResult.Success -> {
                            Log.d(TAG, "✅ SOGO Fees data fetched successfully: ${feesResult.data.size} fees")
                            feesSuccess = true
                        }
                        is NetworkResult.Error -> {
                            feesError = feesResult.error.toUserMessage()
                            Log.e(TAG, "❌ Failed to fetch SOGO fees data: $feesError")
                        }
                        is NetworkResult.Loading -> { /* Already handled */ }
                    }

                    // ✅Fetch SogoGolfer Data
                    Log.d(TAG, "👤 Fetching sogo golfer data...")
                    _uiState.value = _uiState.value.copy(progressMessage = "Downloading golfer…", progressPercent = 90)
                    // Priority order: existing SogoGolfer -> MSL golfer -> game data
                    val golfLinkNo = sogoGolfer.value?.golfLinkNo?.takeIf { it.isNotBlank() }
                        ?: currentGolfer.value?.golfLinkNo?.takeIf { it.isNotBlank() }
                        ?: localGame.value?.golflinkNumber
                    
                    Log.d(TAG, "sogoGolfer.value?.golfLinkNo: ${sogoGolfer.value?.golfLinkNo}")
                    Log.d(TAG, "currentGolfer.value?.golfLinkNo: ${currentGolfer.value?.golfLinkNo}")
                    Log.d(TAG, "localGame.value?.golflinkNumber: ${localGame.value?.golflinkNumber}")
                    Log.d(TAG, "Final golfLinkNo for fetch: $golfLinkNo")
                    
                    // Log current local token balance before refresh
                    Log.d(TAG, "💰 LOCAL TOKEN BALANCE (before refresh): ${sogoGolfer.value?.tokenBalance}")
                    
                    if (golfLinkNo.isNullOrBlank()) {
                        Log.w(TAG, "⚠️ No golfLinkNo available - skipping sogo golfer fetch")
                        sogoGolferError = "No golf link number available from either golfer or game data"
                    } else {
                        // Fetch the sogo golfer data
                        when (val sogoGolferResult = fetchAndSaveSogoGolferUseCase(golfLinkNo)) {
                            is NetworkResult.Success -> {
                                Log.d(TAG, "✅ Sogo golfer data fetched successfully: ${sogoGolferResult.data.email}")
                                Log.d(TAG, "💰 REMOTE TOKEN BALANCE (from API): ${sogoGolferResult.data.tokenBalance}")
                                // Give a moment for the local flow to update
                                kotlinx.coroutines.delay(100)
                                Log.d(TAG, "💰 LOCAL TOKEN BALANCE (after refresh): ${sogoGolfer.value?.tokenBalance}")
                                sogoGolfSuccess = true
                            }
                            is NetworkResult.Error -> {
                                sogoGolferError = sogoGolferResult.error.toUserMessage() // Fixed: was incorrectly setting feesError
                                Log.e(TAG, "❌ Failed to fetch sogo golfer data: $sogoGolferError")
                            }
                            is NetworkResult.Loading -> {
                                Log.d(TAG, "⏳ Sogo golfer data is loading...")
                                // Handle loading state if needed
                            }
                        }
                    }


                    // Update UI state based on results
                    when {
                        gameSuccess && competitionSuccess -> {
                            Log.d(TAG, "✅ All data fetched successfully!")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "Today's golf data loaded successfully!",
                                progressMessage = null,
                                progressPercent = 100
                            )
                        }
                        gameSuccess && !competitionSuccess -> {
                            Log.w(TAG, "⚠️MSL Game data fetched but competition failed")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "MSL Game data loaded successfully",
                                errorMessage = "MSL Competition data failed: $competitionError",
                                progressMessage = null,
                                progressPercent = null
                            )
                        }
                        !gameSuccess && competitionSuccess -> {
                            Log.w(TAG, "⚠️ MSL Competition data fetched but game failed")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "MSL Competition data loaded successfully",
                                errorMessage = "MSL Game data failed: $gameError",
                                progressMessage = null,
                                progressPercent = null
                            )
                        }
                        else -> {
                            Log.e(TAG, "❌ Both data fetches failed")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "Failed to load today's data. Game: $gameError, Competition: $competitionError",
                                progressMessage = null,
                                progressPercent = null
                            )
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Exception while fetching today's data", e)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error loading today's data: ${e.message}",
                        progressMessage = null,
                        progressPercent = null
                    )
                }
            }
        }

        // NEW: Method to manually retry fetching data
        fun retryFetchData() {
            Log.d(TAG, "🔄 Manual retry requested")
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
                    "MSL Competition: $competitionName ($competitionType) with $playerCount players"
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
                    "MSL Game: MSL Competition ${game.mainCompetitionId}, Hole ${game.startingHoleNumber}, $partnersCount partners, $competitionsCount competitions"
                }
            }
        }

        fun checkForUpdatesAndStartCompetition(
            activity: android.app.Activity,
            activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
            onUpdateCheckComplete: () -> Unit,
            onNoUpdateRequired: () -> Unit
        ) {
            Log.d(TAG, "=== CHECKING FOR UPDATES BEFORE STARTING COMPETITION ===")

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

            Log.d(TAG, "Data status - Golfer: $hasGolfer, Game: $hasGame, Competition: $hasCompetition")
            return hasGolfer && hasGame && hasCompetition
        }

        // NEW: Method to get data status summary
        fun getDataStatusSummary(): String {
            val golfer = currentGolfer.value
            val game = localGame.value
            val competition = localCompetition.value

            return buildString {
                appendLine("📊 Data Status:")
                appendLine("👤 MSL Golfer: ${if (golfer != null) "✅ ${golfer.firstName} ${golfer.surname}" else "❌ Not loaded"}")
                appendLine("🎮 MSL Game: ${if (game != null) "✅ Competition ${game.mainCompetitionId}" else "❌ Not loaded"}")
                appendLine("🏆 MSL Competition: ${if (competition != null) "✅ ${competition.players.size} players" else "❌ Not loaded"}")
            }
        }

        fun isValidAustralianPostcode(postcode: String): Boolean {
            // Australian postcodes are 4 digits long
            // They generally start with a number from 0-8 for states and territories
            // 9 is reserved for special purposes
            val postcodeRegex = "^[0-8]\\d{3}$".toRegex()
            return postcodeRegex.matches(postcode)
        }

        fun isValidMobileNumber(mobile: String): Boolean {
            val trimmedMobile = mobile.trim()

            // Regular expression for Australian mobile numbers, allowing spaces
            val mobileRegex = """^04\d{2}(\s?\d{3}){2}$|^\+614\d{2}(\s?\d{3}){2}$|^614\d{2}(\s?\d{3}){2}$""".toRegex()

            return mobileRegex.matches(trimmedMobile)
        }

        fun isValidEmail(email: String): Boolean {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        /**
         * Validates if the given postcode matches the selected Australian state
         * @param postcode 4-digit Australian postcode
         * @param state Australian state abbreviation (NSW, VIC, QLD, WA, SA, TAS, ACT, NT)
         * @return true if postcode matches the state, false otherwise
         */
        fun isPostcodeValidForState(postcode: String, state: String): Boolean {
            if (!isValidAustralianPostcode(postcode)) return false
            
            val postcodeInt = postcode.toIntOrNull() ?: return false
            
            return when (state.uppercase()) {
                "NSW" -> postcodeInt in 1000..2599 || postcodeInt in 2619..2899 || postcodeInt in 2921..2999
                "ACT" -> postcodeInt in 2600..2618 || postcodeInt in 2900..2920
                "VIC" -> postcodeInt in 3000..3999 || postcodeInt in 8000..8999
                "QLD" -> postcodeInt in 4000..4999 || postcodeInt in 9000..9999
                "SA" -> postcodeInt in 5000..5999
                "WA" -> postcodeInt in 6000..6797 || postcodeInt in 6800..6999
                "TAS" -> postcodeInt in 7000..7999
                "NT" -> postcodeInt in 800..999
                else -> false
            }
        }

    suspend fun processGolferConfirmationData(
        firstName: String,
        lastName: String,
        currentEmail: String,
        state: String,
        dateOfBirth: java.util.Date,
        currentPostcode: String,
        currentMobile: String,
        sogoGender: String,
        existingSogoGolfer: SogoGolfer? = null
    ): Boolean {
        return try {
            // DEBUG: Log all available data sources
            Log.d(TAG, "=== DEBUG: processGolferConfirmationData ===")
            Log.d(TAG, "currentGolfer.value: ${currentGolfer.value}")
            Log.d(TAG, "currentGolfer.value?.golfLinkNo: ${currentGolfer.value?.golfLinkNo}")
            Log.d(TAG, "localGame.value: ${localGame.value}")
            Log.d(TAG, "localGame.value?.golflinkNumber: ${localGame.value?.golflinkNumber}")
            Log.d(TAG, "localGame.value?.mainCompetitionId: ${localGame.value?.mainCompetitionId}")
            
            // For new golfers, get golflinkNumber from game data since no golfer exists yet
            val golfLinkNo = currentGolfer.value?.golfLinkNo?.takeIf { it.isNotBlank() }
                ?: localGame.value?.golflinkNumber 
                ?: throw Exception("No golf link number available from either golfer or game data")
            
            Log.d(TAG, "Final golfLinkNo to use: $golfLinkNo")
            
            val currentMslGolfer = currentGolfer.value
            
            if (existingSogoGolfer != null) {
                Log.d(TAG, "Updating existing golfer: ${existingSogoGolfer.firstName} ${existingSogoGolfer.lastName}")
                
                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                val dateOfBirthString = dateFormat.format(dateOfBirth)
                
                val deviceManufacturer = android.os.Build.MANUFACTURER
                val deviceModel = android.os.Build.MODEL
                val deviceOS = "Android"
                val deviceOSVersion = android.os.Build.VERSION.RELEASE
                val sogoAppVersion = com.sogo.golf.msl.BuildConfig.VERSION_NAME
                
                val updateRequest = UpdateGolferRequestDto(
                    appSettings = com.sogo.golf.msl.data.network.api.AppSettingsDto(
                        isAcceptedSogoTermsAndConditions = true
                    ),
                    firstName = firstName,
                    lastName = lastName,
                    postCode = currentPostcode,
                    mobileNo = currentMobile,
                    gender = sogoGender,
                    email = currentEmail,
                    dateOfBirth = dateOfBirthString,
                    deviceModel = deviceModel,
                    deviceManufacturer = deviceManufacturer,
                    deviceOS = deviceOS,
                    deviceOSVersion = deviceOSVersion,
                    sogoAppVersion = sogoAppVersion,
                )


                when (val result = updateGolferUseCase(golfLinkNo, updateRequest)) {
                    is NetworkResult.Success -> {
                        Log.d(TAG, "✅ Golfer updated successfully")
                        true
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "❌ Failed to update golfer: ${result.error}")
                        false
                    }
                    is NetworkResult.Loading -> {
                        false
                    }
                }
            } else {
                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                val dateOfBirthString = dateFormat.format(dateOfBirth)
                
                val deviceManufacturer = android.os.Build.MANUFACTURER
                val deviceModel = android.os.Build.MODEL
                val deviceOS = "Android"
                val deviceOSVersion = android.os.Build.VERSION.RELEASE
                val sogoAppVersion = com.sogo.golf.msl.BuildConfig.VERSION_NAME
                
                val request = CreateGolferRequestDto(
                    authSystemUid = golfLinkNo,
                    country = "australia",
                    dateOfBirth = dateOfBirthString,
                    deviceManufacturer = deviceManufacturer,
                    deviceModel = deviceModel,
                    deviceOS = deviceOS,
                    deviceOSVersion = deviceOSVersion,
                    deviceToken = null,
                    email = currentEmail,
                    firstName = firstName,
                    gender = sogoGender,
                    golflinkNo = golfLinkNo,
                    isAcceptedSogoTermsAndConditions = true,
                    lastName = lastName,
                    mobileNo = currentMobile,
                    postCode = currentPostcode,
                    sogoAppVersion = sogoAppVersion,
                    state = state.uppercase()
                )
                
                when (val result = createGolferUseCase(request)) {
                    is NetworkResult.Success -> {
                        Log.d(TAG, "✅ Golfer created successfully")
                        
                        // Now fetch and save the newly created golfer locally
                        when (val fetchResult = fetchAndSaveSogoGolferUseCase(golfLinkNo)) {
                            is NetworkResult.Success -> {
                                Log.d(TAG, "✅ Newly created golfer fetched and saved locally")
                                true
                            }
                            is NetworkResult.Error -> {
                                Log.e(TAG, "❌ Failed to fetch newly created golfer: ${fetchResult.error}")
                                // Still return true since the golfer was created remotely
                                true
                            }
                            is NetworkResult.Loading -> {
                                true
                            }
                        }
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "❌ Failed to create golfer: ${result.error}")
                        false
                    }
                    is NetworkResult.Loading -> {
                        false
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception processing golfer confirmation data", e)
            false
        }
    }
}
