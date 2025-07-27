package com.sogo.golf.msl.features.play.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.usecase.club.GetMslClubAndTenantIdsUseCase
import com.sogo.golf.msl.domain.usecase.game.FetchAndSaveGameUseCase
import com.sogo.golf.msl.domain.usecase.game.GetLocalGameUseCase
import com.sogo.golf.msl.domain.usecase.marker.RemoveMarkerUseCase
import com.sogo.golf.msl.domain.usecase.msl_golfer.GetMslGolferUseCase
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
    private val getMslGolferUseCase: GetMslGolferUseCase,
    private val removeMarkerUseCase: RemoveMarkerUseCase,
    private val fetchAndSaveGameUseCase: FetchAndSaveGameUseCase,
    private val getMslClubAndTenantIdsUseCase: GetMslClubAndTenantIdsUseCase
) : ViewModel() {

    private val _deleteMarkerEnabled = MutableStateFlow(false)
    val deleteMarkerEnabled: StateFlow<Boolean> = _deleteMarkerEnabled.asStateFlow()

    private val _isRemovingMarker = MutableStateFlow(false)
    val isRemovingMarker: StateFlow<Boolean> = _isRemovingMarker.asStateFlow()

    private val _markerError = MutableStateFlow<String?>(null)
    val markerError: StateFlow<String?> = _markerError.asStateFlow()

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

    fun removeMarkerAndNavigateBack(navController: NavController) {
        android.util.Log.d("PlayRoundVM", "=== removeMarkerAndNavigateBack called ===")

        val partnerGolfLinkNumber = getPartnerMarkedByMe()
        android.util.Log.d("PlayRoundVM", "Partner golf link number: $partnerGolfLinkNumber")

        if (partnerGolfLinkNumber == null) {
            android.util.Log.d("PlayRoundVM", "No marker to remove, navigating back normally")
            // No marker to remove, just navigate back normally
            navController.popBackStack()
            return
        }

        viewModelScope.launch {
            android.util.Log.d("PlayRoundVM", "Starting marker removal process")
            _isRemovingMarker.value = true
            _markerError.value = null

            android.util.Log.d("PlayRoundVM", "=== REMOVING MARKER ON BACK NAVIGATION ===")
            android.util.Log.d("PlayRoundVM", "Golf Link Number: $partnerGolfLinkNumber")

            when (val result = removeMarkerUseCase(partnerGolfLinkNumber)) {
                is NetworkResult.Success -> {
                    android.util.Log.d("PlayRoundVM", "✅ SUCCESS: Marker removed successfully")

                    // Refresh game data to update marker assignments
                    refreshGameData()

                    _isRemovingMarker.value = false

                    android.util.Log.d("PlayRoundVM", "Navigating back to choose partner screen")
                    // Navigate back to choose partner screen
                    navController.popBackStack()
                }
                is NetworkResult.Error -> {
                    android.util.Log.e("PlayRoundVM", "❌ ERROR: Failed to remove marker: ${result.error}")
                    _isRemovingMarker.value = false
                    _markerError.value = "Failed to remove marker: ${result.error.toUserMessage()}"
                }
                is NetworkResult.Loading -> {
                    android.util.Log.d("PlayRoundVM", "Loading state received")
                    // Already handled above
                }
            }
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
}