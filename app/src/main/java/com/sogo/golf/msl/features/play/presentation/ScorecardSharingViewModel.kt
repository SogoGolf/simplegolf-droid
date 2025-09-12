package com.sogo.golf.msl.features.play.presentation

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.model.msl.MslCompetition
import com.sogo.golf.msl.domain.usecase.sharing.ShareScorecardUseCase
import com.sogo.golf.msl.shared_components.ui.PlayerType
import com.sogo.golf.msl.shared_components.ui.ScorecardSharingUtils
import com.sogo.golf.msl.shared_components.ui.VerticalScorecardForSharing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScorecardSharingState(
    val isGeneratingImage: Boolean = false,
    val selectedPlayer: PlayerType = PlayerType.GOLFER,
    val error: String? = null
)

@HiltViewModel
class ScorecardSharingViewModel @Inject constructor(
    private val shareScorecardUseCase: ShareScorecardUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(ScorecardSharingState())
    val state: StateFlow<ScorecardSharingState> = _state.asStateFlow()
    
    fun shareScorecard(
        context: Context,
        round: Round,
        mslCompetition: MslCompetition?,
        isNineHoles: Boolean
    ) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isGeneratingImage = true, error = null)
                
                val currentState = _state.value
                val bitmap = ScorecardSharingUtils.captureComposableAsBitmap(
                    context = context,
                    width = 1080,
                    height = if (isNineHoles) 1200 else 1600
                ) {
                    VerticalScorecardForSharing(
                        round = round,
                        mslCompetition = mslCompetition,
                        selectedPlayer = currentState.selectedPlayer,
                        isNineHoles = isNineHoles
                    )
                }
                
                val optimizedBitmap = ScorecardSharingUtils.optimizeBitmapForSharing(bitmap)
                
                val playerName = when (currentState.selectedPlayer) {
                    PlayerType.GOLFER -> "${round.golferFirstName} ${round.golferLastName}"
                    PlayerType.PLAYING_PARTNER -> "${round.playingPartnerRound?.golferFirstName} ${round.playingPartnerRound?.golferLastName}"
                }
                
                val shareIntent = shareScorecardUseCase(
                    context = context,
                    scorecardBitmap = optimizedBitmap,
                    round = round,
                    playerName = playerName
                )
                
                context.startActivity(Intent.createChooser(shareIntent, "Share Scorecard"))
                
                _state.value = _state.value.copy(isGeneratingImage = false)
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isGeneratingImage = false,
                    error = "Failed to share scorecard: ${e.message}"
                )
            }
        }
    }
    
    fun selectPlayer(playerType: PlayerType) {
        _state.value = _state.value.copy(selectedPlayer = playerType)
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
