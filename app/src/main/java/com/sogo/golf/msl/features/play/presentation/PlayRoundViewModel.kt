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
import com.sogo.golf.msl.domain.model.HoleScoreForCalcs
import com.sogo.golf.msl.data.network.NetworkStateMonitor
import android.util.Log
import com.sogo.golf.msl.data.network.NetworkState
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
    private val updateHoleScoreUseCase: UpdateHoleScoreUseCase,
    private val bulkSyncRoundUseCase: BulkSyncRoundUseCase,
    private val calcStablefordUseCase: CalcStablefordUseCase,
    private val calcParUseCase: CalcParUseCase,
    private val calcStrokeUseCase: CalcStrokeUseCase,
    private val networkStateMonitor: NetworkStateMonitor,
    private val holeStatePreferences: HoleStatePreferences,
    private val mslRepository: MslRepository,
    private val mslGolferLocalDbRepository: MslGolferLocalDbRepository,
    private val appLifecycleManager: AppLifecycleManager,
    private val gameDataTimestampPreferences: GameDataTimestampPreferences,
    private val resetStaleDataUseCase: ResetStaleDataUseCase,
    private val updatePickupUseCase: UpdatePickupUseCase,
) : ViewModel() {

    private val _deleteMarkerEnabled = MutableStateFlow(false)
    val deleteMarkerEnabled: StateFlow<Boolean> = _deleteMarkerEnabled.asStateFlow()

    private val _isRemovingMarker = MutableStateFlow(false)
    val isRemovingMarker: StateFlow<Boolean> = _isRemovingMarker.asStateFlow()

    private val _markerError = MutableStateFlow<String?>(null)
    val markerError: StateFlow<String?> = _markerError.asStateFlow()

    private val _isAbandoningRound = MutableStateFlow(false)
    val isAbandoningRound: StateFlow<Boolean> = _isAbandoningRound.asStateFlow()

    private val _abandonError = MutableStateFlow<String?>(null)
    val abandonError: StateFlow<String?> = _abandonError.asStateFlow()

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
                if (game != null && _currentHoleNumber.value == 1) {
                    // Set starting hole number from game data
                    val startingHole = game.startingHoleNumber
                    _currentHoleNumber.value = startingHole
                    android.util.Log.d("PlayRoundVM", "Set starting hole number to: $startingHole")
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

    // Hole navigation functions
    fun navigateToNextHole() {
        val game = localGame.value
        if (game != null) {
            val currentHole = _currentHoleNumber.value
            val maxHole = getMaxHoleNumber(game)
            
            if (currentHole < maxHole) {
                val newHole = currentHole + 1
                _currentHoleNumber.value = newHole
                android.util.Log.d("PlayRoundVM", "Navigated to hole: $newHole")
                saveCurrentHoleState(newHole)
                triggerHoleNavigationUpdate()
                updateBackButtonVisibility(currentRound.value)
            } else {
                android.util.Log.d("PlayRoundVM", "Already at last hole: $maxHole")
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
            val minHole = game.startingHoleNumber
            
            if (currentHole > minHole) {
                val newHole = currentHole - 1
                _currentHoleNumber.value = newHole
                android.util.Log.d("PlayRoundVM", "Navigated to hole: $newHole")
                saveCurrentHoleState(newHole)
                triggerHoleNavigationUpdate()
                updateBackButtonVisibility(currentRound.value)
            } else {
                android.util.Log.d("PlayRoundVM", "Already at first hole: $minHole")
            }
        }
    }

    fun navigateToHole(holeNumber: Int) {
        val game = localGame.value
        if (game != null) {
            val minHole = game.startingHoleNumber
            val maxHole = getMaxHoleNumber(game)
            
            if (holeNumber in minHole..maxHole) {
                _currentHoleNumber.value = holeNumber
                android.util.Log.d("PlayRoundVM", "Navigated to hole: $holeNumber")
                saveCurrentHoleState(holeNumber)
                triggerHoleNavigationUpdate()
                updateBackButtonVisibility(currentRound.value)
            } else {
                android.util.Log.w("PlayRoundVM", "Invalid hole number: $holeNumber (valid range: $minHole-$maxHole)")
            }
        }
    }

    private fun getMaxHoleNumber(game: MslGame): Int {
        val startingHole = game.startingHoleNumber
        val numberOfHoles = game.numberOfHoles ?: 18
        
        return when {
            // 18-hole round starting from hole 1
            startingHole == 1 && numberOfHoles == 18 -> 18
            // 9-hole round starting from hole 1 (front nine)
            startingHole == 1 && numberOfHoles == 9 -> 9
            // 9-hole round starting from hole 10 (back nine)
            startingHole == 10 && numberOfHoles == 9 -> 18
            // Default fallback
            else -> startingHole + numberOfHoles - 1
        }
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
                val competition = localCompetition.value
                val currentHole = currentHoleNumber.value
                
                if (round == null || competition == null) {
                    android.util.Log.w("PlayRoundVM", "Cannot update strokes - missing round or competition data")
                    return@launch
                }

                // Get par for current hole
                val par = getParForHole(competition, currentHole) ?: 4 // Default to par 4
                
                // Get current strokes and pickup state for this hole
                val holeIndex = currentHole - 1 // Convert to 0-based index
                val currentHoleScore = if (holeIndex < round.holeScores.size) {
                    round.holeScores[holeIndex]
                } else {
                    null
                }
                
                val currentStrokes = currentHoleScore?.strokes ?: 0
                val isPickedUp = currentHoleScore?.isBallPickedUp ?: false
                
                // If ball is picked up, reset pickup state first
                if (isPickedUp) {
                    android.util.Log.d("PlayRoundVM", "Main golfer - Ball was picked up, resetting pickup state before stroke update")
                    
                    val game = localGame.value ?: return@launch
                    val holeData = competition.players.firstOrNull()?.holes?.find { 
                        it.holeNumber == currentHole 
                    } ?: return@launch
                    
                    val dailyHandicap = game.dailyHandicap?.toDouble() ?: 0.0
                    
                    // Reset pickup state to false
                    updatePickupUseCase(
                        round = round,
                        holeNumber = currentHole,
                        isMainGolfer = true,
                        dailyHandicap = dailyHandicap,
                        par = holeData.par,
                        index1 = holeData.strokeIndexes.getOrNull(0) ?: 0,
                        index2 = holeData.strokeIndexes.getOrNull(1) ?: 0,
                        index3 = holeData.strokeIndexes.getOrNull(2)
                    )
                    
                    // Reload round to get updated state
                    loadCurrentRound()
                    
                    // Sync pickup reset to remote
                    val updatedRound = currentRound.value
                    if (updatedRound != null) {
                        updatePickupUseCase.syncToRemote(updatedRound)
                    }
                }

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
                val competition = localCompetition.value
                val currentHole = currentHoleNumber.value
                
                if (round == null || competition == null) {
                    android.util.Log.w("PlayRoundVM", "Cannot update strokes - missing round or competition data")
                    return@launch
                }

                // Get par for current hole
                val par = getParForHole(competition, currentHole) ?: 4 // Default to par 4
                
                // Get current strokes and pickup state for this hole
                val holeIndex = currentHole - 1 // Convert to 0-based index
                val currentHoleScore = if (round.playingPartnerRound != null && holeIndex < round.playingPartnerRound.holeScores.size) {
                    round.playingPartnerRound.holeScores[holeIndex]
                } else {
                    null
                }
                
                val currentStrokes = currentHoleScore?.strokes ?: 0
                val isPickedUp = currentHoleScore?.isBallPickedUp ?: false
                
                // If ball is picked up, reset pickup state first
                if (isPickedUp) {
                    android.util.Log.d("PlayRoundVM", "Partner - Ball was picked up, resetting pickup state before stroke update")
                    
                    val game = localGame.value ?: return@launch
                    val holeData = competition.players.firstOrNull()?.holes?.find { 
                        it.holeNumber == currentHole 
                    } ?: return@launch
                    
                    val dailyHandicap = game.playingPartners.find { partner ->
                        partner.markedByGolfLinkNumber == currentGolfer.value?.golfLinkNo
                    }?.dailyHandicap?.toDouble() ?: 0.0
                    
                    // Reset pickup state to false
                    updatePickupUseCase(
                        round = round,
                        holeNumber = currentHole,
                        isMainGolfer = false,
                        dailyHandicap = dailyHandicap,
                        par = holeData.par,
                        index1 = holeData.strokeIndexes.getOrNull(0) ?: 0,
                        index2 = holeData.strokeIndexes.getOrNull(1) ?: 0,
                        index3 = holeData.strokeIndexes.getOrNull(2)
                    )
                    
                    // Reload round to get updated state
                    loadCurrentRound()
                    
                    // Sync pickup reset to remote
                    val updatedRound = currentRound.value
                    if (updatedRound != null) {
                        updatePickupUseCase.syncToRemote(updatedRound)
                    }
                }

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

    private fun getParForHole(competition: com.sogo.golf.msl.domain.model.msl.MslCompetition, holeNumber: Int): Int? {
        return try {
            // Find the hole data in competition structure
            competition.players.firstOrNull()?.holes?.find { hole ->
                hole.holeNumber == holeNumber
            }?.par
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
                    val holeIndex = currentHole - 1
                    
                    val mainGolferStrokes = round.holeScores.getOrNull(holeIndex)?.strokes ?: 0
                    val partnerStrokes = round.playingPartnerRound?.holeScores?.getOrNull(holeIndex)?.strokes ?: 0
                    
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
                val savedCurrentHole = holeStatePreferences.getCurrentHole(round.id)
                
                val targetHole = savedCurrentHole ?: findStartingHole(round)
                
                if (savedCurrentHole != null) {
                    android.util.Log.d("PlayRoundVM", "🔄 App restart: Found saved current hole $savedCurrentHole")
                } else {
                    android.util.Log.d("PlayRoundVM", "🔄 App restart: No saved hole state, using starting hole $targetHole")
                }
                
                if (targetHole != _currentHoleNumber.value) {
                    _currentHoleNumber.value = targetHole
                    android.util.Log.d("PlayRoundVM", "✅ App restart: Navigated to hole $targetHole")
                    updateBackButtonVisibility(round)
                }
                
            } catch (e: Exception) {
                android.util.Log.e("PlayRoundVM", "Error restoring current hole on app start", e)
            }
        }
    }

    private fun findStartingHole(round: com.sogo.golf.msl.domain.model.Round): Int {
        val game = localGame.value
        return game?.startingHoleNumber ?: 1
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

            val score = when (scoreType.lowercase()) {
                "stableford" -> calcStablefordUseCase(holeScoreForCalcs, dailyHandicap, strokes)
                "par" -> calcParUseCase(strokes, holeScoreForCalcs, dailyHandicap) ?: 0f
                "stroke" -> calcStrokeUseCase(strokes, holeScoreForCalcs, dailyHandicap)
                else -> calcStablefordUseCase(holeScoreForCalcs, dailyHandicap, strokes)
            }

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
                    when (val markerResult = removeMarkerUseCase(partnerGolfLinkNumber)) {
                        is NetworkResult.Success -> {
                            android.util.Log.d("PlayRoundVM", "✅ Marker removed successfully")
                        }
                        is NetworkResult.Error -> {
                            android.util.Log.e("PlayRoundVM", "❌ Failed to remove marker: ${markerResult.error.toUserMessage()}")
                            _isAbandoningRound.value = false
                            _abandonError.value = "Failed to remove marker: ${markerResult.error.toUserMessage()}"
                            return@launch
                        }
                        is NetworkResult.Loading -> {
                            android.util.Log.w("PlayRoundVM", "Unexpected loading state during marker removal")
                        }
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
                val competition = localCompetition.value ?: return@launch
                val game = localGame.value ?: return@launch
                val currentHole = _currentHoleNumber.value
                
                val holeData = competition.players.firstOrNull()?.holes?.find { 
                    it.holeNumber == currentHole 
                } ?: return@launch
                
                val dailyHandicap = if (isMainGolfer) {
                    game.dailyHandicap?.toDouble() ?: 0.0
                } else {
                    game.playingPartners.find { partner ->
                        partner.markedByGolfLinkNumber == currentGolfer.value?.golfLinkNo
                    }?.dailyHandicap?.toDouble() ?: 0.0
                }
                
                val currentPickupState = if (isMainGolfer) {
                    round.holeScores.find { it.holeNumber == currentHole }?.isBallPickedUp ?: false
                } else {
                    round.playingPartnerRound?.holeScores?.find { it.holeNumber == currentHole }?.isBallPickedUp ?: false
                }
                
                android.util.Log.d("PlayRoundVM", "Pickup - Hole: $currentHole, Par: ${holeData.par}, Daily handicap: $dailyHandicap")
                android.util.Log.d("PlayRoundVM", "Current pickup state from Room: $currentPickupState (${if (isMainGolfer) "Main Golfer" else "Partner"})")
                
                updatePickupUseCase(
                    round = round,
                    holeNumber = currentHole,
                    isMainGolfer = isMainGolfer,
                    dailyHandicap = dailyHandicap,
                    par = holeData.par,
                    index1 = holeData.strokeIndexes.getOrNull(0) ?: 0,
                    index2 = holeData.strokeIndexes.getOrNull(1) ?: 0,
                    index3 = holeData.strokeIndexes.getOrNull(2)
                )
                
                loadCurrentRound()
                
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
}
