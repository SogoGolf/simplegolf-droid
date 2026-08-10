package com.sogo.golf.msl.features.review_scores.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.gson.Gson
import com.sogo.golf.msl.common.Resource
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.model.HoleStats
import com.sogo.golf.msl.domain.model.msl.v2.HolePayload
import com.sogo.golf.msl.domain.model.msl.v2.ScoresContainer
import com.sogo.golf.msl.domain.model.msl.v2.ScoresPayload
import com.sogo.golf.msl.domain.repository.RoundLocalDbRepository
import com.sogo.golf.msl.domain.repository.MslCompetitionLocalDbRepository
import com.sogo.golf.msl.domain.usecase.round.GetRoundUseCase
import com.sogo.golf.msl.domain.usecase.round.SubmitRoundUseCase
import com.sogo.golf.msl.domain.usecase.round.HoleStatsRemoteSync
import com.sogo.golf.msl.domain.usecase.round.RequiredHoleStatsSyncException
import com.sogo.golf.msl.domain.usecase.round.RoundSyncCoordinator
import com.sogo.golf.msl.domain.usecase.round.UpdateHoleStatsUseCase
import com.sogo.golf.msl.domain.usecase.date.ResetStaleDataUseCase
import com.sogo.golf.msl.data.local.preferences.HoleStatePreferences
import com.sogo.golf.msl.domain.usecase.club.GetMslClubAndTenantIdsUseCase
import com.sogo.golf.msl.domain.usecase.game.GetLocalGameUseCase
import com.sogo.golf.msl.shared.utils.HoleCycleUtils
import com.sogo.golf.msl.shared.utils.TimeFormatUtils
import kotlinx.coroutines.flow.firstOrNull
import com.sogo.golf.msl.domain.repository.remote.SogoMongoRepository
import com.sogo.golf.msl.analytics.AnalyticsManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@HiltViewModel(assistedFactory = ReviewScoresViewModel.Factory::class)
class ReviewScoresViewModel @AssistedInject constructor(
    private val getRoundUseCase: GetRoundUseCase,
    private val roundRepository: RoundLocalDbRepository,
    private val competitionRepository: MslCompetitionLocalDbRepository,
    private val submitRoundUseCase: SubmitRoundUseCase,
    private val updateHoleStatsUseCase: UpdateHoleStatsUseCase,
    private val getMslClubAndTenantIdsUseCase: GetMslClubAndTenantIdsUseCase,
    private val resetStaleDataUseCase: ResetStaleDataUseCase,
    private val holeStatePreferences: HoleStatePreferences,
    private val getLocalGameUseCase: GetLocalGameUseCase,
    private val sogoMongoRepository: SogoMongoRepository,
    private val roundSyncCoordinator: RoundSyncCoordinator,
    private val analyticsManager: AnalyticsManager,
    @Assisted private val navController: NavController
) : ViewModel() {

    companion object {
        private const val TAG = "ReviewScoresViewModel"
    }

    private val _uiState = MutableStateFlow(ReviewScoresUiState())
    val uiState: StateFlow<ReviewScoresUiState> = _uiState.asStateFlow()

    private val _currentRoundId = MutableStateFlow<String?>(null)
    
    private val _playerSignatures = MutableStateFlow<Map<String, String>>(emptyMap())
    val playerSignatures: StateFlow<Map<String, String>> = _playerSignatures.asStateFlow()

    private val _roundSubmitState = MutableStateFlow(RoundSubmitState())
    val roundSubmitState: StateFlow<RoundSubmitState> = _roundSubmitState.asStateFlow()

    // Review edits are serialized so whole-Round Room rows cannot overwrite one another. The
    // map retains each latest edit until its targeted Mongo PATCH has definitely succeeded.
    private val statsSaveMutex = Mutex()
    private val pendingStatsByHole = mutableMapOf<Int, HoleStats>()
    private val statsEditVersionByHole = mutableMapOf<Int, Long>()
    private val statsSaveFailureByHole = mutableMapOf<Int, Throwable>()
    private var latestStatsSaveJob: Job? = null
    private var activeStatsSaveCount = 0
    private var roundLoadGeneration = 0L

    val currentRound = _currentRoundId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // Get competition from local storage
    val currentCompetition = competitionRepository.getCompetition()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun loadRound(roundId: String) {
        android.util.Log.d(TAG, "Loading round: $roundId")
        val generation = ++roundLoadGeneration
        _currentRoundId.value = roundId
        
        viewModelScope.launch {
            try {
                val initialRound = getRoundUseCase.getRoundById(roundId)
                if (initialRound != null) {
                    val roundTimeText = computeRoundTimeText(roundId, initialRound)

                    // Orientation changes can start overlapping loads. Re-read Room after the
                    // slower pace calculation, then allow only the newest request to publish.
                    val authoritativeRound = roundRepository.getRoundById(roundId)
                    if (!canPublishReviewRoundLoad(
                            requestGeneration = generation,
                            latestGeneration = roundLoadGeneration,
                            requestedRoundId = roundId,
                            activeRoundId = _currentRoundId.value
                        )
                    ) {
                        return@launch
                    }
                    if (authoritativeRound == null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Round not found"
                        )
                        return@launch
                    }

                    if (_uiState.value.round?.id != authoritativeRound.id) {
                        pendingStatsByHole.clear()
                        statsEditVersionByHole.clear()
                        statsSaveFailureByHole.clear()
                    }

                    // If this screen was recreated with a dirty round, in-memory provenance is
                    // gone. Target-flush every populated stats object before allowing submit.
                    // putIfAbsent preserves edits made while an older load was still running.
                    if (!authoritativeRound.isSynced) {
                        authoritativeRound.holeScores.forEach { hole ->
                            hole.stats?.let { pendingStatsByHole.putIfAbsent(hole.holeNumber, it) }
                        }
                    }

                    val visibleRound = authoritativeRound.withPendingReviewStats(pendingStatsByHole)
                    _uiState.value = _uiState.value.copy(
                        round = visibleRound,
                        isLoading = false,
                        errorMessage = null,
                        roundTimeText = roundTimeText
                    )
                    android.util.Log.d(TAG, "Round loaded successfully: ${visibleRound.golferFirstName} ${visibleRound.golferLastName}")
                } else {
                    if (!canPublishReviewRoundLoad(generation, roundLoadGeneration, roundId, _currentRoundId.value)) {
                        return@launch
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Round not found"
                    )
                    android.util.Log.w(TAG, "Round not found: $roundId")
                }
            } catch (e: Exception) {
                if (!canPublishReviewRoundLoad(generation, roundLoadGeneration, roundId, _currentRoundId.value)) {
                    return@launch
                }
                android.util.Log.e(TAG, "Error loading round", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading round: ${e.message}"
                )
            }
        }
    }

    /**
     * "Round Time" for the submit-success dialog: total time since the round's
     * (booked) start as of entering the FINAL hole's score. The Play screen
     * freezes each hole's completion into HoleStatePreferences the moment its
     * second score lands, so review/sign/submit time never counts. The final
     * CYCLE hole's snapshot is used (not the latest of any hole) so a skipped
     * hole backfilled after the final one doesn't inflate the value — matching
     * iOS exactly. Computed here at screen load because post-Done cleanup
     * purges the local game (the booked tee time) — and null (row hidden)
     * when never captured, e.g. a round played before this feature shipped.
     */
    private suspend fun computeRoundTimeText(roundId: String, round: Round): String? {
        return try {
            val snapshots = holeStatePreferences.getPaceSnapshots(roundId)
            if (snapshots.isEmpty()) return null
            val game = getLocalGameUseCase().firstOrNull()
            val finalHole = game?.let {
                HoleCycleUtils.buildHoleCycle(it.startingHoleNumber, it.numberOfHoles ?: 18).lastOrNull()
            }
            // Fallback (game unexpectedly gone): latest snapshot of any hole.
            val finalHoleMillis = finalHole?.let { snapshots[it] } ?: snapshots.values.maxOrNull()
                ?: return null
            val startMillis = TimeFormatUtils.resolvePaceStartMillis(game, round)
                ?: return null
            if (finalHoleMillis <= startMillis) return null
            TimeFormatUtils.formatFriendlyDuration(
                ((finalHoleMillis - startMillis) / 1000L).toInt()
            )
        } catch (e: Exception) {
            android.util.Log.w(TAG, "Could not compute round time", e)
            null
        }
    }

    fun calculateFrontNineScore(round: Round): Int {
        return round.holeScores
            .filter { it.holeNumber in 1..9 }
            .sumOf { it.strokes }
    }

    fun calculateBackNineScore(round: Round): Int {
        return round.holeScores
            .filter { it.holeNumber in 10..18 }
            .sumOf { it.strokes }
    }

    fun calculateGrandTotal(round: Round): Int {
        return round.holeScores.sumOf { it.strokes }
    }

    fun updatePlayerSignature(playerId: String, signatureBase64: String) {
        android.util.Log.d(TAG, "Updating signature for player: $playerId")
        val currentSignatures = _playerSignatures.value.toMutableMap()
        currentSignatures[playerId] = signatureBase64
        _playerSignatures.value = currentSignatures
        
        // Track signature captured event
        trackSignatureCaptured(playerId)
    }

    fun clearPlayerSignature(playerId: String) {
        android.util.Log.d(TAG, "Clearing signature for player: $playerId")
        val currentSignatures = _playerSignatures.value.toMutableMap()
        currentSignatures.remove(playerId)
        _playerSignatures.value = currentSignatures
    }

    fun onErrorDialogDismissed() {
        val shouldReset = _uiState.value.shouldResetAfterError

        android.util.Log.d(TAG, "Error dialog dismissed. Should reset: $shouldReset")

        // Clear the error dialog state
        _uiState.value = _uiState.value.copy(
            showErrorDialog = false,
            errorMessage = null
        )

        if (shouldReset) {
            // Reset and navigate to home
            viewModelScope.launch {
                roundSyncCoordinator.withRoundSync {
                    resetStaleDataUseCase()
                    holeStatePreferences.clearAllHoleStates()
                }
                navController.navigate("homescreen") {
                    popUpTo("homescreen") { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    fun submitRound() {
        viewModelScope.launch {
            if (_uiState.value.round == null) {
                android.util.Log.w(TAG, "Cannot submit - no round loaded")
                _uiState.value = _uiState.value.copy(
                    errorMessage = "No round to submit"
                )
                return@launch
            }

            try {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = true,
                    errorMessage = null
                )

                // These jobs acquire RoundSyncCoordinator themselves, so drain them before the
                // finalization critical section (the coordinator's Mutex is non-reentrant).
                awaitStatsSaveJobs()

                val selectedClub = getMslClubAndTenantIdsUseCase()
                val clubId = selectedClub?.clubId?.toString()
                
                if (clubId == null) {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = "No club selected. Please select a club first."
                    )
                    return@launch
                }

                // Finalization is atomic relative to every score/stats/pickup/bulk writer. Re-read
                // Room only after acquiring, PATCH all authoritative stats directly, and retain
                // the gate through the terminal score-submission response.
                roundSyncCoordinator.withRoundSync {
                    val roundId = _uiState.value.round?.id ?: error("No round to submit")
                    var currentRound = roundRepository.getRoundById(roundId)
                        ?: error("Round $roundId is no longer available")

                    // A prior Review save may have failed locally. Retry its latest in-memory
                    // edit now, while the finalization gate is held, and make that persisted row
                    // the authoritative source for the direct PATCH pass below.
                    if (pendingStatsByHole.isNotEmpty()) {
                        currentRound = currentRound.withPendingReviewStats(pendingStatsByHole).copy(
                            isSynced = false,
                            lastUpdated = System.currentTimeMillis()
                        )
                        roundRepository.saveRound(currentRound)
                        statsSaveFailureByHole.clear()
                    }
                    syncAuthoritativeStatsBeforeSubmit(currentRound)
                    _uiState.value = _uiState.value.copy(round = currentRound)

                    val scoresContainer = createScoresContainer(currentRound)
                    submitRoundUseCase(clubId, scoresContainer).collect { result ->
                        when (result) {
                        is Resource.Success -> {
                            val response = result.data

                            android.util.Log.d(TAG, "=== PROCESSING SCORE SUBMISSION RESPONSE ===")
                            android.util.Log.d(TAG, "scoreSavedInSimpleGolf: ${response?.scoreSavedInSimpleGolf}")
                            android.util.Log.d(TAG, "errorMessage: ${response?.errorMessage}")

                            // Apply the 4 rules based on response
                            when {
                                // Rule 1: scoreSavedInSimpleGolf = true && errorMessage empty → Navigate to home
                                response?.scoreSavedInSimpleGolf == true && response.errorMessage.isNullOrEmpty() -> {
                                    android.util.Log.d(TAG, "Rule 1: Success - Navigate to home")

                                    val updatedRound = currentRound.copy(
                                        isSubmitted = true,
                                        submittedTime = org.threeten.bp.LocalDateTime.now(),
                                        lastUpdated = System.currentTimeMillis()
                                    )
                                    roundRepository.saveRound(updatedRound)

                                    // Update MongoDB round submission status
                                    android.util.Log.d(TAG, "Updating MongoDB round submission status to true")
                                    when (val mongoResult = sogoMongoRepository.updateRoundSubmissionStatus(currentRound.id, true)) {
                                        is com.sogo.golf.msl.domain.model.NetworkResult.Success -> {
                                            android.util.Log.d(TAG, "✅ Successfully updated MongoDB round submission status")
                                        }
                                        is com.sogo.golf.msl.domain.model.NetworkResult.Error -> {
                                            android.util.Log.w(TAG, "⚠️ Failed to update MongoDB round submission status: ${mongoResult.error.toUserMessage()}")
                                        }
                                        is com.sogo.golf.msl.domain.model.NetworkResult.Loading -> {}
                                    }

                                    _roundSubmitState.value = _roundSubmitState.value.copy(
                                        isSending = false,
                                        isSuccess = true,
                                        error = null
                                    )

                                    _uiState.value = _uiState.value.copy(
                                        isSubmitting = false,
                                        isSubmitted = true,
                                        successMessage = "Round submitted successfully!",
                                        scoreSavedInSimpleGolf = true
                                    )

                                    trackRoundSubmitted(currentRound)
                                }

                                // Rule 2: scoreSavedInSimpleGolf = true && errorMessage NOT empty → Show error, then reset & navigate to home
                                response?.scoreSavedInSimpleGolf == true && !response.errorMessage.isNullOrEmpty() -> {
                                    android.util.Log.d(TAG, "Rule 2: Saved with warning - Show error, reset & navigate home")

                                    val updatedRound = currentRound.copy(
                                        isSubmitted = true,
                                        submittedTime = org.threeten.bp.LocalDateTime.now(),
                                        lastUpdated = System.currentTimeMillis()
                                    )
                                    roundRepository.saveRound(updatedRound)

                                    // Update MongoDB round submission status
                                    when (val mongoResult = sogoMongoRepository.updateRoundSubmissionStatus(currentRound.id, true)) {
                                        is com.sogo.golf.msl.domain.model.NetworkResult.Success -> {
                                            android.util.Log.d(TAG, "✅ Successfully updated MongoDB round submission status")
                                        }
                                        is com.sogo.golf.msl.domain.model.NetworkResult.Error -> {
                                            android.util.Log.w(TAG, "⚠️ Failed to update MongoDB round submission status: ${mongoResult.error.toUserMessage()}")
                                        }
                                        is com.sogo.golf.msl.domain.model.NetworkResult.Loading -> {}
                                    }

                                    _uiState.value = _uiState.value.copy(
                                        isSubmitting = false,
                                        showErrorDialog = true,
                                        shouldResetAfterError = true,
                                        errorMessage = response.errorMessage,
                                        scoreSavedInSimpleGolf = true
                                    )

                                    trackRoundSubmitted(currentRound)
                                }

                                // Rule 3: scoreSavedInSimpleGolf = false && errorMessage empty → Show error, reset & navigate to home
                                response?.scoreSavedInSimpleGolf == false && response.errorMessage.isNullOrEmpty() -> {
                                    android.util.Log.d(TAG, "Rule 3: Not saved (no error message) - Show generic error, reset & navigate home")

                                    _uiState.value = _uiState.value.copy(
                                        isSubmitting = false,
                                        showErrorDialog = true,
                                        shouldResetAfterError = true,
                                        errorMessage = "Score submission failed. Please try again.",
                                        scoreSavedInSimpleGolf = false
                                    )
                                }

                                // Rule 4: scoreSavedInSimpleGolf = false && errorMessage NOT empty → Show error, DO NOT reset
                                response?.scoreSavedInSimpleGolf == false && !response.errorMessage.isNullOrEmpty() -> {
                                    android.util.Log.d(TAG, "Rule 4: Not saved with error - Show error, stay on screen")

                                    _roundSubmitState.value = _roundSubmitState.value.copy(
                                        isSending = false,
                                        isSuccess = false,
                                        error = response.errorMessage
                                    )

                                    _uiState.value = _uiState.value.copy(
                                        isSubmitting = false,
                                        showErrorDialog = true,
                                        shouldResetAfterError = false,
                                        errorMessage = response.errorMessage,
                                        scoreSavedInSimpleGolf = false
                                    )
                                }

                                // Fallback case
                                else -> {
                                    android.util.Log.e(TAG, "Unexpected response state")
                                    _uiState.value = _uiState.value.copy(
                                        isSubmitting = false,
                                        showErrorDialog = true,
                                        shouldResetAfterError = false,
                                        errorMessage = "Unexpected error occurred. Please try again."
                                    )
                                }
                            }
                        }
                        is Resource.Error -> {
                            _roundSubmitState.value = _roundSubmitState.value.copy(
                                isSending = false,
                                isSuccess = false,
                                error = result.message
                            )
                            
                            _uiState.value = _uiState.value.copy(
                                isSubmitting = false,
                                errorMessage = result.message ?: "Error submitting round"
                            )
                            
                            android.util.Log.e(TAG, "Error submitting round: ${result.message}")
                        }
                        is Resource.Loading -> {
                            _roundSubmitState.value = _roundSubmitState.value.copy(
                                isSending = true,
                                isSuccess = false,
                                error = null
                            )
                        }
                        }
                    }
                }
                
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error submitting round", e)
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    showErrorDialog = true,
                    shouldResetAfterError = false,
                    errorMessage = e.message ?: "Could not sync round stats. Please try again."
                )
                _roundSubmitState.value = _roundSubmitState.value.copy(
                    isSending = false,
                    isSuccess = false,
                    error = e.message
                )
            }
        }
    }

    /** Optimistic UI update followed by a serialized, local-first targeted stats save. */
    fun saveHoleStats(holeNumber: Int, stats: HoleStats) {
        val visibleRound = _uiState.value.round ?: return
        val nextVersion = (statsEditVersionByHole[holeNumber] ?: 0L) + 1L
        statsEditVersionByHole[holeNumber] = nextVersion
        pendingStatsByHole[holeNumber] = stats
        statsSaveFailureByHole.remove(holeNumber)
        activeStatsSaveCount += 1

        _uiState.value = _uiState.value.copy(
            round = visibleRound.copy(
                holeScores = visibleRound.holeScores.map { hole ->
                    if (hole.holeNumber == holeNumber) hole.copy(stats = stats) else hole
                },
                isSynced = false,
                lastUpdated = System.currentTimeMillis()
            ),
            isSavingStats = true
        )

        latestStatsSaveJob = viewModelScope.launch {
            try {
                val result = statsSaveMutex.withLock {
                    val latestRound = roundRepository.getRoundById(visibleRound.id)
                        ?: _uiState.value.round
                        ?: error("Round ${visibleRound.id} is no longer available")
                    updateHoleStatsUseCase(latestRound, holeNumber, stats)
                }

                if (statsEditVersionByHole[holeNumber] == nextVersion) {
                    statsSaveFailureByHole.remove(holeNumber)
                    if (result.remoteSync == HoleStatsRemoteSync.Synced &&
                        pendingStatsByHole[holeNumber] == stats
                    ) {
                        pendingStatsByHole.remove(holeNumber)
                    }
                }
                android.util.Log.d(TAG, "Hole $holeNumber stats persisted (${result.remoteSync})")
            } catch (e: Exception) {
                if (statsEditVersionByHole[holeNumber] == nextVersion) {
                    statsSaveFailureByHole[holeNumber] = e
                }
                android.util.Log.e(TAG, "Could not persist hole $holeNumber stats", e)
                _uiState.value = _uiState.value.copy(
                    showErrorDialog = true,
                    shouldResetAfterError = false,
                    errorMessage = "Could not save Hole $holeNumber stats. ${e.message ?: "Please try again."}"
                )
            } finally {
                activeStatsSaveCount = (activeStatsSaveCount - 1).coerceAtLeast(0)
                _uiState.value = _uiState.value.copy(isSavingStats = activeStatsSaveCount > 0)
            }
        }
    }

    /** Wait outside [RoundSyncCoordinator] because each in-flight save acquires that same gate. */
    private suspend fun awaitStatsSaveJobs() {
        while (true) {
            val job = latestStatsSaveJob ?: break
            job.join()
            if (job === latestStatsSaveJob) break
        }

    }

    /**
     * Called only while [RoundSyncCoordinator] is already held. This deliberately bypasses the
     * gated UpdateHoleStatsUseCase (the mutex is non-reentrant) and proves every authoritative,
     * non-null stats object is on Mongo before score submission proceeds.
     */
    private suspend fun syncAuthoritativeStatsBeforeSubmit(round: Round) {
        round.holeScores.sortedBy { it.holeNumber }.forEach { hole ->
            val stats = hole.stats ?: return@forEach
            when (val result = sogoMongoRepository.updateHoleStats(round.id, hole.holeNumber, stats)) {
                is NetworkResult.Success -> Unit
                is NetworkResult.Error -> throw RequiredHoleStatsSyncException(result.error.toUserMessage())
                is NetworkResult.Loading -> throw RequiredHoleStatsSyncException(
                    "Stats sync did not complete. Please try again."
                )
            }
        }

        pendingStatsByHole.clear()
    }

    private fun createScoresContainer(round: Round): ScoresContainer {
        val playerScores = mutableListOf<ScoresPayload>()

        val golferSignature = _playerSignatures.value[round.golferId ?: ""] ?: ""
        val golferGLNumber = round.golferGLNumber ?: ""
        
        round.playingPartnerRound?.let { partnerRound ->
            val partnerSignature = _playerSignatures.value[partnerRound.golferId ?: ""] ?: ""
            val partnerGLNumber = partnerRound.golflinkNo ?: ""
            
            if (golferGLNumber.isNotEmpty()) {
                val golferHoles = round.holeScores.map { holeScore ->
                    HolePayload(
                        grossScore = holeScore.strokes,
                        ballPickedUp = holeScore.isBallPickedUp ?: false,
                        notPlayed = holeScore.isHoleNotPlayed ?: false
                    )
                }
                
                playerScores.add(
                    ScoresPayload(
                        golfLinkNumber = golferGLNumber,
                        signature = partnerSignature,
                        holes = golferHoles
                    )
                )
            }
            
            if (partnerGLNumber.isNotEmpty()) {
                val partnerHoles = partnerRound.holeScores.map { holeScore ->
                    HolePayload(
                        grossScore = holeScore.strokes,
                        ballPickedUp = holeScore.isBallPickedUp ?: false,
                        notPlayed = holeScore.isHoleNotPlayed ?: false
                    )
                }
                
                playerScores.add(
                    ScoresPayload(
                        golfLinkNumber = partnerGLNumber,
                        signature = golferSignature,
                        holes = partnerHoles
                    )
                )
            }
        } ?: run {
            if (golferGLNumber.isNotEmpty()) {
                val golferHoles = round.holeScores.map { holeScore ->
                    HolePayload(
                        grossScore = holeScore.strokes,
                        ballPickedUp = holeScore.isBallPickedUp ?: false,
                        notPlayed = holeScore.isHoleNotPlayed ?: false
                    )
                }
                
                playerScores.add(
                    ScoresPayload(
                        golfLinkNumber = golferGLNumber,
                        signature = golferSignature,
                        holes = golferHoles
                    )
                )
            }
        }

        return ScoresContainer(playerScores = playerScores)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    fun resetSubmissionState() {
        _uiState.value = _uiState.value.copy(
            isSubmitted = false,
            isSubmitting = false,
            successMessage = null
        )
        _roundSubmitState.value = RoundSubmitState()
    }
    
    fun navigateToHomeAfterSuccess() {
        // Deprecated - navigation now handled directly in the UI layer for better reliability
        android.util.Log.d(TAG, "navigateToHomeAfterSuccess called - navigation should be handled in UI")
    }
    
    fun clearNavigationState() {
        // Clear any navigation-related state to prevent memory leaks
        android.util.Log.d(TAG, "Clearing navigation state")
        _uiState.value = _uiState.value.copy(
            isSubmitted = false,
            isSubmitting = false
        )
        _playerSignatures.value = emptyMap()
        _roundSubmitState.value = RoundSubmitState()
    }

    private fun trackSignatureCaptured(playerId: String) {
        val currentRound = _uiState.value.round ?: return
        val eventProperties = mutableMapOf<String, Any>()
        
        // Determine if this is main golfer or playing partner based on playerId
        val isMainGolfer = playerId == currentRound.golferId
        val isPlayingPartner = playerId == currentRound.playingPartnerRound?.golferId
        
        when {
            isMainGolfer -> {
                // Main golfer signature
                currentRound.golferFirstName?.let { firstName ->
                    currentRound.golferLastName?.let { lastName ->
                        eventProperties["golfer_name"] = "$firstName $lastName".trim()
                    }
                }
                currentRound.golferGLNumber?.let { glNumber ->
                    eventProperties["golfer_gl_number"] = glNumber
                }
                eventProperties["golfer_type"] = "user"
            }
            isPlayingPartner -> {
                // Playing partner signature
                currentRound.playingPartnerRound?.golferFirstName?.let { firstName ->
                    currentRound.playingPartnerRound?.golferLastName?.let { lastName ->
                        eventProperties["golfer_name"] = "$firstName $lastName".trim()
                    }
                }
                currentRound.playingPartnerRound?.golferGLNumber?.let { glNumber ->
                    eventProperties["golfer_gl_number"] = glNumber
                }
                eventProperties["golfer_type"] = "partner"
            }
        }
        
        analyticsManager.trackEvent(AnalyticsManager.EVENT_SIGNATURE_CAPTURED, eventProperties)
    }

    private fun trackRoundSubmitted(round: Round) {
        val eventProperties = mutableMapOf<String, Any>()
        
        // Add compType and mongo ID
        round.compType?.let { compType ->
            eventProperties["comp_type"] = compType
        }
        eventProperties["mongo_id"] = round.id
        
        // Add playing partner information if available
        round.playingPartnerRound?.let { partnerRound ->
            partnerRound.golferFirstName?.let { firstName ->
                partnerRound.golferLastName?.let { lastName ->
                    eventProperties["playing_partner_name"] = "$firstName $lastName".trim()
                }
            }
        }
        
        // Add MSL API payload split by player
        try {
            val scoresContainer = createScoresContainer(round)
            val sanitizedPayload = createSanitizedPayload(scoresContainer)
            
            @Suppress("UNCHECKED_CAST")
            val playerScores = sanitizedPayload["playerScores"] as List<Map<String, Any>>
            
            // Split payload by player to avoid Amplitude's 1024 character limit
            if (playerScores.isNotEmpty()) {
                val golferPayload = Gson().toJson(playerScores[0])
                eventProperties["msl_golfer_payload"] = golferPayload
                
                if (playerScores.size > 1) {
                    val partnerPayload = Gson().toJson(playerScores[1])
                    eventProperties["msl_partner_payload"] = partnerPayload
                }
            }
        } catch (e: Exception) {
            android.util.Log.w(TAG, "Failed to serialize MSL payload for analytics", e)
        }
        
        analyticsManager.trackEvent(AnalyticsManager.EVENT_ROUND_SUBMITTED, eventProperties)
    }

    private fun createSanitizedPayload(scoresContainer: ScoresContainer): Map<String, Any> {
        return mapOf(
            "playerScores" to scoresContainer.playerScores.map { scorePayload ->
                mapOf(
                    "gl" to scorePayload.golfLinkNumber,
                    "hasSignature" to scorePayload.signature.isNotEmpty(),
                    "holes" to scorePayload.holes.map { hole ->
                        mapOf(
                            "strokes" to hole.grossScore,
                            "bpu" to hole.ballPickedUp,
                            "np" to hole.notPlayed
                        )
                    }
                )
            }
        )
    }


    suspend fun performPostSubmissionCleanup() {
        try {
            roundSyncCoordinator.withRoundSync {
                val currentRound = _uiState.value.round
                if (currentRound == null) {
                    android.util.Log.w(TAG, "No round available for cleanup")
                    return@withRoundSync
                }
                
                android.util.Log.d(TAG, "Starting post-submission cleanup after user dismissed dialog")
                
                // Clear hole state and reset stale data
                holeStatePreferences.clearCurrentHole(currentRound.id)

                // Pace snapshots are per-round and done with (Round Time is
                // already in uiState) — clear them so they don't accumulate
                // one prefs entry per submitted round.
                holeStatePreferences.clearPaceSnapshots(currentRound.id)

                resetStaleDataUseCase()
                
                android.util.Log.d(TAG, "Post-submission cleanup completed")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error during post-submission cleanup", e)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(navController: NavController): ReviewScoresViewModel
    }
}

data class ReviewScoresUiState(
    val round: Round? = null,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val isSavingStats: Boolean = false,
    val isSubmitted: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showErrorDialog: Boolean = false,
    val shouldResetAfterError: Boolean = false,
    val scoreSavedInSimpleGolf: Boolean = false,
    /** "Round Time" for the submit-success dialog: total time since the round's
     * (booked) start as of the final hole's score entry, human-friendly
     * ("4 hr 53 min" / "7 min 53 sec"). null hides the line (never captured —
     * e.g. legacy round). */
    val roundTimeText: String? = null
)

data class RoundSubmitState(
    val isSending: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

/** Merge only pending stats into the newest Room row; never restore a whole stale UI snapshot. */
internal fun Round.withPendingReviewStats(pendingStatsByHole: Map<Int, HoleStats>): Round {
    if (pendingStatsByHole.isEmpty()) return this

    return copy(
        holeScores = holeScores.map { hole ->
            pendingStatsByHole[hole.holeNumber]?.let { hole.copy(stats = it) } ?: hole
        },
        isSynced = false
    )
}

internal fun canPublishReviewRoundLoad(
    requestGeneration: Long,
    latestGeneration: Long,
    requestedRoundId: String,
    activeRoundId: String?
): Boolean = requestGeneration == latestGeneration && requestedRoundId == activeRoundId
