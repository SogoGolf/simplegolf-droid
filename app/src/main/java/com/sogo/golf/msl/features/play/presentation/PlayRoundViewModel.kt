package com.sogo.golf.msl.features.play.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.sogo.golf.msl.app.lifecycle.AppLifecycleManager
import com.sogo.golf.msl.app.lifecycle.AppResumeAction
import com.sogo.golf.msl.data.local.preferencesdata.GameDataTimestampPreferences
import com.sogo.golf.msl.data.local.preferences.HoleStatePreferences
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.repository.MslGolferLocalDbRepository
import com.sogo.golf.msl.domain.repository.remote.MslRepository
import com.sogo.golf.msl.domain.usecase.club.GetMslClubAndTenantIdsUseCase
import com.sogo.golf.msl.domain.usecase.competition.FetchAndSaveCompetitionUseCase
import com.sogo.golf.msl.domain.usecase.date.ResetStaleDataUseCase
import com.sogo.golf.msl.domain.usecase.round.UpdatePickupUseCase
import com.sogo.golf.msl.domain.usecase.game.FetchAndSaveGameUseCase
import com.sogo.golf.msl.domain.usecase.game.GetLocalGameUseCase
import com.sogo.golf.msl.domain.usecase.competition.GetLocalCompetitionUseCase
import com.sogo.golf.msl.domain.model.msl.MslGame
import com.sogo.golf.msl.domain.usecase.marker.RemoveMarkerUseCase
import com.sogo.golf.msl.domain.usecase.msl_golfer.GetMslGolferUseCase
import com.sogo.golf.msl.domain.usecase.round.GetActiveTodayRoundUseCase
import com.sogo.golf.msl.domain.usecase.round.DeleteLocalAndRemoteRoundUseCase
import com.sogo.golf.msl.domain.usecase.round.UpdateHoleScoreUseCase
import com.sogo.golf.msl.domain.usecase.round.BulkSyncRoundUseCase
import com.sogo.golf.msl.domain.usecase.scoring.CalcStablefordUseCase
import com.sogo.golf.msl.domain.usecase.scoring.CalcParUseCase
import com.sogo.golf.msl.domain.usecase.scoring.CalcStrokeUseCase
import com.sogo.golf.msl.domain.usecase.scoring.CalcHoleNetParUseCase
import com.sogo.golf.msl.domain.model.HoleScoreForCalcs
import com.sogo.golf.msl.data.network.NetworkStateMonitor
import android.util.Log
import com.sogo.golf.msl.data.network.NetworkState
import com.sogo.golf.msl.shared.utils.DateUtils
import com.sogo.golf.msl.analytics.AnalyticsManager
import com.sogo.golf.msl.domain.model.msl.MslCompetition
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
    private val updateHoleScoreUseCase: UpdateHoleScoreUseCase,
    private val bulkSyncRoundUseCase: BulkSyncRoundUseCase,
    private val calcStablefordUseCase: CalcStablefordUseCase,
    private val calcParUseCase: CalcParUseCase,
    private val calcStrokeUseCase: CalcStrokeUseCase,
    private val calcHoleNetParUseCase: CalcHoleNetParUseCase,
    private val networkStateMonitor: NetworkStateMonitor,
    private val holeStatePreferences: HoleStatePreferences,
    private val mslRepository: MslRepository,
    private val mslGolferLocalDbRepository: MslGolferLocalDbRepository,
    private val appLifecycleManager: AppLifecycleManager,
    private val gameDataTimestampPreferences: GameDataTimestampPreferences,
    private val resetStaleDataUseCase: ResetStaleDataUseCase,
    private val updatePickupUseCase: UpdatePickupUseCase,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _deleteMarkerEnabled = MutableStateFlow(false)
    val deleteMarkerEnabled: StateFlow<Boolean> = _deleteMarkerEnabled.asStateFlow()

    private val _isRemovingMarker = MutableStateFlow(false)
    val isRemovingMarker: StateFlow<Boolean> = _isRemovingMarker.asStateFlow()

    // Flags to prevent race conditions during active play
    private var isAppStartup = true
    private var isLoadingRound = false
    private var lastBulkSyncTime = 0L
    private val BULK_SYNC_DEBOUNCE_MS = 2000L // 2 seconds

    private val _markerError = MutableStateFlow<String?>(null)
    val markerError: StateFlow<String?> = _markerError.asStateFlow()

    private val _isAbandoningRound = MutableStateFlow(false)
    val isAbandoningRound: StateFlow<Boolean> = _isAbandoningRound.asStateFlow()

    private val _abandonError = MutableStateFlow<String?>(null)
    val abandonError: StateFlow<String?> = _abandonError.asStateFlow()

    // Store partner golflink number for analytics tracking
    private var removedPartnerGolfLinkNumber: String? = null

    // 🔧 NEW: Debug message state
    private val _debugMessage = MutableStateFlow("")
    val debugMessage: StateFlow<String> = _debugMessage.asStateFlow()

    // Back button visibility based on stroke counts
    private val _showBackButton = MutableStateFlow(true)
    val showBackButton: StateFlow<Boolean> = _showBackButton.asStateFlow()

    // GoToHole dialog state
    private val _showGoToHoleDialog = MutableStateFlow(false)
    val showGoToHoleDialog: StateFlow<Boolean> = _showGoToHoleDialog.asStateFlow()

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

    // Current hole number for navigation
    private val _currentHoleNumber = MutableStateFlow(1)
    val currentHoleNumber: StateFlow<Int> = _currentHoleNumber.asStateFlow()


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
                
                if (round != null) {
                    restoreCurrentHoleOnAppStart(round)
                }
            }
        }
        
        // Initialize hole number based on game data
        viewModelScope.launch {
            localGame.collect { game ->
                if (game != null) {
                    val currentHole = _currentHoleNumber.value
                    val startingHole = game.startingHoleNumber
                    
                    // Update hole number if:
                    // 1. We're still on the default hole 1 and the game starts on a different hole
                    // 2. Or we need to ensure consistency with the game's starting hole
                    if (currentHole == 1 && startingHole != 1) {
                        _currentHoleNumber.value = startingHole
                        android.util.Log.d("PlayRoundVM", "Set starting hole number to: $startingHole (was: $currentHole)")
                    }
                }
            }
        }
        
        // Monitor network state changes for bulk sync
        viewModelScope.launch {
            networkStateMonitor.networkStateFlow().collect { networkState ->
                when (networkState) {
                    is NetworkState.Available -> {
                        android.util.Log.d("PlayRoundVM", "🌐 Network restored - checking for bulk sync")
                        triggerBulkSyncIfNeeded()
                    }
                    is NetworkState.Lost -> {
                        android.util.Log.d("PlayRoundVM", "📵 Network lost")
                    }
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

    private suspend fun loadCurrentRound() {
        // Prevent concurrent loadCurrentRound calls
        if (isLoadingRound) {
            android.util.Log.d("PlayRoundVM", "🔄 Skipping loadCurrentRound - already loading")
            return
        }
        
        isLoadingRound = true
        try {
            val round = getActiveTodayRoundUseCase()
            _currentRound.value = round
            android.util.Log.d("PlayRoundVM", "✅ Current round loaded: ${round?.id}")
            
            // Update back button visibility based on current round
            updateBackButtonVisibility(round)
            
            if (round != null) {
                restoreCurrentHoleOnAppStart(round)
            }
        } catch (e: Exception) {
            android.util.Log.e("PlayRoundVM", "❌ Error loading current round", e)
            _currentRound.value = null
        } finally {
            isLoadingRound = false
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

        // Get the starting hole number and current hole number from game data
        val game = localGame.value
        val startingHoleNumber = game?.startingHoleNumber ?: 1
        val currentHole = _currentHoleNumber.value
        
        android.util.Log.d("PlayRoundVM", "Current hole: $currentHole, Starting hole: $startingHoleNumber")
        
        // If we're not on the starting hole, allow back navigation
        if (currentHole != startingHoleNumber) {
            android.util.Log.d("PlayRoundVM", "Not on starting hole - allowing back navigation")
            return true
        }
        
        // If we're on the starting hole, check strokes as before
        android.util.Log.d("PlayRoundVM", "On starting hole - checking strokes")
        
        // Check main golfer's starting hole strokes
        val mainGolferStartingHoleStrokes = round.holeScores.find { it.holeNumber == startingHoleNumber }?.strokes ?: 0
        android.util.Log.d("PlayRoundVM", "Main golfer starting hole ($startingHoleNumber) strokes: $mainGolferStartingHoleStrokes")

        // Check playing partner's starting hole strokes
        val partnerStartingHoleStrokes = round.playingPartnerRound?.holeScores?.find { it.holeNumber == startingHoleNumber }?.strokes ?: 0
        android.util.Log.d("PlayRoundVM", "Partner starting hole ($startingHoleNumber) strokes: $partnerStartingHoleStrokes")

        // Prevent back navigation only if BOTH golfers have strokes > 0 on starting hole
        val canNavigateBack = !(mainGolferStartingHoleStrokes > 0 && partnerStartingHoleStrokes > 0)
        
        android.util.Log.d("PlayRoundVM", "Back navigation allowed: $canNavigateBack (main: $mainGolferStartingHoleStrokes, partner: $partnerStartingHoleStrokes)")
        
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

            // Store the partner golflink number for tracking after successful removal
            removedPartnerGolfLinkNumber = partnerGolfLinkNumber

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
                                
                                // Track the round reset marker event after successful removal
                                trackRoundResetMarker()

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

    // Hole navigation functions
    fun navigateToNextHole(navController: NavController? = null) {
        val game = localGame.value
        if (game != null) {
            val currentHole = _currentHoleNumber.value
            val startingHole = game.startingHoleNumber
            val numberOfHoles = game.numberOfHoles ?: 18
            
            // Get the cycle of holes for this round
            val cycle = getCycleIndices(startingHole, numberOfHoles)
            val currentIndex = cycle.indexOf(currentHole - 1)
            
            if (currentIndex >= 0 && currentIndex < cycle.size - 1) {
                // Move to next hole in cycle
                val nextIndex = currentIndex + 1
                val newHole = cycle[nextIndex] + 1
                _currentHoleNumber.value = newHole
                android.util.Log.d("PlayRoundVM", "Navigated to hole: $newHole (cycle index: $nextIndex)")
                saveCurrentHoleState(newHole)
                android.util.Log.d("PlayRoundVM", "🔄 Saved hole state: $newHole before triggering navigation update")
                triggerHoleNavigationUpdate()
                updateBackButtonVisibility(currentRound.value)
            } else {
                android.util.Log.d("PlayRoundVM", "On last hole of cycle - checking completion status")
                if (areAllHolesCompleted()) {
                    android.util.Log.d("PlayRoundVM", "All holes completed - navigating to review screen")
                    val currentRoundId = currentRound.value?.id ?: ""
                    navController?.navigate("reviewscreen/$currentRoundId")
                } else {
                    android.util.Log.d("PlayRoundVM", "Not all holes completed - showing go to hole dialog")
                    showGoToHoleDialog()
                }
            }
        }
    }

    fun navigateToPreviousHole() {
        if (!showBackButton.value) {
            android.util.Log.d("PlayRoundVM", "Back navigation blocked - strokes exist on starting hole")
            return
        }
        
        val game = localGame.value
        if (game != null) {
            val currentHole = _currentHoleNumber.value
            val startingHole = game.startingHoleNumber
            val numberOfHoles = game.numberOfHoles ?: 18
            
            // Get the cycle of holes for this round
            val cycle = getCycleIndices(startingHole, numberOfHoles)
            val currentIndex = cycle.indexOf(currentHole - 1)
            
            if (currentIndex > 0) {
                // Move to previous hole in cycle
                val prevIndex = currentIndex - 1
                val newHole = cycle[prevIndex] + 1
                _currentHoleNumber.value = newHole
                android.util.Log.d("PlayRoundVM", "Navigated to hole: $newHole (cycle index: $prevIndex)")
                saveCurrentHoleState(newHole)
                android.util.Log.d("PlayRoundVM", "🔄 Saved hole state: $newHole before triggering navigation update")
                triggerHoleNavigationUpdate()
                updateBackButtonVisibility(currentRound.value)
            } else {
                android.util.Log.d("PlayRoundVM", "Already at first hole in cycle: $startingHole")
            }
        }
    }

    fun navigateToHole(holeNumber: Int) {
        val game = localGame.value
        if (game != null) {
            val startingHole = game.startingHoleNumber
            val numberOfHoles = game.numberOfHoles ?: 18
            
            // Get the cycle of holes for this round to validate if hole is in cycle
            val cycle = getCycleIndices(startingHole, numberOfHoles)
            val isValidHole = cycle.contains(holeNumber - 1)
            
            if (isValidHole) {
                _currentHoleNumber.value = holeNumber
                android.util.Log.d("PlayRoundVM", "Navigated to hole: $holeNumber (cycle-aware navigation)")
                saveCurrentHoleState(holeNumber)
                android.util.Log.d("PlayRoundVM", "🔄 Saved hole state: $holeNumber before triggering navigation update")
                triggerHoleNavigationUpdate()
                updateBackButtonVisibility(currentRound.value)
            } else {
                android.util.Log.w("PlayRoundVM", "Invalid hole number: $holeNumber (not in cycle for starting hole: $startingHole)")
            }
        }
    }

    private fun getMaxHoleNumber(game: MslGame): Int {
        val startingHole = game.startingHoleNumber
        val numberOfHoles = game.numberOfHoles ?: 18
        
        return when {
            startingHole == 1 && numberOfHoles == 18 -> 18
            startingHole == 1 && numberOfHoles == 9 -> 9
            startingHole == 10 && numberOfHoles == 9 -> 18
            else -> startingHole + numberOfHoles - 1
        }
    }

    private fun areAllHolesCompleted(): Boolean {
        val round = currentRound.value
        val game = localGame.value
        
        if (round == null || game == null) {
            android.util.Log.d("PlayRoundVM", "Missing round or game data - considering incomplete")
            return false
        }
        
        val startingHole = game.startingHoleNumber
        val numberOfHoles = game.numberOfHoles ?: 18
        
        // Get the cycle of holes for this round (same logic as navigation)
        val cycle = getCycleIndices(startingHole, numberOfHoles)
        val holeNumbers = cycle.map { it + 1 } // Convert from 0-based to 1-based hole numbers
        
        android.util.Log.d("PlayRoundVM", "Checking completion for cycle holes: $holeNumbers")
        
        // Check all holes in the cycle
        for (holeNumber in holeNumbers) {
            // Check main golfer strokes
            val mainGolferHole = round.holeScores.find { it.holeNumber == holeNumber }
            val mainGolferStrokes = mainGolferHole?.strokes ?: 0
            
            // Check partner strokes
            val partnerHole = round.playingPartnerRound?.holeScores?.find { it.holeNumber == holeNumber }
            val partnerStrokes = partnerHole?.strokes ?: 0
            
            android.util.Log.d("PlayRoundVM", "Hole $holeNumber: main=$mainGolferStrokes, partner=$partnerStrokes")
            
            // If either golfer has 0 strokes, round is not complete
            if (mainGolferStrokes <= 0 || partnerStrokes <= 0) {
                android.util.Log.d("PlayRoundVM", "Hole $holeNumber incomplete - main: $mainGolferStrokes, partner: $partnerStrokes")
                return false
            }
        }
        
        android.util.Log.d("PlayRoundVM", "All holes in cycle completed!")
        return true
    }

    // Stroke counting functions
    fun onMainGolferStrokeButtonClick() {
        updateMainGolferStrokes { currentStrokes, par ->
            if (currentStrokes == 0) {
                par // Set to par when strokes is 0
            } else {
                val maxStrokes = par * 5
                minOf(currentStrokes + 1, maxStrokes) // Increment by 1, capped at 5 × par
            }
        }
    }

    fun onMainGolferPlusButtonClick() {
        updateMainGolferStrokes { currentStrokes, par ->
            val maxStrokes = par * 5
            if (currentStrokes == 0) {
                minOf(par + 1, maxStrokes) // Set to par + 1 when strokes is 0
            } else {
                minOf(currentStrokes + 1, maxStrokes) // Increment by 1, capped at 5 × par
            }
        }
    }

    fun onMainGolferMinusButtonClick() {
        updateMainGolferStrokes { currentStrokes, par ->
            if (currentStrokes == 0) {
                par - 1 // Set to par - 1 when strokes is 0
            } else {
                currentStrokes - 1 // Decrement by 1
            }
        }
    }

    fun onPartnerStrokeButtonClick() {
        updatePartnerStrokes { currentStrokes, par ->
            if (currentStrokes == 0) {
                par // Set to par when strokes is 0
            } else {
                val maxStrokes = par * 5
                minOf(currentStrokes + 1, maxStrokes) // Increment by 1, capped at 5 × par
            }
        }
    }

    fun onPartnerPlusButtonClick() {
        updatePartnerStrokes { currentStrokes, par ->
            val maxStrokes = par * 5
            if (currentStrokes == 0) {
                minOf(par + 1, maxStrokes) // Set to par + 1 when strokes is 0
            } else {
                minOf(currentStrokes + 1, maxStrokes) // Increment by 1, capped at 5 × par
            }
        }
    }

    fun onPartnerMinusButtonClick() {
        updatePartnerStrokes { currentStrokes, par ->
            if (currentStrokes == 0) {
                par - 1 // Set to par - 1 when strokes is 0
            } else {
                currentStrokes - 1 // Decrement by 1
            }
        }
    }

    private fun updateMainGolferStrokes(updateLogic: (currentStrokes: Int, par: Int) -> Int) {
        viewModelScope.launch {
            try {
                val round = currentRound.value
                val currentHole = currentHoleNumber.value
                
                if (round == null) {
                    android.util.Log.w("PlayRoundVM", "Cannot update strokes - missing round data")
                    return@launch
                }

                // Get par for current hole from main golfer's data
                val par = getParForHole(round, currentHole, isMainGolfer = true) ?: 0
                
                if (par == 0) {
                    android.util.Log.w("PlayRoundVM", "Cannot update strokes - par is 0 for hole $currentHole")
                    return@launch
                }
                
                // Get current strokes for this hole using correct hole lookup
                val currentHoleScore = round.holeScores.find { it.holeNumber == currentHole }
                val currentStrokes = currentHoleScore?.strokes ?: 0

                // Calculate new strokes using the provided logic
                val newStrokes = updateLogic(currentStrokes, par)
                
                android.util.Log.d("PlayRoundVM", "Main golfer hole $currentHole: $currentStrokes -> $newStrokes (par: $par)")
                
                // Update the round data and persist to database
                updateRoundStrokesInDatabase(round, currentHole, newStrokes, isMainGolfer = true)
                
            } catch (e: Exception) {
                android.util.Log.e("PlayRoundVM", "Error updating main golfer strokes", e)
            }
        }
    }

    private fun updatePartnerStrokes(updateLogic: (currentStrokes: Int, par: Int) -> Int) {
        viewModelScope.launch {
            try {
                val round = currentRound.value
                val currentHole = currentHoleNumber.value
                
                if (round == null) {
                    android.util.Log.w("PlayRoundVM", "Cannot update strokes - missing round data")
                    return@launch
                }

                // Get par for current hole from partner's data
                val par = getParForHole(round, currentHole, isMainGolfer = false) ?: 0
                
                if (par == 0) {
                    android.util.Log.w("PlayRoundVM", "Cannot update strokes - par is 0 for hole $currentHole")
                    return@launch
                }
                
                // Get current strokes for this hole using correct hole lookup
                val currentHoleScore = round.playingPartnerRound?.holeScores?.find { it.holeNumber == currentHole }
                val currentStrokes = currentHoleScore?.strokes ?: 0

                // Calculate new strokes using the provided logic
                val newStrokes = updateLogic(currentStrokes, par)
                
                android.util.Log.d("PlayRoundVM", "Partner hole $currentHole: $currentStrokes -> $newStrokes (par: $par)")
                
                // Update the round data and persist to database
                updateRoundStrokesInDatabase(round, currentHole, newStrokes, isMainGolfer = false)
                
            } catch (e: Exception) {
                android.util.Log.e("PlayRoundVM", "Error updating partner strokes", e)
            }
        }
    }

    private fun getParForHole(round: com.sogo.golf.msl.domain.model.Round?, holeNumber: Int, isMainGolfer: Boolean): Int? {
        return try {
            if (round == null) return null
            
            // Get the par from the correct golfer's hole scores in the Round data
            val holeScore = if (isMainGolfer) {
                round.holeScores.find { it.holeNumber == holeNumber }
            } else {
                round.playingPartnerRound?.holeScores?.find { it.holeNumber == holeNumber }
            }
            
            holeScore?.par
        } catch (e: Exception) {
            android.util.Log.w("PlayRoundVM", "Error getting par for hole $holeNumber", e)
            null
        }
    }

    private suspend fun updateRoundStrokesInDatabase(
        round: com.sogo.golf.msl.domain.model.Round,
        holeNumber: Int,
        newStrokes: Int,
        isMainGolfer: Boolean
    ) {
        try {
            updateHoleScoreUseCase(round, holeNumber, newStrokes, isMainGolfer)
            android.util.Log.d("PlayRoundVM", "✅ Hole score updated - Hole $holeNumber, Strokes: $newStrokes, Main golfer: $isMainGolfer")
            
            loadCurrentRound()
            
        } catch (e: Exception) {
            android.util.Log.e("PlayRoundVM", "❌ Error updating round strokes in database", e)
        }
    }

    private fun triggerHoleNavigationUpdate() {
        viewModelScope.launch {
            try {
                val round = currentRound.value
                if (round != null) {
                    val currentHole = _currentHoleNumber.value
                    
                    val mainGolferStrokes = round.holeScores.find { it.holeNumber == currentHole }?.strokes ?: 0
                    val partnerStrokes = round.playingPartnerRound?.holeScores?.find { it.holeNumber == currentHole }?.strokes ?: 0
                    
                    if (mainGolferStrokes > 0) {
                        updateHoleScoreUseCase(round, currentHole, mainGolferStrokes, true)
                    }
                    if (partnerStrokes > 0) {
                        updateHoleScoreUseCase(round, currentHole, partnerStrokes, false)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PlayRoundVM", "Error triggering hole navigation update", e)
            }
        }
    }

    private fun saveCurrentHoleState(holeNumber: Int) {
        viewModelScope.launch {
            try {
                val round = currentRound.value
                if (round != null) {
                    holeStatePreferences.saveCurrentHole(round.id, holeNumber)
                }
            } catch (e: Exception) {
                android.util.Log.e("PlayRoundVM", "Error saving current hole state", e)
            }
        }
    }

    private fun restoreCurrentHoleOnAppStart(round: com.sogo.golf.msl.domain.model.Round) {
        viewModelScope.launch {
            try {
                // Only restore hole state on actual app startup, not during active play
                if (!isAppStartup) {
                    android.util.Log.d("PlayRoundVM", "🔄 Skipping hole restoration - not app startup")
                    return@launch
                }
                
                val savedCurrentHole = holeStatePreferences.getCurrentHole(round.id)
                
                val targetHole = savedCurrentHole ?: findStartingHole(round)
                
                if (savedCurrentHole != null) {
                    android.util.Log.d("PlayRoundVM", "🔄 App restart: Found saved current hole $savedCurrentHole")
                    // Only restore if we have a saved hole state
                    if (targetHole != _currentHoleNumber.value) {
                        _currentHoleNumber.value = targetHole
                        android.util.Log.d("PlayRoundVM", "✅ App restart: Restored to saved hole $targetHole")
                        updateBackButtonVisibility(round)
                    }
                } else {
                    android.util.Log.d("PlayRoundVM", "🔄 App restart: No saved hole state, letting game data set starting hole")
                    // Don't override here - let the game data flow handle initial hole setting
                }
                
                // Mark that app startup is complete
                isAppStartup = false
                
            } catch (e: Exception) {
                android.util.Log.e("PlayRoundVM", "Error restoring current hole on app start", e)
            }
        }
    }

    private fun findStartingHole(round: com.sogo.golf.msl.domain.model.Round): Int {
        val game = localGame.value
        return game?.startingHoleNumber ?: 1
    }

    private fun getHoleIndex(holeNumber: Int): Int {
        val game = localGame.value
        val startingHole = game?.startingHoleNumber ?: 1
        val numberOfHoles = game?.numberOfHoles ?: 18
        
        val cycle = getCycleIndices(startingHole, numberOfHoles)
        return cycle.indexOf(holeNumber - 1)
    }

    private fun getCycleIndices(startingHole: Int, numberOfHoles: Int): List<Int> {
        val maxHole = when {
            numberOfHoles == 18 -> 18  // Any 18-hole round uses holes 1-18
            startingHole >= 10 && numberOfHoles == 9 -> 18  // 10-18 hole range
            startingHole >= 1 && startingHole <= 9 && numberOfHoles == 9 -> 9  // 1-9 hole range
            else -> startingHole + numberOfHoles - 1
        }
        
        val holeNumbers = mutableListOf<Int>()
        var currentHole = startingHole
        
        repeat(numberOfHoles) {
            holeNumbers.add(currentHole)
            currentHole++
            if (currentHole > maxHole) {
                currentHole = when {
                    // 10-18 hole rounds: wrap from 18 back to 10
                    startingHole >= 10 && numberOfHoles == 9 -> 10
                    // All other rounds: wrap back to 1
                    else -> 1
                }
            }
        }
        
        return holeNumbers.map { it - 1 }
    }


    fun clearCurrentHoleForRound() {
        viewModelScope.launch {
            try {
                val round = currentRound.value
                if (round != null) {
                    holeStatePreferences.clearCurrentHole(round.id)
                    android.util.Log.d("PlayRoundVM", "🗑️ Cleared current hole for round: ${round.id}")
                }
            } catch (e: Exception) {
                android.util.Log.e("PlayRoundVM", "Error clearing current hole", e)
            }
        }
    }

    private fun triggerBulkSyncIfNeeded() {
        viewModelScope.launch {
            try {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastBulkSyncTime < BULK_SYNC_DEBOUNCE_MS) {
                    android.util.Log.d("PlayRoundVM", "🔄 Debouncing bulk sync - too soon since last sync")
                    return@launch
                }
                
                lastBulkSyncTime = currentTime
                val syncResult = bulkSyncRoundUseCase()
                if (syncResult) {
                    android.util.Log.d("PlayRoundVM", "✅ Bulk sync completed successfully")
                    // Reload current round to refresh UI with synced state
                    loadCurrentRound()
                } else {
                    android.util.Log.d("PlayRoundVM", "ℹ️ Bulk sync not needed or failed silently")
                }
            } catch (e: Exception) {
                android.util.Log.w("PlayRoundVM", "⚠️ Error during bulk sync trigger", e)
            }
        }
    }

    fun calculateCurrentPoints(
        strokes: Int,
        par: Int,
        index1: Int,
        index2: Int,
        index3: Int,
        dailyHandicap: Double,
        scoreType: String
    ): Int {
        return try {
            val holeScoreForCalcs = HoleScoreForCalcs(
                par = par,
                index1 = index1,
                index2 = index2,
                index3 = index3
            )

            // Debug logging for scoring discrepancy investigation
            Log.d("PlayRoundVM", "=== SCORING DEBUG ===")
            Log.d("PlayRoundVM", "Input parameters:")
            Log.d("PlayRoundVM", "  strokes: $strokes")
            Log.d("PlayRoundVM", "  par: $par")
            Log.d("PlayRoundVM", "  index1: $index1")
            Log.d("PlayRoundVM", "  index2: $index2")
            Log.d("PlayRoundVM", "  index3: $index3")
            Log.d("PlayRoundVM", "  dailyHandicap: $dailyHandicap")
            Log.d("PlayRoundVM", "  scoreType: $scoreType")
            
            // Calculate net par to see intermediate result
            val netPar = calcHoleNetParUseCase.invoke(holeScoreForCalcs, dailyHandicap)
            Log.d("PlayRoundVM", "  calculated netPar: $netPar")

            val score = when (scoreType.lowercase()) {
                "stableford" -> calcStablefordUseCase(holeScoreForCalcs, dailyHandicap, strokes)
                "par" -> calcParUseCase(strokes, holeScoreForCalcs, dailyHandicap) ?: 0f
                "stroke" -> calcStrokeUseCase(strokes, holeScoreForCalcs, dailyHandicap)
                else -> calcStablefordUseCase(holeScoreForCalcs, dailyHandicap, strokes)
            }

            Log.d("PlayRoundVM", "  calculated score (float): $score")
            Log.d("PlayRoundVM", "  final score (int): ${score.toInt()}")
            Log.d("PlayRoundVM", "=== END SCORING DEBUG ===")

            score.toInt()
        } catch (e: Exception) {
            Log.e("PlayRoundVM", "Error calculating current points", e)
            0
        }
    }

    fun clearAbandonError() {
        _abandonError.value = null
    }

    fun abandonRound(navController: NavController) {
        android.util.Log.d("PlayRoundVM", "=== abandonRound called ===")

        viewModelScope.launch {
            android.util.Log.d("PlayRoundVM", "Starting round abandonment process")
            _isAbandoningRound.value = true
            _abandonError.value = null

            try {
                // First, remove marker from playing partner (following removeMarkerAndNavigateBack pattern)
                val partnerGolfLinkNumber = getPartnerMarkedByMe()
                android.util.Log.d("PlayRoundVM", "Partner golf link number: $partnerGolfLinkNumber")
                
                if (partnerGolfLinkNumber != null) {
                    android.util.Log.d("PlayRoundVM", "Removing marker from playing partner: $partnerGolfLinkNumber")
                    try {
                        when (val markerResult = removeMarkerUseCase(partnerGolfLinkNumber)) {
                            is NetworkResult.Success -> {
                                android.util.Log.d("PlayRoundVM", "✅ Marker removed successfully")
                            }
                            is NetworkResult.Error -> {
                                android.util.Log.e("PlayRoundVM", "❌ Failed to remove marker: ${markerResult.error.toUserMessage()}")
                            }
                            is NetworkResult.Loading -> {
                                android.util.Log.w("PlayRoundVM", "Unexpected loading state during marker removal")
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("PlayRoundVM", "❌ Exception during marker removal", e)
                    }
                } else {
                    android.util.Log.d("PlayRoundVM", "No marker to remove, proceeding to round deletion")
                }

                // Second, delete the current round if it exists
                val currentRound = _currentRound.value
                if (currentRound != null) {
                    android.util.Log.d("PlayRoundVM", "Deleting current round: ${currentRound.id}")
                    when (val deleteResult = deleteLocalAndRemoteRoundUseCase(currentRound.id)) {
                        is NetworkResult.Success -> {
                            android.util.Log.d("PlayRoundVM", "✅ Round deleted successfully")
                        }
                        is NetworkResult.Error -> {
                            android.util.Log.e("PlayRoundVM", "❌ Failed to delete round: ${deleteResult.error.toUserMessage()}")
                            _isAbandoningRound.value = false
                            _abandonError.value = "Failed to delete round: ${deleteResult.error.toUserMessage()}"
                            return@launch
                        }
                        is NetworkResult.Loading -> {
                            // This shouldn't happen in this context, but handle it gracefully
                            android.util.Log.w("PlayRoundVM", "Unexpected loading state during round deletion")
                        }
                    }
                }

                // Finally, reset all stale data for comprehensive state clearing
                android.util.Log.d("PlayRoundVM", "Resetting stale data...")
                when (val resetResult = resetStaleDataUseCase()) {
                    is Result -> {
                        if (resetResult.isSuccess) {
                            android.util.Log.d("PlayRoundVM", "✅ Round abandoned successfully")
                            _isAbandoningRound.value = false

                            android.util.Log.d("PlayRoundVM", "Navigating to home screen")
                            navController.navigate("homescreen") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            android.util.Log.e("PlayRoundVM", "❌ Failed to reset stale data: ${resetResult.exceptionOrNull()}")
                            _isAbandoningRound.value = false
                            _abandonError.value = "Failed to reset data: ${resetResult.exceptionOrNull()?.message ?: "Unknown error"}"
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PlayRoundVM", "❌ Unexpected error during round abandonment", e)
                _isAbandoningRound.value = false
                _abandonError.value = "Unexpected error: ${e.message ?: "Unknown error"}"
            }
        }
    }

    
    fun onMainGolferPickupButtonClick() {
        android.util.Log.d("PlayRoundVM", "=== Main golfer pickup button clicked ===")
        handlePickupButtonClick(isMainGolfer = true)
    }
    
    fun onPartnerPickupButtonClick() {
        android.util.Log.d("PlayRoundVM", "=== Partner pickup button clicked ===")
        handlePickupButtonClick(isMainGolfer = false)
    }
    
    private fun handlePickupButtonClick(isMainGolfer: Boolean) {
        viewModelScope.launch {
            try {
                val round = currentRound.value ?: return@launch
                val game = localGame.value ?: return@launch
                val currentHole = _currentHoleNumber.value
                
                // Get hole data from the correct golfer's round data
                val holeData = if (isMainGolfer) {
                    round.holeScores.find { it.holeNumber == currentHole }
                } else {
                    round.playingPartnerRound?.holeScores?.find { it.holeNumber == currentHole }
                } ?: return@launch
                
                val dailyHandicap = if (isMainGolfer) {
                    game.dailyHandicap?.toDouble() ?: 0.0
                } else {
                    game.playingPartners.find { partner ->
                        partner.markedByGolfLinkNumber == currentGolfer.value?.golfLinkNo
                    }?.dailyHandicap?.toDouble() ?: 0.0
                }
                
                android.util.Log.d("PlayRoundVM", "Pickup button clicked - updating local state immediately")
                
                // Check current pickup state before updating to track the new state
                val currentPickupState = if (isMainGolfer) {
                    round.holeScores.find { it.holeNumber == currentHole }?.isBallPickedUp ?: false
                } else {
                    round.playingPartnerRound?.holeScores?.find { it.holeNumber == currentHole }?.isBallPickedUp ?: false
                }
                val newPickupState = !currentPickupState
                
                // Track the pickup tap event with the new state
                trackPickupTapped(isMainGolfer, newPickupState)
                
                // Update pickup state locally first (local-first approach)
                updatePickupUseCase(
                    round = round,
                    holeNumber = currentHole,
                    isMainGolfer = isMainGolfer,
                    dailyHandicap = dailyHandicap,
                    par = holeData.par,
                    index1 = holeData.index1,
                    index2 = holeData.index2,
                    index3 = holeData.index3
                )
                
                // Reload round to update UI immediately
                loadCurrentRound()
                
                // Sync to remote in background (don't block UI)
                val updatedRound = currentRound.value
                if (updatedRound != null) {
                    updatePickupUseCase.syncToRemote(updatedRound)
                }
                
            } catch (e: Exception) {
                android.util.Log.e("PlayRoundVM", "Error handling pickup button click", e)
            }
        }
    }

    fun hideGoToHoleDialog() {
        _showGoToHoleDialog.value = false
    }

    fun showGoToHoleDialog() {
        _showGoToHoleDialog.value = true
    }
    
    /**
     * Gets the valid hole range for the current course based on competition data.
     * This ensures the GoToHole dialog only shows holes that exist on the course.
     */
    fun getValidHoleRange(): IntRange? {
        val game = localGame.value ?: return null
        val competition = localCompetition.value ?: return null
        
        // Get holes from competition data to determine actual available holes
        val competitionHoles = competition.players.firstOrNull()?.holes ?: return null
        
        if (competitionHoles.isEmpty()) return null
        
        // Get the actual hole numbers from competition data
        val holeNumbers = competitionHoles.map { it.holeNumber }.sorted()
        val minHole = holeNumbers.firstOrNull() ?: game.startingHoleNumber
        val maxHole = holeNumbers.lastOrNull() ?: getMaxHoleNumber(game)
        
        return minHole..maxHole
    }

    fun trackRoundResetMarker() {
        val eventProperties = mutableMapOf<String, Any>()
        
        // Add the golfer whose marker was removed (stored before removal)
        removedPartnerGolfLinkNumber?.let { partnerNo ->
            eventProperties["removed_marker"] = partnerNo
        }
        
        analyticsManager.trackEvent(AnalyticsManager.EVENT_ROUND_RESET_MARKER, eventProperties)
    }

    fun trackScorecardViewed() {
        val eventProperties = mutableMapOf<String, Any>()
        
        // Add main golfer details
        currentGolfer.value?.let { golfer ->
            eventProperties["golferGLNumber"] = golfer.golfLinkNo ?: ""
            eventProperties["golfer_name"] = "${golfer.firstName ?: ""} ${golfer.surname ?: ""}".trim()
        }
        
        // Add playing partner details
        val partnerGolfLinkNumber = getPartnerMarkedByMe()
        partnerGolfLinkNumber?.let { partnerNo ->
            localGame.value?.playingPartners?.find { 
                it.golfLinkNumber == partnerNo 
            }?.let { partner ->
                eventProperties["playing_partner_GLNumber"] = partnerNo
                eventProperties["playing_partner_name"] = "${partner.firstName ?: ""} ${partner.lastName ?: ""}".trim()
            }
        }
        
        // Add current hole number
        eventProperties["holeNumber"] = _currentHoleNumber.value
        
        analyticsManager.trackEvent(AnalyticsManager.EVENT_SCORECARD_VIEWED, eventProperties)
    }

    fun trackPickupTapped(isMainGolfer: Boolean, pickupState: Boolean) {
        val eventProperties = mutableMapOf<String, Any>()
        
        if (isMainGolfer) {
            // Main golfer details
            currentGolfer.value?.let { golfer ->
                eventProperties["golfer_name"] = "${golfer.firstName ?: ""} ${golfer.surname ?: ""}".trim()
                eventProperties["golflinkNo"] = golfer.golfLinkNo ?: ""
            }
        } else {
            // Playing partner details
            val partnerGolfLinkNumber = getPartnerMarkedByMe()
            partnerGolfLinkNumber?.let { partnerNo ->
                localGame.value?.playingPartners?.find { 
                    it.golfLinkNumber == partnerNo 
                }?.let { partner ->
                    eventProperties["golfer_name"] = "${partner.firstName ?: ""} ${partner.lastName ?: ""}".trim()
                    eventProperties["golflinkNo"] = partnerNo
                }
            }
        }
        
        eventProperties["pickup"] = pickupState
        eventProperties["holeNumber"] = _currentHoleNumber.value
        
        analyticsManager.trackEvent(AnalyticsManager.EVENT_PICKUP_TAPPED, eventProperties)
    }
}
