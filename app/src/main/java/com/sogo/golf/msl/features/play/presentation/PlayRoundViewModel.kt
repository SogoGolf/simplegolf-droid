package com.sogo.golf.msl.features.play.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.sogo.golf.msl.app.lifecycle.AppLifecycleManager
import com.sogo.golf.msl.app.lifecycle.AppResumeAction
import com.sogo.golf.msl.data.local.preferencesdata.GameDataTimestampPreferences
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.repository.MslGolferLocalDbRepository
import com.sogo.golf.msl.domain.repository.remote.MslRepository
import com.sogo.golf.msl.domain.usecase.club.GetMslClubAndTenantIdsUseCase
import com.sogo.golf.msl.domain.usecase.competition.FetchAndSaveCompetitionUseCase
import com.sogo.golf.msl.domain.usecase.game.FetchAndSaveGameUseCase
import com.sogo.golf.msl.domain.usecase.game.GetLocalGameUseCase
import com.sogo.golf.msl.domain.usecase.competition.GetLocalCompetitionUseCase
import com.sogo.golf.msl.domain.usecase.marker.RemoveMarkerUseCase
import com.sogo.golf.msl.domain.usecase.msl_golfer.GetMslGolferUseCase
import com.sogo.golf.msl.domain.usecase.round.GetActiveTodayRoundUseCase
import com.sogo.golf.msl.domain.usecase.round.DeleteLocalAndRemoteRoundUseCase
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
class PlayRoundViewModel @Inject constructor(
    private val getLocalGameUseCase: GetLocalGameUseCase,
    private val getLocalCompetitionUseCase: GetLocalCompetitionUseCase,
    private val getMslGolferUseCase: GetMslGolferUseCase,
    private val removeMarkerUseCase: RemoveMarkerUseCase,
    private val fetchAndSaveGameUseCase: FetchAndSaveGameUseCase,
    private val getMslClubAndTenantIdsUseCase: GetMslClubAndTenantIdsUseCase,
    private val fetchAndSaveCompetitionUseCase: FetchAndSaveCompetitionUseCase,
    private val getActiveTodayRoundUseCase: GetActiveTodayRoundUseCase,
    private val deleteLocalAndRemoteRoundUseCase: DeleteLocalAndRemoteRoundUseCase,
    private val mslRepository: MslRepository,
    private val mslGolferLocalDbRepository: MslGolferLocalDbRepository,
    private val appLifecycleManager: AppLifecycleManager,
    private val gameDataTimestampPreferences: GameDataTimestampPreferences,
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

    // Back button visibility based on stroke counts
    private val _showBackButton = MutableStateFlow(true)
    val showBackButton: StateFlow<Boolean> = _showBackButton.asStateFlow()

    // Get the local game data
    val localGame = getLocalGameUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Get the local competition data
    val localCompetition = getLocalCompetitionUseCase()
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

    // Get the current active round data
    private val _currentRound = MutableStateFlow<com.sogo.golf.msl.domain.model.Round?>(null)
    val currentRound: StateFlow<com.sogo.golf.msl.domain.model.Round?> = _currentRound.asStateFlow()


    init {
        // 🔄 DEBUG: Log when data is loaded
        viewModelScope.launch {
            currentGolfer.collect { golfer ->
                android.util.Log.d("PlayRoundVM", "=== CURRENT GOLFER UPDATED ===")
                android.util.Log.d("PlayRoundVM", "Golfer: ${golfer?.firstName} ${golfer?.surname} (${golfer?.golfLinkNo})")
            }
        }

        viewModelScope.launch {
            localGame.collect { game ->
                android.util.Log.d("PlayRoundVM", "=== LOCAL GAME UPDATED ===")
                android.util.Log.d("PlayRoundVM", "Game available: ${game != null}")
                android.util.Log.d("PlayRoundVM", "Playing partners count: ${game?.playingPartners?.size ?: 0}")
                game?.playingPartners?.forEach { partner ->
                    android.util.Log.d("PlayRoundVM",
                        "Partner: ${partner.firstName} ${partner.lastName} - Marked by: ${partner.markedByGolfLinkNumber}")
                }
            }
        }

        // Load current round data and monitor changes
        viewModelScope.launch {
            loadCurrentRound()
        }
        
        // Monitor round data and update back button visibility
        viewModelScope.launch {
            currentRound.collect { round ->
                updateBackButtonVisibility(round)
            }
        }
    }


    fun setDeleteMarkerEnabled(enabled: Boolean) {
        _deleteMarkerEnabled.value = enabled
    }

    fun clearMarkerError() {
        _markerError.value = null
    }

    private suspend fun loadCurrentRound() {
        try {
            val round = getActiveTodayRoundUseCase()
            _currentRound.value = round
            android.util.Log.d("PlayRoundVM", "Loaded current round: ${round?.id}")
        } catch (e: Exception) {
            android.util.Log.e("PlayRoundVM", "Error loading current round", e)
            _currentRound.value = null
        }
    }

    private fun updateBackButtonVisibility(round: com.sogo.golf.msl.domain.model.Round?) {
        val canNavigateBack = canNavigateBackBasedOnStrokes(round)
        
        android.util.Log.d("PlayRoundVM", "=== UPDATE BACK BUTTON VISIBILITY ===")
        android.util.Log.d("PlayRoundVM", "Round available: ${round != null}")
        android.util.Log.d("PlayRoundVM", "Can navigate back: $canNavigateBack")
        
        _showBackButton.value = canNavigateBack
    }

    private fun canNavigateBackBasedOnStrokes(round: com.sogo.golf.msl.domain.model.Round?): Boolean {
        if (round == null) {
            android.util.Log.d("PlayRoundVM", "No round data - allowing back navigation")
            return true
        }

        // Check main golfer's first hole strokes
        val mainGolferFirstHoleStrokes = round.holeScores.firstOrNull()?.strokes ?: 0
        android.util.Log.d("PlayRoundVM", "Main golfer first hole strokes: $mainGolferFirstHoleStrokes")

        // Check playing partner's first hole strokes
        val partnerFirstHoleStrokes = round.playingPartnerRound?.holeScores?.firstOrNull()?.strokes ?: 0
        android.util.Log.d("PlayRoundVM", "Partner first hole strokes: $partnerFirstHoleStrokes")

        // Allow back navigation only if BOTH golfers have 0 strokes on first hole
        val canNavigateBack = mainGolferFirstHoleStrokes == 0 && partnerFirstHoleStrokes == 0
        
        android.util.Log.d("PlayRoundVM", "Back navigation allowed: $canNavigateBack (main: $mainGolferFirstHoleStrokes, partner: $partnerFirstHoleStrokes)")
        
        return canNavigateBack
    }

    // Find the partner marked by current user
    private fun getPartnerMarkedByMe(): String? {
        val currentUser = currentGolfer.value
        val game = localGame.value

        android.util.Log.d("PlayRoundVM", "=== getPartnerMarkedByMe ===")
        android.util.Log.d("PlayRoundVM", "Current user: ${currentUser?.firstName} ${currentUser?.surname} (${currentUser?.golfLinkNo})")
        android.util.Log.d("PlayRoundVM", "Game available: ${game != null}")
        android.util.Log.d("PlayRoundVM", "Playing partners count: ${game?.playingPartners?.size ?: 0}")

        return if (currentUser != null && game != null) {
            game.playingPartners.forEach { partner ->
                android.util.Log.d("PlayRoundVM", "Partner: ${partner.firstName} ${partner.lastName} (${partner.golfLinkNumber}) - Marked by: ${partner.markedByGolfLinkNumber}")
            }

            val markedPartner = game.playingPartners.find { partner ->
                partner.markedByGolfLinkNumber == currentUser.golfLinkNo
            }

            android.util.Log.d("PlayRoundVM", "Found marked partner: ${markedPartner?.firstName} ${markedPartner?.lastName} (${markedPartner?.golfLinkNumber})")
            markedPartner?.golfLinkNumber
        } else {
            android.util.Log.d("PlayRoundVM", "No current user or game data available")
            null
        }
    }

    // In PlayRoundViewModel.kt - Fix the removeMarkerAndNavigateBack method

    fun removeMarkerAndNavigateBack(navController: NavController) {
        android.util.Log.d("PlayRoundVM", "=== removeMarkerAndNavigateBack called ===")

        // Check if back navigation is allowed based on strokes
        val round = currentRound.value
        if (!canNavigateBackBasedOnStrokes(round)) {
            android.util.Log.d("PlayRoundVM", "❌ Back navigation blocked - strokes exist on first hole")
            _markerError.value = "Cannot go back - round has already started"
            return
        }

        val partnerGolfLinkNumber = getPartnerMarkedByMe()
        android.util.Log.d("PlayRoundVM", "Partner golf link number: $partnerGolfLinkNumber")

        if (partnerGolfLinkNumber == null) {
            android.util.Log.d("PlayRoundVM", "No marker to remove, navigating back normally")
            navController.popBackStack()
            return
        }

        viewModelScope.launch {
            android.util.Log.d("PlayRoundVM", "Starting marker removal and round deletion process")
            _isRemovingMarker.value = true
            _markerError.value = null

            android.util.Log.d("PlayRoundVM", "Step 1: Removing marker...")
            when (val markerResult = removeMarkerUseCase(partnerGolfLinkNumber)) {
                is NetworkResult.Success -> {
                    android.util.Log.d("PlayRoundVM", "✅ Marker removed successfully")

                    android.util.Log.d("PlayRoundVM", "Step 2: Getting current round for deletion...")
                    val currentRound = getActiveTodayRoundUseCase()
                    
                    if (currentRound != null) {
                        android.util.Log.d("PlayRoundVM", "Step 3: Deleting round: ${currentRound.id}")
                        
                        when (val deleteResult = deleteLocalAndRemoteRoundUseCase(currentRound.id)) {
                            is NetworkResult.Success<*> -> {
                                android.util.Log.d("PlayRoundVM", "✅ Round deleted successfully")
                                
                                android.util.Log.d("PlayRoundVM", "Step 4: Refreshing game and competition data...")
                                refreshDataAfterMarkerRemoval()
                                
                                _isRemovingMarker.value = false
                                android.util.Log.d("PlayRoundVM", "✅ Complete flow successful - navigating back")

                                navController.popBackStack()
                            }
                            is NetworkResult.Error<*> -> {
                                android.util.Log.e("PlayRoundVM", "❌ Failed to delete round: ${deleteResult.error}")
                                _isRemovingMarker.value = false
                                _markerError.value = "Failed to delete round: ${deleteResult.error.toUserMessage()}"
                            }
                            is NetworkResult.Loading<*> -> {
                                android.util.Log.d("PlayRoundVM", "Round deletion in progress...")
                            }
                        }
                    } else {
                        android.util.Log.w("PlayRoundVM", "⚠️ No active round found for deletion")
                        refreshDataAfterMarkerRemoval()
                        _isRemovingMarker.value = false
                        navController.popBackStack()
                    }
                }
                is NetworkResult.Error -> {
                    android.util.Log.e("PlayRoundVM", "❌ Failed to remove marker: ${markerResult.error}")
                    _isRemovingMarker.value = false
                    _markerError.value = "Failed to remove marker: ${markerResult.error.toUserMessage()}"
                }
                is NetworkResult.Loading -> {
                    android.util.Log.d("PlayRoundVM", "Marker removal in progress...")
                }
            }
        }
    }

    private suspend fun refreshDataAfterMarkerRemoval() {
        try {
            val selectedClub = getMslClubAndTenantIdsUseCase()
            if (selectedClub?.clubId != null) {
                val clubIdStr = selectedClub.clubId.toString()

                when (val gameResult = fetchAndSaveGameUseCase(clubIdStr)) {
                    is NetworkResult.Success -> {
                        android.util.Log.d("PlayRoundVM", "✅ Game data refreshed successfully")
                    }
                    is NetworkResult.Error -> {
                        android.util.Log.w("PlayRoundVM", "⚠️ Failed to refresh game data: ${gameResult.error}")
                    }
                    is NetworkResult.Loading -> { }
                }

                when (val competitionResult = fetchAndSaveCompetitionUseCase(clubIdStr)) {
                    is NetworkResult.Success -> {
                        android.util.Log.d("PlayRoundVM", "✅ Competition data refreshed successfully")
                    }
                    is NetworkResult.Error -> {
                        android.util.Log.w("PlayRoundVM", "⚠️ Failed to refresh competition data: ${competitionResult.error}")
                    }
                    is NetworkResult.Loading -> { }
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("PlayRoundVM", "⚠️ Exception while refreshing data", e)
        }
    }

    // Refresh game data after marker operations
    private suspend fun refreshGameData() {
        try {
            val selectedClub = getMslClubAndTenantIdsUseCase()
            if (selectedClub?.clubId != null) {
                android.util.Log.d("PlayRoundVM", "Refreshing game data after marker removal...")

                when (val result = fetchAndSaveGameUseCase(selectedClub.clubId.toString())) {
                    is NetworkResult.Success -> {
                        android.util.Log.d("PlayRoundVM", "✅ Game data refreshed successfully")
                    }
                    is NetworkResult.Error -> {
                        android.util.Log.w("PlayRoundVM", "⚠️ Failed to refresh game data: ${result.error}")
                        // Don't show error to user - marker removal was successful
                    }
                    is NetworkResult.Loading -> {
                        // Ignore
                    }
                }
            } else {
                android.util.Log.w("PlayRoundVM", "⚠️ No club selected, cannot refresh game data")
            }
        } catch (e: Exception) {
            android.util.Log.w("PlayRoundVM", "⚠️ Exception while refreshing game data", e)
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

                android.util.Log.d("PlayRoundVM", "🔧 DEBUG: Simulated yesterday's data - stored: $yesterday")
            } catch (e: Exception) {
                _debugMessage.value = "Error setting yesterday's data: ${e.message}"
                android.util.Log.e("PlayRoundVM", "Error simulating yesterday's data", e)
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

                android.util.Log.d("PlayRoundVM", "🔧 DEBUG: Simulated today's data - stored: $today")
            } catch (e: Exception) {
                _debugMessage.value = "Error setting today's data: ${e.message}"
                android.util.Log.e("PlayRoundVM", "Error simulating today's data", e)
            }
        }
    }

    fun triggerAppResume(navController: NavController) {
        viewModelScope.launch {
            try {
                _debugMessage.value = "🔄 Triggering app resume check..."

                android.util.Log.d("PlayRoundVM", "🔧 DEBUG: Manually triggering app resume check")

                when (val action = appLifecycleManager.onAppResumed()) {
                    AppResumeAction.Continue -> {
                        _debugMessage.value = "✅ Data is FRESH - staying on Play Round screen"
                        android.util.Log.d("PlayRoundVM", "✅ Data is fresh, continuing normally")
                    }

                    AppResumeAction.NavigateToHome -> {
                        _debugMessage.value = "📅 Data is STALE - navigating to Home screen..."
                        android.util.Log.d("PlayRoundVM", "📅 Data is stale, navigating to home")

                        // Navigate to home and clear back stack (same as MainActivity does)
                        navController.navigate("homescreen") {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

            } catch (e: Exception) {
                _debugMessage.value = "❌ Error during app resume check: ${e.message}"
                android.util.Log.e("PlayRoundVM", "Error during app resume check", e)
            }
        }
    }

    fun clearDebugMessage() {
        _debugMessage.value = ""
    }

    fun refreshBackButtonState() {
        viewModelScope.launch {
            loadCurrentRound()
        }
    }
}
