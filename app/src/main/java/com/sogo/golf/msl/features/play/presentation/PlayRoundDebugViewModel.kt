package com.sogo.golf.msl.features.play.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.sogo.golf.msl.app.lifecycle.AppLifecycleManager
import com.sogo.golf.msl.app.lifecycle.AppResumeAction
import com.sogo.golf.msl.data.local.preferencesdata.GameDataTimestampPreferences
import com.sogo.golf.msl.domain.model.NetworkError
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.repository.MslGolferLocalDbRepository
import com.sogo.golf.msl.domain.repository.remote.MslRepository
import com.sogo.golf.msl.domain.usecase.club.GetMslClubAndTenantIdsUseCase
import com.sogo.golf.msl.domain.usecase.competition.FetchAndSaveCompetitionUseCase
import com.sogo.golf.msl.domain.usecase.game.FetchAndSaveGameUseCase
import com.sogo.golf.msl.domain.usecase.game.GetLocalGameUseCase
import com.sogo.golf.msl.domain.usecase.marker.RemoveMarkerUseCase
import com.sogo.golf.msl.domain.usecase.msl_golfer.GetMslGolferUseCase
import com.sogo.golf.msl.domain.repository.remote.AuthRepository
import com.sogo.golf.msl.shared.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayRoundDebugViewModel @Inject constructor(
    private val getLocalGameUseCase: GetLocalGameUseCase,
    private val getMslGolferUseCase: GetMslGolferUseCase,
    private val removeMarkerUseCase: RemoveMarkerUseCase,
    private val fetchAndSaveGameUseCase: FetchAndSaveGameUseCase,
    private val getMslClubAndTenantIdsUseCase: GetMslClubAndTenantIdsUseCase,
    private val fetchAndSaveCompetitionUseCase: FetchAndSaveCompetitionUseCase,
    private val mslRepository: MslRepository,
    private val mslGolferLocalDbRepository: MslGolferLocalDbRepository,
    private val appLifecycleManager: AppLifecycleManager,
    private val gameDataTimestampPreferences: GameDataTimestampPreferences,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _deleteMarkerEnabled = MutableStateFlow(false)
    val deleteMarkerEnabled: StateFlow<Boolean> = _deleteMarkerEnabled.asStateFlow()

    private val _isRemovingMarker = MutableStateFlow(false)
    val isRemovingMarker: StateFlow<Boolean> = _isRemovingMarker.asStateFlow()

    private val _markerError = MutableStateFlow<String?>(null)
    val markerError: StateFlow<String?> = _markerError.asStateFlow()

    // 🔧 NEW: Debug message state
    private val _debugMessage = MutableStateFlow("")
    val debugMessage: StateFlow<String> = _debugMessage.asStateFlow()

    // Get the local game data
    val localGame = getLocalGameUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Get the current golfer data
    val currentGolfer = getMslGolferUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )


    init {
        // 🔄 DEBUG: Log when data is loaded
        viewModelScope.launch {
            currentGolfer.collect { golfer ->
                android.util.Log.d("PlayRoundDebugVM", "=== CURRENT GOLFER UPDATED ===")
                android.util.Log.d("PlayRoundDebugVM", "Golfer: ${golfer?.firstName} ${golfer?.surname} (${golfer?.golfLinkNo})")
            }
        }

        viewModelScope.launch {
            localGame.collect { game ->
                android.util.Log.d("PlayRoundDebugVM", "=== LOCAL GAME UPDATED ===")
                android.util.Log.d("PlayRoundDebugVM", "Game available: ${game != null}")
                android.util.Log.d("PlayRoundDebugVM", "Playing partners count: ${game?.playingPartners?.size ?: 0}")
                game?.playingPartners?.forEach { partner ->
                    android.util.Log.d("PlayRoundDebugVM",
                        "Partner: ${partner.firstName} ${partner.lastName} - Marked by: ${partner.markedByGolfLinkNumber}")
                }
            }
        }
    }


    fun setDeleteMarkerEnabled(enabled: Boolean) {
        _deleteMarkerEnabled.value = enabled
    }

    fun clearMarkerError() {
        _markerError.value = null
    }

    // Find the partner marked by current user
    private fun getPartnerMarkedByMe(): String? {
        val currentUser = currentGolfer.value
        val game = localGame.value

        android.util.Log.d("PlayRoundDebugVM", "=== getPartnerMarkedByMe ===")
        android.util.Log.d("PlayRoundDebugVM", "Current user: ${currentUser?.firstName} ${currentUser?.surname} (${currentUser?.golfLinkNo})")
        android.util.Log.d("PlayRoundDebugVM", "Game available: ${game != null}")
        android.util.Log.d("PlayRoundDebugVM", "Playing partners count: ${game?.playingPartners?.size ?: 0}")

        return if (currentUser != null && game != null) {
            game.playingPartners.forEach { partner ->
                android.util.Log.d("PlayRoundDebugVM", "Partner: ${partner.firstName} ${partner.lastName} (${partner.golfLinkNumber}) - Marked by: ${partner.markedByGolfLinkNumber}")
            }

            val markedPartner = game.playingPartners.find { partner ->
                partner.markedByGolfLinkNumber == currentUser.golfLinkNo
            }

            android.util.Log.d("PlayRoundDebugVM", "Found marked partner: ${markedPartner?.firstName} ${markedPartner?.lastName} (${markedPartner?.golfLinkNumber})")
            markedPartner?.golfLinkNumber
        } else {
            android.util.Log.d("PlayRoundDebugVM", "No current user or game data available")
            null
        }
    }

    // In PlayRoundDebugViewModel.kt - Fix the removeMarkerAndNavigateBack method

    fun removeMarkerAndNavigateBack(navController: NavController) {
        android.util.Log.d("PlayRoundDebugVM", "=== removeMarkerAndNavigateBack called ===")

        val partnerGolfLinkNumber = getPartnerMarkedByMe()
        android.util.Log.d("PlayRoundDebugVM", "Partner golf link number: $partnerGolfLinkNumber")

        if (partnerGolfLinkNumber == null) {
            android.util.Log.d("PlayRoundDebugVM", "No marker to remove, navigating back normally")
            navController.popBackStack()
            return
        }

        viewModelScope.launch {
            android.util.Log.d("PlayRoundDebugVM", "Starting marker removal process")
            _isRemovingMarker.value = true
            _markerError.value = null

            android.util.Log.d("PlayRoundDebugVM", "=== REMOVING MARKER ON BACK NAVIGATION ===")
            android.util.Log.d("PlayRoundDebugVM", "Golf Link Number: $partnerGolfLinkNumber")

            when (val result = removeMarkerUseCase(partnerGolfLinkNumber)) {
                is NetworkResult.Success -> {
                    android.util.Log.d("PlayRoundDebugVM", "✅ SUCCESS: Marker removed successfully")

                    // 🔄 CRITICAL: Refresh data BEFORE navigating back
                    android.util.Log.d("PlayRoundDebugVM", "🔄 Refreshing game and competition data...")

                    try {
                        val selectedClub = getMslClubAndTenantIdsUseCase()
                        if (selectedClub?.clubId != null) {
                            val clubIdStr = selectedClub.clubId.toString()

                            // Step 1: Refresh game data to get updated marker assignments
                            android.util.Log.d("PlayRoundDebugVM", "Step 1: Refreshing game data...")
                            when (val gameResult = fetchAndSaveGameUseCase(clubIdStr)) {
                                is NetworkResult.Success -> {
                                    android.util.Log.d("PlayRoundDebugVM", "✅ Game data refreshed successfully")
                                    android.util.Log.d("PlayRoundDebugVM", "Updated playing partners: ${gameResult.data.playingPartners.size}")

                                    // Log the updated marker assignments
                                    gameResult.data.playingPartners.forEach { partner ->
                                        android.util.Log.d("PlayRoundDebugVM",
                                            "Partner: ${partner.firstName} ${partner.lastName} - Marked by: ${partner.markedByGolfLinkNumber}")
                                    }
                                }
                                is NetworkResult.Error -> {
                                    android.util.Log.w("PlayRoundDebugVM", "⚠️ Failed to refresh game data: ${gameResult.error}")
                                    
                                    if (gameResult.error is NetworkError.HttpError && gameResult.error.code == 401 && gameResult.error.isRefreshFailure) {
                                        handleAuthenticationFailure()
                                    }
                                    // Don't fail the whole operation - marker was still removed successfully
                                }
                                is NetworkResult.Loading -> { /* Ignore */ }
                            }

                            // Step 2: Refresh competition data
                            android.util.Log.d("PlayRoundDebugVM", "Step 2: Refreshing competition data...")
                            when (val competitionResult = fetchAndSaveCompetitionUseCase(clubIdStr)) {
                                is NetworkResult.Success -> {
                                    android.util.Log.d("PlayRoundDebugVM", "✅ Competition data refreshed successfully")
                                }
                                is NetworkResult.Error -> {
                                    android.util.Log.w("PlayRoundDebugVM", "⚠️ Failed to refresh competition data: ${competitionResult.error}")
                                    
                                    if (competitionResult.error is NetworkError.HttpError && competitionResult.error.code == 401 && competitionResult.error.isRefreshFailure) {
                                        handleAuthenticationFailure()
                                    }
                                    // Don't fail the whole operation
                                }
                                is NetworkResult.Loading -> { /* Ignore */ }
                            }

                            android.util.Log.d("PlayRoundDebugVM", "✅ All data refresh operations completed")

                        } else {
                            android.util.Log.w("PlayRoundDebugVM", "⚠️ No club selected, cannot refresh data")
                        }
                    } catch (e: Exception) {
                        android.util.Log.w("PlayRoundDebugVM", "⚠️ Exception while refreshing data", e)
                    }

                    _isRemovingMarker.value = false

                    android.util.Log.d("PlayRoundDebugVM", "🔄 Data refresh complete - now navigating back to choose partner screen")
                    // ✅ NOW navigate back - the fresh data is in Room DB
                    navController.popBackStack()
                }
                is NetworkResult.Error -> {
                    android.util.Log.e("PlayRoundDebugVM", "❌ ERROR: Failed to remove marker: ${result.error}")
                    _isRemovingMarker.value = false
                    _markerError.value = "Failed to remove marker: ${result.error.toUserMessage()}"
                }
                is NetworkResult.Loading -> {
                    android.util.Log.d("PlayRoundDebugVM", "Loading state received")
                }
            }
        }
    }

    // Refresh game data after marker operations
    private suspend fun refreshGameData() {
        try {
            val selectedClub = getMslClubAndTenantIdsUseCase()
            if (selectedClub?.clubId != null) {
                android.util.Log.d("PlayRoundDebugVM", "Refreshing game data after marker removal...")

                when (val result = fetchAndSaveGameUseCase(selectedClub.clubId.toString())) {
                    is NetworkResult.Success -> {
                        android.util.Log.d("PlayRoundDebugVM", "✅ Game data refreshed successfully")
                    }
                    is NetworkResult.Error -> {
                        android.util.Log.w("PlayRoundDebugVM", "⚠️ Failed to refresh game data: ${result.error}")
                        
                        if (result.error is NetworkError.HttpError && result.error.code == 401 && result.error.isRefreshFailure) {
                            handleAuthenticationFailure()
                        }
                        // Don't show error to user - marker removal was successful
                    }
                    is NetworkResult.Loading -> {
                        // Ignore
                    }
                }
            } else {
                android.util.Log.w("PlayRoundDebugVM", "⚠️ No club selected, cannot refresh game data")
            }
        } catch (e: Exception) {
            android.util.Log.w("PlayRoundDebugVM", "⚠️ Exception while refreshing game data", e)
        }
    }

    // 🔧 NEW: Debug methods for testing daily reset
    fun simulateYesterdayData() {
        viewModelScope.launch {
            try {
                val yesterday = "2025-07-27" // Yesterday's date
                gameDataTimestampPreferences.saveGameDataDate(yesterday)
                DateUtils.clearDebugDate() // Keep today as real today

                _debugMessage.value = "🔧 Set stored date to YESTERDAY ($yesterday). Real today is ${DateUtils.getTodayDateString()}"

                android.util.Log.d("PlayRoundDebugVM", "🔧 DEBUG: Simulated yesterday's data - stored: $yesterday")
            } catch (e: Exception) {
                _debugMessage.value = "Error setting yesterday's data: ${e.message}"
                android.util.Log.e("PlayRoundDebugVM", "Error simulating yesterday's data", e)
            }
        }
    }

    fun simulateTodayData() {
        viewModelScope.launch {
            try {
                val today = DateUtils.getTodayDateString()
                gameDataTimestampPreferences.saveGameDataDate(today)
                DateUtils.clearDebugDate()

                _debugMessage.value = "🔧 Set stored date to TODAY ($today). Should be fresh!"

                android.util.Log.d("PlayRoundDebugVM", "🔧 DEBUG: Simulated today's data - stored: $today")
            } catch (e: Exception) {
                _debugMessage.value = "Error setting today's data: ${e.message}"
                android.util.Log.e("PlayRoundDebugVM", "Error simulating today's data", e)
            }
        }
    }

    fun triggerAppResume(navController: NavController) {
        viewModelScope.launch {
            try {
                _debugMessage.value = "🔄 Triggering app resume check..."

                android.util.Log.d("PlayRoundDebugVM", "🔧 DEBUG: Manually triggering app resume check")

                when (val action = appLifecycleManager.onAppResumed()) {
                    AppResumeAction.Continue -> {
                        _debugMessage.value = "✅ Data is FRESH - staying on Play Round screen"
                        android.util.Log.d("PlayRoundDebugVM", "✅ Data is fresh, continuing normally")
                    }

                    AppResumeAction.NavigateToHome -> {
                        _debugMessage.value = "📅 Data is STALE - navigating to Home screen..."
                        android.util.Log.d("PlayRoundDebugVM", "📅 Data is stale, navigating to home")

                        // Navigate to home and clear back stack (same as MainActivity does)
                        navController.navigate("homescreen") {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

            } catch (e: Exception) {
                _debugMessage.value = "❌ Error during app resume check: ${e.message}"
                android.util.Log.e("PlayRoundDebugVM", "Error during app resume check", e)
            }
        }
    }

    fun clearDebugMessage() {
        _debugMessage.value = ""
    }

    private fun handleAuthenticationFailure() {
        viewModelScope.launch {
            try {
                authRepository.logout()
                android.util.Log.d("PlayRoundDebugViewModel", "🔓 User logged out due to authentication failure")
            } catch (e: Exception) {
                android.util.Log.e("PlayRoundDebugViewModel", "❌ Failed to logout after auth failure", e)
            }
        }
    }
}
