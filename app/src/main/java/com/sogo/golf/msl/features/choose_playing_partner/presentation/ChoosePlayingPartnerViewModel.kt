// app/src/main/java/com/sogo/golf/msl/features/choose_playing_partner/presentation/ChoosePlayingPartnerViewModel.kt
package com.sogo.golf.msl.features.choose_playing_partner.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.msl.MslPlayingPartner
import com.sogo.golf.msl.domain.usecase.club.GetMslClubAndTenantIdsUseCase
import com.sogo.golf.msl.domain.usecase.game.FetchAndSaveGameUseCase
import com.sogo.golf.msl.domain.usecase.game.GetLocalGameUseCase
import com.sogo.golf.msl.domain.usecase.marker.RemoveMarkerUseCase
import com.sogo.golf.msl.domain.usecase.marker.SelectMarkerUseCase
import com.sogo.golf.msl.domain.usecase.msl_golfer.GetMslGolferUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MarkerUiState(
    val isSelectingMarker: Boolean = false,
    val isRemovingMarker: Boolean = false,
    val markerSuccessMessage: String? = null,
    val markerErrorMessage: String? = null
)

@HiltViewModel
class ChoosePlayingPartnerViewModel @Inject constructor(
    private val getLocalGameUseCase: GetLocalGameUseCase,
    private val getMslGolferUseCase: GetMslGolferUseCase,
    private val selectMarkerUseCase: SelectMarkerUseCase,
    private val removeMarkerUseCase: RemoveMarkerUseCase,
    private val fetchAndSaveGameUseCase: FetchAndSaveGameUseCase,
    private val getMslClubAndTenantIdsUseCase: GetMslClubAndTenantIdsUseCase
) : ViewModel() {

    // State for selected playing partner
    private val _selectedPartner = MutableStateFlow<MslPlayingPartner?>(null)
    val selectedPartner: StateFlow<MslPlayingPartner?> = _selectedPartner.asStateFlow()

    // State for marker API calls
    private val _markerUiState = MutableStateFlow(MarkerUiState())
    val markerUiState: StateFlow<MarkerUiState> = _markerUiState.asStateFlow()

    // NEW: Navigation event (only emitted on successful API calls)
    private val _navigationEvent = MutableSharedFlow<Unit>()
    val navigationEvent: SharedFlow<Unit> = _navigationEvent

    // Get the local game data (contains playing partners)
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

    // Method to select a playing partner (only one can be selected)
    fun selectPartner(partner: MslPlayingPartner) {
        _selectedPartner.value = if (_selectedPartner.value == partner) {
            null // Deselect if same partner is tapped again
        } else {
            partner // Select the new partner
        }
    }

    // Method to check if a partner is selected
    fun isPartnerSelected(partner: MslPlayingPartner): Boolean {
        return _selectedPartner.value == partner
    }

    // Method to clear selection
    fun clearSelection() {
        _selectedPartner.value = null
    }

    // NEW: Method to check if any partner is marked by current user
    fun hasPartnerMarkedByMe(): Boolean {
        val currentUser = currentGolfer.value
        val game = localGame.value

        return currentUser != null && game != null &&
                game.playingPartners.any { partner ->
                    partner.markedByGolfLinkNumber == currentUser.golfLinkNo
                }
    }

    // NEW: Method to get the partner marked by current user
    private fun getPartnerMarkedByMe(): MslPlayingPartner? {
        val currentUser = currentGolfer.value
        val game = localGame.value

        return if (currentUser != null && game != null) {
            game.playingPartners.find { partner ->
                partner.markedByGolfLinkNumber == currentUser.golfLinkNo
            }
        } else {
            null
        }
    }

    // NEW: Method to remove marker
    fun removeMarker() {
        val partnerMarkedByMe = getPartnerMarkedByMe()
        if (partnerMarkedByMe?.golfLinkNumber == null) {
            _markerUiState.value = _markerUiState.value.copy(
                markerErrorMessage = "No partner is currently marked by you"
            )
            return
        }

        viewModelScope.launch {
            _markerUiState.value = _markerUiState.value.copy(
                isRemovingMarker = true,
                markerErrorMessage = null,
                markerSuccessMessage = null
            )

            android.util.Log.d("ChoosePartnerVM", "=== REMOVING MARKER ===")
            android.util.Log.d("ChoosePartnerVM", "Golf Link Number: ${partnerMarkedByMe.golfLinkNumber}")

            when (val result = removeMarkerUseCase(partnerMarkedByMe.golfLinkNumber!!)) {
                is NetworkResult.Success -> {
                    android.util.Log.d("ChoosePartnerVM", "✅ SUCCESS: Marker removed successfully")

                    val partnerName = when {
                        partnerMarkedByMe.firstName != null && partnerMarkedByMe.lastName != null ->
                            "${partnerMarkedByMe.firstName} ${partnerMarkedByMe.lastName}"
                        partnerMarkedByMe.firstName != null -> partnerMarkedByMe.firstName!!
                        partnerMarkedByMe.lastName != null -> partnerMarkedByMe.lastName!!
                        else -> "Partner"
                    }

                    // Clear selection when marker is removed
                    _selectedPartner.value = null

                    _markerUiState.value = _markerUiState.value.copy(
                        isRemovingMarker = false,
                        markerSuccessMessage = "✅ Marker removed from $partnerName successfully!"
                    )

                    // NEW: Refresh game data to update marker assignments
                    refreshGameData()
                }
                is NetworkResult.Error -> {
                    android.util.Log.e("ChoosePartnerVM", "❌ ERROR: Failed to remove marker: ${result.error}")
                    _markerUiState.value = _markerUiState.value.copy(
                        isRemovingMarker = false,
                        markerErrorMessage = "Failed to remove marker: ${result.error.toUserMessage()}"
                    )
                }
                is NetworkResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    // NEW: Method to select marker (call API)
    fun selectMarker() {
        val selectedPartner = _selectedPartner.value
        if (selectedPartner?.golfLinkNumber == null) {
            _markerUiState.value = _markerUiState.value.copy(
                markerErrorMessage = "No playing partner selected or partner has no Golf Link Number"
            )
            return
        }

        viewModelScope.launch {
            _markerUiState.value = _markerUiState.value.copy(
                isSelectingMarker = true,
                markerErrorMessage = null,
                markerSuccessMessage = null
            )

            android.util.Log.d("ChoosePartnerVM", "=== SELECTING MARKER ===")
            android.util.Log.d("ChoosePartnerVM", "Golf Link Number: ${selectedPartner.golfLinkNumber}")

            when (val result = selectMarkerUseCase(selectedPartner.golfLinkNumber!!)) {
                is NetworkResult.Success -> {
                    android.util.Log.d("ChoosePartnerVM", "✅ SUCCESS: Marker selected successfully")

                    val partnerName = when {
                        selectedPartner.firstName != null && selectedPartner.lastName != null ->
                            "${selectedPartner.firstName} ${selectedPartner.lastName}"
                        selectedPartner.firstName != null -> selectedPartner.firstName!!
                        selectedPartner.lastName != null -> selectedPartner.lastName!!
                        else -> "Selected Partner"
                    }

                    _markerUiState.value = _markerUiState.value.copy(
                        isSelectingMarker = false,
                        markerSuccessMessage = "✅ $partnerName selected as marker successfully!"
                    )

                    // NEW: Only emit navigation event on SUCCESS
                    _navigationEvent.emit(Unit)

                    // NEW: Refresh game data to update marker assignments
                    refreshGameData()
                }
                is NetworkResult.Error -> {
                    android.util.Log.e("ChoosePartnerVM", "❌ ERROR: Failed to select marker: ${result.error}")
                    _markerUiState.value = _markerUiState.value.copy(
                        isSelectingMarker = false,
                        markerErrorMessage = "Failed to select marker: ${result.error.toUserMessage()}"
                    )
                }
                is NetworkResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    // Method to clear marker messages
    fun clearMarkerMessages() {
        _markerUiState.value = _markerUiState.value.copy(
            markerErrorMessage = null,
            markerSuccessMessage = null
        )
    }

    // NEW: Method to proceed without marker selection
    fun proceedWithoutMarker() {
        viewModelScope.launch {
            android.util.Log.d("ChoosePartnerVM", "Proceeding without marker selection")
            _navigationEvent.emit(Unit)
        }
    }

    // NEW: Method to refresh game data after marker operations
    private fun refreshGameData() {
        viewModelScope.launch {
            try {
                // Get the current club ID to refresh game data
                val selectedClub = getMslClubAndTenantIdsUseCase()
                if (selectedClub?.clubId != null) {
                    android.util.Log.d("ChoosePartnerVM", "Refreshing game data after marker operation...")

                    when (val result = fetchAndSaveGameUseCase(selectedClub.clubId.toString())) {
                        is NetworkResult.Success -> {
                            android.util.Log.d("ChoosePartnerVM", "✅ Game data refreshed successfully")
                            // The local game StateFlow will automatically update with fresh data
                        }
                        is NetworkResult.Error -> {
                            android.util.Log.w("ChoosePartnerVM", "⚠️ Failed to refresh game data: ${result.error}")
                            // Don't show error to user - marker operation was successful
                        }
                        is NetworkResult.Loading -> {
                            // Ignore
                        }
                    }
                } else {
                    android.util.Log.w("ChoosePartnerVM", "⚠️ No club selected, cannot refresh game data")
                }
            } catch (e: Exception) {
                android.util.Log.w("ChoosePartnerVM", "⚠️ Exception while refreshing game data", e)
                // Don't show error to user - marker operation was successful
            }
        }
    }
}