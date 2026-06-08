package com.sogo.golf.msl.features.play.presentation

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sogo.golf.msl.analytics.AnalyticsManager
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
    private val shareScorecardUseCase: ShareScorecardUseCase,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(ScorecardSharingState())
    val state: StateFlow<ScorecardSharingState> = _state.asStateFlow()
    
    fun shareScorecard(
        context: Context,
        round: Round,
        mslCompetition: MslCompetition?,
        isNineHoles: Boolean,
        hideTeeInfo: Boolean = false
    ) {
        android.util.Log.d("ScorecardSharingViewModel", "shareScorecard called")
        
        val eventProperties = mapOf(
            "golfer_name" to "${round.golferFirstName ?: ""} ${round.golferLastName ?: ""}".trim(),
            "mongo_id" to round.id,
            "club_name" to (round.clubName ?: "Unknown"),
            "number_of_holes" to round.holeScores.distinctBy { it.holeNumber }.size,
            "comp_type" to (round.compType ?: mslCompetition?.players?.firstOrNull()?.competitionType ?: "Social")
        )
        analyticsManager.trackEvent(AnalyticsManager.EVENT_SHARE_SCORECARD_TAPPED, eventProperties)
        
        viewModelScope.launch {
            try {
                android.util.Log.d("ScorecardSharingViewModel", "Starting image generation")
                _state.value = _state.value.copy(isGeneratingImage = true, error = null)
                
                val currentState = _state.value
                android.util.Log.d("ScorecardSharingViewModel", "Generating vertical scorecard bitmap")
                val bitmap = ScorecardSharingUtils.generateVerticalScorecardBitmap(
                    context = context,
                    round = round,
                    mslCompetition = mslCompetition,
                    selectedPlayer = currentState.selectedPlayer,
                    isNineHoles = isNineHoles,
                    width = 1080,
                    height = if (isNineHoles) 1200 else 1600,
                    showTeeInfo = !hideTeeInfo
                )
                
                android.util.Log.d("ScorecardSharingViewModel", "Bitmap captured, optimizing")
                val optimizedBitmap = ScorecardSharingUtils.optimizeBitmapForSharing(bitmap)
                
                android.util.Log.d("ScorecardSharingViewModel", "Creating share intent")
                val shareIntent = shareScorecardUseCase(
                    context = context,
                    scorecardBitmap = optimizedBitmap,
                    round = round,
                    mslCompetition = mslCompetition
                )
                
                val chooser = Intent.createChooser(shareIntent, "Share Scorecard")
                if (context !is android.app.Activity) {
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                android.util.Log.d("ScorecardSharingViewModel", "Starting share chooser")
                context.startActivity(chooser)
                
                analyticsManager.trackEvent(AnalyticsManager.EVENT_SHARE_SCORECARD_SENT, eventProperties)
                
                _state.value = _state.value.copy(isGeneratingImage = false)
                android.util.Log.d("ScorecardSharingViewModel", "Share completed successfully")
                
            } catch (e: Exception) {
                android.util.Log.e("ScorecardSharingViewModel", "Share failed", e)
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
