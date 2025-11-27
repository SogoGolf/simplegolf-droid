package com.sogo.golf.msl.features.review_scores.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.gson.Gson
import com.sogo.golf.msl.common.Resource
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.model.msl.v2.HolePayload
import com.sogo.golf.msl.domain.model.msl.v2.ScoresContainer
import com.sogo.golf.msl.domain.model.msl.v2.ScoresPayload
import com.sogo.golf.msl.domain.repository.RoundLocalDbRepository
import com.sogo.golf.msl.domain.repository.MslCompetitionLocalDbRepository
import com.sogo.golf.msl.domain.usecase.round.GetRoundUseCase
import com.sogo.golf.msl.domain.usecase.round.SubmitRoundUseCase
import com.sogo.golf.msl.domain.usecase.date.ResetStaleDataUseCase
import com.sogo.golf.msl.data.local.preferences.HoleStatePreferences
import com.sogo.golf.msl.domain.usecase.club.GetMslClubAndTenantIdsUseCase
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ReviewScoresViewModel.Factory::class)
class ReviewScoresViewModel @AssistedInject constructor(
    private val getRoundUseCase: GetRoundUseCase,
    private val roundRepository: RoundLocalDbRepository,
    private val competitionRepository: MslCompetitionLocalDbRepository,
    private val submitRoundUseCase: SubmitRoundUseCase,
    private val getMslClubAndTenantIdsUseCase: GetMslClubAndTenantIdsUseCase,
    private val resetStaleDataUseCase: ResetStaleDataUseCase,
    private val holeStatePreferences: HoleStatePreferences,
    private val sogoMongoRepository: SogoMongoRepository,
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
        _currentRoundId.value = roundId
        
        viewModelScope.launch {
            try {
                val round = getRoundUseCase.getRoundById(roundId)
                if (round != null) {
                    _uiState.value = _uiState.value.copy(
                        round = round,
                        isLoading = false,
                        errorMessage = null
                    )
                    android.util.Log.d(TAG, "Round loaded successfully: ${round.golferFirstName} ${round.golferLastName}")
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Round not found"
                    )
                    android.util.Log.w(TAG, "Round not found: $roundId")
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error loading round", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading round: ${e.message}"
                )
            }
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

        if (shouldReset) {
            // Reset and navigate to home
            viewModelScope.launch {
                resetStaleDataUseCase()
                holeStatePreferences.clearAllHoleStates()
                navController.navigate("homescreen") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        // Clear the error dialog state
        _uiState.value = _uiState.value.copy(
            showErrorDialog = false,
            errorMessage = null
        )
    }

    fun submitRound() {
        viewModelScope.launch {
            val currentRound = _uiState.value.round
            if (currentRound == null) {
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

                val scoresContainer = createScoresContainer(currentRound)
                
                val selectedClub = getMslClubAndTenantIdsUseCase()
                val clubId = selectedClub?.clubId?.toString()
                
                if (clubId == null) {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = "No club selected. Please select a club first."
                    )
                    return@launch
                }

                submitRoundUseCase(clubId, scoresContainer).onEach { result ->
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
                }.launchIn(viewModelScope)
                
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error submitting round", e)
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = "Error submitting round: ${e.message}"
                )
                _roundSubmitState.value = _roundSubmitState.value.copy(
                    isSending = false,
                    isSuccess = false,
                    error = e.message
                )
            }
        }
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


    fun performPostSubmissionCleanup() {
        viewModelScope.launch {
            try {
                val currentRound = _uiState.value.round
                if (currentRound == null) {
                    android.util.Log.w(TAG, "No round available for cleanup")
                    return@launch
                }
                
                android.util.Log.d(TAG, "Starting post-submission cleanup after user dismissed dialog")
                
                // Clear hole state and reset stale data
                holeStatePreferences.clearCurrentHole(currentRound.id)
                
                resetStaleDataUseCase()
                
                android.util.Log.d(TAG, "Post-submission cleanup completed")
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error during post-submission cleanup", e)
            }
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
    val isSubmitted: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showErrorDialog: Boolean = false,
    val shouldResetAfterError: Boolean = false,
    val scoreSavedInSimpleGolf: Boolean = false
)

data class RoundSubmitState(
    val isSending: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
