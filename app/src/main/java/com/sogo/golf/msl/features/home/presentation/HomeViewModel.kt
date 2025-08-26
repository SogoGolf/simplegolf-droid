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
import com.sogo.golf.msl.analytics.AnalyticsManager
import io.sentry.Sentry
import io.sentry.protocol.User
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
        private val appUpdateManager: com.sogo.golf.msl.app.update.AppUpdateManager,
        private val fetchAndSaveFeesUseCase: FetchAndSaveFeesUseCase, // ✅ ADD THIS
        private val fetchAndSaveSogoGolferUseCase: FetchAndSaveSogoGolferUseCase,
        private val getSogoGolferUseCase: GetSogoGolferUseCase,
        private val createGolferUseCase: CreateGolferUseCase,
        private val updateGolferUseCase: UpdateGolferUseCase,
        private val analyticsManager: AnalyticsManager,
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
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
        
        init {
            // Set Sentry user if SogoGolfer data already exists (for returning users)
            viewModelScope.launch {
                sogoGolfer.collect { golfer ->
                    if (golfer != null && !golfer.email.isNullOrBlank()) {
                        setSentryUser(
                            email = golfer.email,
                            golfLinkNo = golfer.golfLinkNo ?: "",
                            firstName = golfer.firstName ?: "",
                            lastName = golfer.lastName ?: ""
                        )
                        // Only set it once when we have valid data
                        return@collect
                    }
                }
            }
        }

        private var cameFromRoundSubmission = false
        
        fun setSkipDataFetch(skipDataFetch: Boolean) {
            if (!skipDataFetch) {
                // NEW: Only fetch lightweight SOGO data needed for home screen
                Log.d(TAG, "=== HOME SCREEN INIT - FETCHING SOGO DATA ONLY ===")
                // Wait a bit for currentGolfer to be available, then fetch
                viewModelScope.launch {
                    kotlinx.coroutines.delay(100) // Small delay to ensure currentGolfer is set
                    fetchSogoDataOnly()
                }
            } else {
                Log.d(TAG, "=== HOME SCREEN INIT - SKIPPING DATA FETCH (came from successful round submission) ===")
                // Set flag to indicate we came from round submission
                cameFromRoundSubmission = true
                // Set loading to false since we're not fetching
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null,
                    successMessage = "Ready to start new round",
                    progressMessage = null,
                    progressPercent = null
                )
            }
        }

        private fun fetchSogoDataOnly() {
            viewModelScope.launch {
                try {
                    Log.d(TAG, "Starting to fetch SOGO golfer data and fees...")

                    // Set loading state
                    _uiState.value = _uiState.value.copy(
                        isLoading = true,
                        errorMessage = null,
                        successMessage = null,
                        progressMessage = "Loading golfer data…",
                        progressPercent = 25
                    )

                    // ✅ Fetch Sogo Fees Data first (needed for competitions)
                    Log.d(TAG, "💰 Fetching SOGO fees data...")
                    _uiState.value = _uiState.value.copy(progressMessage = "Downloading data…", progressPercent = 50)
                    when (val feesResult = fetchAndSaveFeesUseCase()) {
                        is NetworkResult.Success -> {
                            Log.d(TAG, "✅ SOGO Fees data fetched successfully: ${feesResult.data.size} fees")
                        }
                        is NetworkResult.Error -> {
                            Log.e(TAG, "❌ Failed to fetch SOGO fees data: ${feesResult.error.toUserMessage()}")
                            // Don't fail the whole process for fees
                        }
                        is NetworkResult.Loading -> { /* Already handled */ }
                    }

                    // Fetch SogoGolfer Data
                    Log.d(TAG, "👤 Fetching sogo golfer data...")
                    _uiState.value = _uiState.value.copy(progressMessage = "Loading golfer data…", progressPercent = 75)
                    val golfLinkNo = currentGolfer.value?.golfLinkNo?.takeIf { it.isNotBlank() }
                    
                    if (golfLinkNo.isNullOrBlank()) {
                        Log.w(TAG, "⚠️ No golfLinkNo available - cannot fetch SOGO golfer data")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "No golf link number available"
                        )
                        return@launch
                    }

                    when (val sogoGolferResult = fetchAndSaveSogoGolferUseCase(golfLinkNo)) {
                        is NetworkResult.Success -> {
                            Log.d(TAG, "✅ SOGO API call succeeded: ${sogoGolferResult.data?.email ?: "No golfer found (new user)"}")
                            sogoFetchCompleted = true
                            sogoFetchError = null
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "Ready to start round",
                                progressMessage = null,
                                progressPercent = 100
                            )
                        }
                        is NetworkResult.Error -> {
                            val error = sogoGolferResult.error.toUserMessage()
                            
                            // SOGO Golfer specific: 404 means golfer doesn't exist yet (OK for new users)
                            // Check if this error message indicates a 404 not found for SOGO golfer
                            if (error.contains("SogoGolfer not found (404)") || 
                                error.contains("404") && error.contains("SogoGolfer", ignoreCase = true)) {
                                Log.d(TAG, "✅ SOGO Golfer not found (404) - No golfer exists yet (this is OK for new users)")
                                sogoFetchCompleted = true  // Mark as completed even though no golfer exists
                                sogoFetchError = null
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    successMessage = "Ready",
                                    progressMessage = null,
                                    progressPercent = 100
                                )
                            } else {
                                // Real error (network, server error, etc.)
                                Log.e(TAG, "❌ SOGO API call failed: $error")
                                sogoFetchCompleted = false
                                sogoFetchError = error
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = "Cannot connect to SOGO services: $error",
                                    progressMessage = null,
                                    progressPercent = null
                                )
                            }
                        }
                        is NetworkResult.Loading -> {
                            Log.d(TAG, "⏳ SOGO golfer data is loading...")
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Exception while fetching SOGO data", e)
                    sogoFetchCompleted = false
                    sogoFetchError = e.message
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Cannot connect to SOGO services: ${e.message}",
                        progressMessage = null,
                        progressPercent = null
                    )
                }
            }
        }

        fun trackConfirmGolferDataDisplayed(
            mslGolfer: com.sogo.golf.msl.domain.model.msl.MslGolfer,
            sogoGolfer: SogoGolfer?
        ) {
            val eventProperties = mutableMapOf<String, Any>()
            
            // MSL golfer data (from MSL API)
            mslGolfer.golfLinkNo.let { eventProperties["msl_golflink_number"] = it }
            mslGolfer.firstName.let { eventProperties["msl_first_name"] = it }
            mslGolfer.surname.let { eventProperties["msl_surname"] = it }
            mslGolfer.email?.let { eventProperties["msl_email"] = it }
            mslGolfer.state?.let { eventProperties["msl_state"] = it }
            mslGolfer.postCode?.let { eventProperties["msl_postcode"] = it }
            mslGolfer.mobileNo?.let { eventProperties["msl_mobile"] = it }
            mslGolfer.gender?.let { eventProperties["msl_gender"] = it }
            mslGolfer.dateOfBirth.let { eventProperties["msl_date_of_birth"] = it }
            
            // SOGO golfer data (existing data if any)
            sogoGolfer?.let { sogo ->
                sogo.firstName.let { eventProperties["sogo_first_name"] = it }
                sogo.lastName.let { eventProperties["sogo_last_name"] = it }
                sogo.email?.let { eventProperties["sogo_email"] = it }
                sogo.state?.shortName?.let { eventProperties["sogo_state"] = it }
                sogo.postCode?.let { eventProperties["sogo_postcode"] = it }
                sogo.mobileNo?.let { eventProperties["sogo_mobile"] = it }
                sogo.gender?.let { eventProperties["sogo_gender"] = it }
                sogo.dateOfBirth?.let { eventProperties["sogo_date_of_birth"] = it }
                eventProperties["has_existing_sogo_data"] = true
            } ?: run {
                eventProperties["has_existing_sogo_data"] = false
            }
            
            
            analyticsManager.trackEvent(AnalyticsManager.EVENT_CONFIRM_GOLFER_DATA_DISPLAYED, eventProperties)
            Log.d(TAG, "Tracked confirm_golfer_data_displayed event")
        }

        fun trackConfirmGolferDataSuccess(golferData: Map<String, Any>) {
            val eventProperties = mutableMapOf<String, Any>()
            eventProperties.putAll(golferData)
            
            analyticsManager.trackEvent(AnalyticsManager.EVENT_CONFIRM_GOLFER_DATA_SUCCESS, eventProperties)
            Log.d(TAG, "Tracked confirm_golfer_data_success event")
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

        // Track if SOGO fetch has completed successfully (even if no golfer found)
        private var sogoFetchCompleted = false
        private var sogoFetchError: String? = null

        // NEW: Method to check if we have all required data for home screen
        fun hasRequiredData(): Boolean {
            val golfer = currentGolfer.value
            val hasGolfer = golfer != null
            val hasValidGolfLink = golfer?.golfLinkNo?.isNotBlank() == true  // Only check golfer's own golflink, not localGame
            val isLoading = _uiState.value.isLoading

            // 🔧 ALWAYS validate golflink number first - even if came from round submission
            if (!hasValidGolfLink) {
                Log.d(TAG, "🔧 No valid golflink number - button disabled regardless of other conditions")
                Log.d(TAG, "GolfLink values - golfer.golfLinkNo: '${golfer?.golfLinkNo}', localGame.golflinkNumber: '${localGame.value?.golflinkNumber}' (localGame ignored for button enablement)")
                return false
            }

            // If we came from round submission AND have valid golflink, assume data is available
            if (cameFromRoundSubmission) {
                Log.d(TAG, "Came from round submission with valid golflink - assuming data is available")
                return true
            }

            Log.d(TAG, "Data status - Golfer: $hasGolfer, HasValidGolfLink: $hasValidGolfLink, SogoFetchCompleted: $sogoFetchCompleted, SogoFetchError: $sogoFetchError, isLoading: $isLoading")
            Log.d(TAG, "GolfLink values - golfer.golfLinkNo: '${golfer?.golfLinkNo}', localGame.golflinkNumber: '${localGame.value?.golflinkNumber}' (localGame ignored for button enablement)")
            
            // Enable button only if:
            // 1. We have currentGolfer AND
            // 2. We have a valid golflink number in the golfer profile (NOT from localGame) AND
            // 3. SOGO fetch has completed successfully (regardless of whether golfer exists) AND  
            // 4. We're not currently loading
            // NOTE: SOGO golfer existence is NOT required - only that the fetch completed
            return hasGolfer && hasValidGolfLink && sogoFetchCompleted && !isLoading
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

    private fun setSentryUser(email: String, golfLinkNo: String, firstName: String, lastName: String) {
        try {
            val user = User().apply {
                this.email = email
                this.id = email // Using email as the unique identifier
                this.username = golfLinkNo
                this.data = mapOf(
                    "golf_link_number" to golfLinkNo,
                    "name" to "$firstName $lastName"
                )
            }
            Sentry.setUser(user)
            Log.d(TAG, "✅ Sentry user set: $email (GL#: $golfLinkNo)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set Sentry user", e)
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
                        
                        // Set Sentry user identification with email
                        setSentryUser(currentEmail, golfLinkNo, firstName, lastName)
                        
                        // Track success event
                        trackConfirmGolferDataSuccess(mapOf(
                            "action" to "update",
                            "golflink_number" to golfLinkNo,
                            "first_name" to firstName,
                            "last_name" to lastName,
                            "email" to currentEmail,
                            "state" to state,
                            "postcode" to currentPostcode,
                            "mobile" to currentMobile,
                            "gender" to sogoGender
                        ))
                        
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
                                
                                // Set Sentry user identification with email
                                setSentryUser(currentEmail, golfLinkNo, firstName, lastName)
                                
                                // Track success event
                                trackConfirmGolferDataSuccess(mapOf(
                                    "action" to "create",
                                    "golflink_number" to golfLinkNo,
                                    "first_name" to firstName,
                                    "last_name" to lastName,
                                    "email" to currentEmail,
                                    "state" to state,
                                    "postcode" to currentPostcode,
                                    "mobile" to currentMobile,
                                    "gender" to sogoGender
                                ))
                                
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
