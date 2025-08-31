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
                            _roundSubmitState.value = _roundSubmitState.value.copy(
                                isSending = false,
                                isSuccess = true,
                                error = null
                            )
                            
                            val updatedRound = currentRound.copy(
                                isSubmitted = true,
                                submittedTime = org.threeten.bp.LocalDateTime.now(),
                                lastUpdated = System.currentTimeMillis()
                            )

                            roundRepository.saveRound(updatedRound)
                            
                            // Update MongoDB round submission status immediately after MSL submission succeeds
                            android.util.Log.d(TAG, "Updating MongoDB round submission status to true after MSL success")
                            when (val mongoResult = sogoMongoRepository.updateRoundSubmissionStatus(currentRound.id, true)) {
                                is com.sogo.golf.msl.domain.model.NetworkResult.Success -> {
                                    android.util.Log.d(TAG, "✅ Successfully updated MongoDB round submission status")
                                }
                                is com.sogo.golf.msl.domain.model.NetworkResult.Error -> {
                                    android.util.Log.w(TAG, "⚠️ Failed to update MongoDB round submission status: ${mongoResult.error.toUserMessage()}")
                                }
                                is com.sogo.golf.msl.domain.model.NetworkResult.Loading -> {
                                    android.util.Log.d(TAG, "MongoDB round submission status update in progress...")
                                }
                            }
                            
                            _uiState.value = _uiState.value.copy(
                                isSubmitting = false,
                                isSubmitted = true,
                                successMessage = "Round submitted successfully!"
                            )
                            
                            android.util.Log.d(TAG, "Round submitted successfully: ${currentRound.id}")
                            
                            // Track round submission
                            trackRoundSubmitted(currentRound)
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
        
        // Add playing partner information if available
        round.playingPartnerRound?.let { partnerRound ->
            partnerRound.golferFirstName?.let { firstName ->
                partnerRound.golferLastName?.let { lastName ->
                    eventProperties["playing_partner_name"] = "$firstName $lastName".trim()
                }
            }
        }
        
        // Add MSL API payload with size validation
        try {
            val scoresContainer = createScoresContainer(round)
            val sanitizedPayload = createSanitizedPayload(scoresContainer)
            val payloadJson = Gson().toJson(sanitizedPayload)
            
            // Amplitude has a 1024 character limit for event properties
            if (payloadJson.length <= 1024) {
                eventProperties["msl_api_payload"] = payloadJson
            } else {
                // Handle large payloads by chunking or compressing
                handleLargePayload(eventProperties, sanitizedPayload, payloadJson)
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
                    "golfLinkNumber" to scorePayload.golfLinkNumber,
                    "hasSignature" to scorePayload.signature.isNotEmpty(),
                    "holes" to scorePayload.holes.map { hole ->
                        mapOf(
                            "grossScore" to hole.grossScore,
                            "ballPickedUp" to hole.ballPickedUp,
                            "notPlayed" to hole.notPlayed
                        )
                    }
                )
            }
        )
    }

    private fun handleLargePayload(eventProperties: MutableMap<String, Any>, sanitizedPayload: Map<String, Any>, fullJson: String) {
        android.util.Log.d(TAG, "MSL payload too large (${fullJson.length} chars), implementing chunking strategy")
        
        // Strategy 1: Try compressed summary first
        val summaryPayload = createCompressedSummary(sanitizedPayload)
        val summaryJson = Gson().toJson(summaryPayload)
        
        if (summaryJson.length <= 1024) {
            eventProperties["msl_api_payload_summary"] = summaryJson
            eventProperties["msl_payload_truncated"] = true
            eventProperties["msl_payload_full_size"] = fullJson.length
        } else {
            // Strategy 2: Chunk into multiple events
            chunkPayloadIntoMultipleEvents(sanitizedPayload)
            eventProperties["msl_payload_chunked"] = true
            eventProperties["msl_payload_full_size"] = fullJson.length
        }
    }

    private fun createCompressedSummary(payload: Map<String, Any>): Map<String, Any> {
        @Suppress("UNCHECKED_CAST")
        val playerScores = payload["playerScores"] as List<Map<String, Any>>
        
        return mapOf(
            "playerCount" to playerScores.size,
            "players" to playerScores.map { player ->
                @Suppress("UNCHECKED_CAST")
                val holes = player["holes"] as List<Map<String, Any>>
                mapOf(
                    "golfLinkNumber" to player["golfLinkNumber"],
                    "hasSignature" to player["hasSignature"],
                    "holeCount" to holes.size,
                    "totalStrokes" to holes.sumOf { (it["grossScore"] as Number).toInt() },
                    "pickupCount" to holes.count { it["ballPickedUp"] as Boolean },
                    "notPlayedCount" to holes.count { it["notPlayed"] as Boolean }
                )
            }
        )
    }

    private fun chunkPayloadIntoMultipleEvents(payload: Map<String, Any>) {
        @Suppress("UNCHECKED_CAST")
        val playerScores = payload["playerScores"] as List<Map<String, Any>>
        
        playerScores.forEachIndexed { playerIndex, player ->
            val playerPayload = mapOf("player" to player)
            val playerJson = Gson().toJson(playerPayload)
            
            if (playerJson.length <= 1024) {
                val chunkProperties = mapOf<String, Any>(
                    "msl_payload_chunk" to playerJson,
                    "chunk_index" to playerIndex,
                    "total_chunks" to playerScores.size
                )
                analyticsManager.trackEvent("${AnalyticsManager.EVENT_ROUND_SUBMITTED}_chunk", chunkProperties)
            } else {
                android.util.Log.w(TAG, "Individual player payload still too large: ${playerJson.length} chars")
            }
        }
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
    val successMessage: String? = null
)

data class RoundSubmitState(
    val isSending: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
