package com.sogo.golf.msl.features.review_scores.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.sogo.golf.msl.common.Resource
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.model.msl.v2.HolePayload
import com.sogo.golf.msl.domain.model.msl.v2.ScoresContainer
import com.sogo.golf.msl.domain.model.msl.v2.ScoresPayload
import com.sogo.golf.msl.domain.repository.RoundLocalDbRepository
import com.sogo.golf.msl.domain.usecase.round.GetRoundUseCase
import com.sogo.golf.msl.domain.usecase.round.SubmitRoundUseCase
import com.sogo.golf.msl.domain.usecase.date.ResetStaleDataUseCase
import com.sogo.golf.msl.data.local.preferences.HoleStatePreferences
import com.sogo.golf.msl.domain.usecase.club.GetMslClubAndTenantIdsUseCase
import com.sogo.golf.msl.domain.repository.remote.SogoMongoRepository
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
    private val submitRoundUseCase: SubmitRoundUseCase,
    private val getMslClubAndTenantIdsUseCase: GetMslClubAndTenantIdsUseCase,
    private val resetStaleDataUseCase: ResetStaleDataUseCase,
    private val holeStatePreferences: HoleStatePreferences,
    private val sogoMongoRepository: SogoMongoRepository,
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
                            
                            _uiState.value = _uiState.value.copy(
                                isSubmitting = false,
                                isSubmitted = true,
                                successMessage = "Round submitted successfully!"
                            )
                            
                            android.util.Log.d(TAG, "Round submitted successfully: ${currentRound.id}")
                            
                            // Perform immediate cleanup operations (but not navigation)
                            viewModelScope.launch {
                                try {
                                    android.util.Log.d(TAG, "Starting post-submission cleanup")
                                    
                                    // Update MongoDB round with isSubmitted = true using minimal payload
                                    android.util.Log.d(TAG, "Updating MongoDB round submission status to true")
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
                                    
                                    holeStatePreferences.clearCurrentHole(currentRound.id)
                                    
                                    resetStaleDataUseCase()
                                    
                                    android.util.Log.d(TAG, "Post-submission cleanup completed - waiting for user to dismiss dialog")
                                } catch (e: Exception) {
                                    android.util.Log.e(TAG, "Error during post-submission cleanup", e)
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
        android.util.Log.d(TAG, "Navigating to home screen after user dismissed success dialog")
        try {
            navController.navigate("homescreen?skipDataFetch=true") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Navigation failed: ${e.message}", e)
            // Try alternative navigation approach
            try {
                navController.popBackStack("homescreen", inclusive = false)
            } catch (fallbackError: Exception) {
                android.util.Log.e(TAG, "Fallback navigation also failed: ${fallbackError.message}", fallbackError)
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
