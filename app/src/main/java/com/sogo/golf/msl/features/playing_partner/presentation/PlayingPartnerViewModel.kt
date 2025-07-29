package com.sogo.golf.msl.features.playing_partner.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sogo.golf.msl.domain.repository.MslGameLocalDbRepository
import com.sogo.golf.msl.domain.usecase.fees.GetFeesUseCase
import com.sogo.golf.msl.domain.usecase.msl_golfer.GetMslGolferUseCase
import com.sogo.golf.msl.domain.usecase.sogo_golfer.GetSogoGolferUseCase
import com.sogo.golf.msl.domain.usecase.marker.SelectMarkerUseCase
import com.sogo.golf.msl.domain.usecase.game.FetchAndSaveGameUseCase
import com.sogo.golf.msl.domain.usecase.competition.FetchAndSaveCompetitionUseCase
import com.sogo.golf.msl.domain.usecase.club.GetMslClubAndTenantIdsUseCase
import com.sogo.golf.msl.domain.repository.RoundLocalDbRepository
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.model.MslMetaData
import com.sogo.golf.msl.domain.model.NetworkResult
import org.threeten.bp.LocalDateTime
import java.util.UUID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import com.sogo.golf.msl.domain.model.msl.MslPlayingPartner
import com.sogo.golf.msl.domain.repository.MslGolferLocalDbRepository
import com.sogo.golf.msl.domain.repository.remote.MslRepository
import com.sogo.golf.msl.domain.usecase.sogo_golfer.FetchAndSaveSogoGolferUseCase
import kotlinx.coroutines.launch

@HiltViewModel
class PlayingPartnerViewModel @Inject constructor(
    private val getMslGolferUseCase: GetMslGolferUseCase,
    private val gameRepository: MslGameLocalDbRepository,
    private val getSogoGolferUseCase: GetSogoGolferUseCase,
    private val getFeesUseCase: GetFeesUseCase,
    private val selectMarkerUseCase: SelectMarkerUseCase,
    private val fetchAndSaveGameUseCase: FetchAndSaveGameUseCase,
    private val fetchAndSaveCompetitionUseCase: FetchAndSaveCompetitionUseCase,
    private val getMslClubAndTenantIdsUseCase: GetMslClubAndTenantIdsUseCase,
    private val mslRepository: MslRepository,
    private val mslGolferLocalDbRepository: MslGolferLocalDbRepository,
    private val fetchAndSaveSogoGolferUseCase: FetchAndSaveSogoGolferUseCase,
    private val roundRepository: RoundLocalDbRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayingPartnerUiState())
    val uiState: StateFlow<PlayingPartnerUiState> = _uiState.asStateFlow()

    // Observe local game data from Room database
    val localGame = gameRepository.getGame()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // MSL Game State that combines loading state with actual game data
    val mslGameState = combine(localGame, _uiState) { game, uiState ->
        MslGameState(
            isLoading = uiState.isLoading,
            game = game,
            error = uiState.errorMessage ?: ""
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MslGameState()
    )

    // State for selected playing partner
    private val _selectedPartner = MutableStateFlow<MslPlayingPartner?>(null)
    val selectedPartner: StateFlow<MslPlayingPartner?> = _selectedPartner.asStateFlow()

    // Observe current golfer data from Room database
    val currentGolfer = getMslGolferUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Observe Sogo golfer data based on current golfer's golf link number
    val sogoGolfer = currentGolfer
        .flatMapLatest { golfer ->
            if (golfer?.golfLinkNo != null) {
                getSogoGolferUseCase(golfer.golfLinkNo)
            } else {
                flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Observe MSL fees from Room database
    val mslFees = getFeesUseCase.getFeesByEntityName("msl")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Include round state
    private val _includeRound = MutableStateFlow(true)
    val includeRound: StateFlow<Boolean> = _includeRound.asStateFlow()

    // Token cost calculation based on game holes and fees
    val tokenCost = combine(
        localGame,
        mslFees,
        _includeRound
    ) { game, fees, include ->
        if (!include) {
            0.0
        } else {
            game?.numberOfHoles?.let { holes ->
                fees.find { fee ->
                    fee.numberHoles == holes
                }?.cost ?: 0.0
            } ?: 0.0
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    // Can proceed calculation based on game availability and token balance
    val canProceed = combine(
        localGame,
        sogoGolfer,
        tokenCost,
        _includeRound
    ) { game, sogo, cost, include ->
        val hasCompetitions = game?.numberOfHoles != null && game.numberOfHoles > 0
        val hasSufficientTokens = !include || cost == 0.0 ||
                (sogo?.tokenBalance ?: 0) >= cost
        hasCompetitions && hasSufficientTokens
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun setIncludeRound(include: Boolean) {
        _includeRound.value = include
    }

    // Method to select a playing partner
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

    // Method to refresh all data from internet
    suspend fun refreshAllData(): Boolean {
        return try {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // Get the current club ID
            val selectedClub = getMslClubAndTenantIdsUseCase()
            if (selectedClub?.clubId != null) {
                val clubIdStr = selectedClub.clubId.toString()
                
                android.util.Log.d("PlayingPartnerVM", "🔄 Starting data refresh...")
                
                var allSuccessful = true
                
                // Step 1: Refresh golfer data from API and save to local DB
                android.util.Log.d("PlayingPartnerVM", "🔄 Step 1: Refreshing golfer data...")
                when (val golferResult = mslRepository.getGolfer(clubIdStr)) {
                    is NetworkResult.Success -> {
                        android.util.Log.d("PlayingPartnerVM", "✅ Fresh golfer data retrieved, saving to DB...")
                        mslGolferLocalDbRepository.saveGolfer(golferResult.data)
                        android.util.Log.d("PlayingPartnerVM", "✅ Golfer data saved to local DB")
                    }
                    is NetworkResult.Error -> {
                        android.util.Log.w("PlayingPartnerVM", "⚠️ Failed to refresh golfer data: ${golferResult.error}")
                        allSuccessful = false
                    }
                    is NetworkResult.Loading -> { /* Ignore */ }
                }
                
                // Step 2: Refresh game data from API and save to local DB
                android.util.Log.d("PlayingPartnerVM", "🔄 Step 2: Refreshing game data...")
                when (val gameResult = fetchAndSaveGameUseCase(clubIdStr)) {
                    is NetworkResult.Success -> {
                        android.util.Log.d("PlayingPartnerVM", "✅ Game data refreshed successfully")
                        android.util.Log.d("PlayingPartnerVM", "Updated playing partners: ${gameResult.data.playingPartners.size}")
                    }
                    is NetworkResult.Error -> {
                        android.util.Log.w("PlayingPartnerVM", "⚠️ Failed to refresh game data: ${gameResult.error}")
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Failed to refresh game data: ${gameResult.error.toUserMessage()}"
                        )
                        allSuccessful = false
                    }
                    is NetworkResult.Loading -> { /* Ignore */ }
                }
                
                // Step 3: Refresh competition data from API and save to local DB
                android.util.Log.d("PlayingPartnerVM", "🔄 Step 3: Refreshing competition data...")
                when (val competitionResult = fetchAndSaveCompetitionUseCase(clubIdStr)) {
                    is NetworkResult.Success -> {
                        android.util.Log.d("PlayingPartnerVM", "✅ Competition data refreshed successfully")
                    }
                    is NetworkResult.Error -> {
                        android.util.Log.w("PlayingPartnerVM", "⚠️ Failed to refresh competition data: ${competitionResult.error}")
                        allSuccessful = false
                    }
                    is NetworkResult.Loading -> { /* Ignore */ }
                }
                
                // Step 4: Refresh Sogo golfer data (includes token balance)
                android.util.Log.d("PlayingPartnerVM", "🔄 Step 4: Refreshing Sogo golfer data...")
                val currentUser = currentGolfer.value
                if (currentUser?.golfLinkNo != null) {
                    when (val sogoGolferResult = fetchAndSaveSogoGolferUseCase(currentUser.golfLinkNo)) {
                        is NetworkResult.Success -> {
                            android.util.Log.d("PlayingPartnerVM", "✅ Sogo golfer data refreshed successfully (Token balance: ${sogoGolferResult.data.tokenBalance})")
                        }
                        is NetworkResult.Error -> {
                            android.util.Log.w("PlayingPartnerVM", "⚠️ Failed to refresh Sogo golfer data: ${sogoGolferResult.error}")
                            allSuccessful = false
                        }
                        is NetworkResult.Loading -> { /* Ignore */ }
                    }
                } else {
                    android.util.Log.w("PlayingPartnerVM", "⚠️ No golf link number available for current golfer - skipping Sogo golfer refresh")
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = if (allSuccessful) "Data refreshed successfully" else null
                )
                
                android.util.Log.d("PlayingPartnerVM", "✅ All data refresh operations completed")
                allSuccessful
            } else {
                android.util.Log.w("PlayingPartnerVM", "⚠️ No club selected, cannot refresh data")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No club selected"
                )
                false
            }
        } catch (e: Exception) {
            android.util.Log.w("PlayingPartnerVM", "⚠️ Exception while refreshing data", e)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Refresh failed: ${e.message}"
            )
            false
        }
    }

    // Method to handle "Let's Play" button flow
    fun onLetsPlayClicked(onNavigateToPlayRound: () -> Unit) {
        val selectedPartner = _selectedPartner.value
        if (selectedPartner?.golfLinkNumber == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "No playing partner selected or partner has no Golf Link Number"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLetsPlayLoading = true,
                    errorMessage = null
                )

                // Step 1: Call PUT marker API
                android.util.Log.d("PlayingPartnerVM", "🔄 Step 1: Calling PUT marker API...")
                when (val markerResult = selectMarkerUseCase(selectedPartner.golfLinkNumber)) {
                    is NetworkResult.Success -> {
                        android.util.Log.d("PlayingPartnerVM", "✅ Marker selected successfully")
                    }
                    is NetworkResult.Error -> {
                        android.util.Log.e("PlayingPartnerVM", "❌ Failed to select marker: ${markerResult.error}")
                        _uiState.value = _uiState.value.copy(
                            isLetsPlayLoading = false,
                            errorMessage = "Failed to select marker: ${markerResult.error.toUserMessage()}"
                        )
                        return@launch
                    }
                    is NetworkResult.Loading -> { /* Ignore */ }
                }

                // Step 2: Get club ID for data refresh
                val selectedClub = getMslClubAndTenantIdsUseCase()
                if (selectedClub?.clubId == null) {
                    _uiState.value = _uiState.value.copy(
                        isLetsPlayLoading = false,
                        errorMessage = "No club selected"
                    )
                    return@launch
                }
                val clubIdStr = selectedClub.clubId.toString()

                // Step 3: Refresh game data
                android.util.Log.d("PlayingPartnerVM", "🔄 Step 3: Refreshing game data...")
                when (val gameResult = fetchAndSaveGameUseCase(clubIdStr)) {
                    is NetworkResult.Success -> {
                        android.util.Log.d("PlayingPartnerVM", "✅ Game data refreshed successfully")
                    }
                    is NetworkResult.Error -> {
                        android.util.Log.w("PlayingPartnerVM", "⚠️ Failed to refresh game data: ${gameResult.error}")
                        _uiState.value = _uiState.value.copy(
                            isLetsPlayLoading = false,
                            errorMessage = "Failed to refresh game data: ${gameResult.error.toUserMessage()}"
                        )
                        return@launch
                    }
                    is NetworkResult.Loading -> { /* Ignore */ }
                }

                // Step 4: Refresh competition data
                android.util.Log.d("PlayingPartnerVM", "🔄 Step 4: Refreshing competition data...")
                when (val competitionResult = fetchAndSaveCompetitionUseCase(clubIdStr)) {
                    is NetworkResult.Success -> {
                        android.util.Log.d("PlayingPartnerVM", "✅ Competition data refreshed successfully")
                    }
                    is NetworkResult.Error -> {
                        android.util.Log.w("PlayingPartnerVM", "⚠️ Failed to refresh competition data: ${competitionResult.error}")
                        // Don't fail the entire flow for competition data
                    }
                    is NetworkResult.Loading -> { /* Ignore */ }
                }

                // Step 5: Create Round object
                android.util.Log.d("PlayingPartnerVM", "🔄 Step 5: Creating Round object...")
                val round = createRoundFromCurrentData(selectedPartner)

                // Step 6: Save Round to Room
                android.util.Log.d("PlayingPartnerVM", "🔄 Step 6: Saving Round to database...")
                roundRepository.saveRound(round)
                android.util.Log.d("PlayingPartnerVM", "✅ Round saved to database")

                _uiState.value = _uiState.value.copy(
                    isLetsPlayLoading = false,
                    successMessage = "Ready to play!"
                )

                // Step 7: Navigate to PlayRound screen
                android.util.Log.d("PlayingPartnerVM", "🔄 Step 7: Navigating to PlayRound screen...")
                onNavigateToPlayRound()

            } catch (e: Exception) {
                android.util.Log.e("PlayingPartnerVM", "❌ Exception in Let's Play flow", e)
                _uiState.value = _uiState.value.copy(
                    isLetsPlayLoading = false,
                    errorMessage = "Let's Play failed: ${e.message}"
                )
            }
        }
    }

    private suspend fun createRoundFromCurrentData(selectedPartner: MslPlayingPartner): Round {
        val currentGolferData = currentGolfer.value
        val gameData = localGame.value
        val sogoGolferData = sogoGolfer.value
        val includeRoundValue = _includeRound.value

        return Round(
            id = UUID.randomUUID().toString(),
            uuid = UUID.randomUUID().toString(),
            entityId = null,
            roundPlayedOff = gameData?.gaHandicap,
            dailyHandicap = gameData?.dailyHandicap?.toDouble(),
            golfLinkHandicap = gameData?.gaHandicap,
            golflinkNo = currentGolferData?.golfLinkNo,
            roundDate = gameData?.bookingTime?.toLocalDate()?.atStartOfDay(),
            startTime = gameData?.bookingTime,
            finishTime = null,
            scratchRating = null,
            slopeRating = null,
            submittedTime = null,
            compScoreTotal = null,
            roundType = "MSL",
            clubId = null,
            clubName = null,
            golferId = null,
            golferFirstName = currentGolferData?.firstName,
            golferLastName = currentGolferData?.surname,
            golferGLNumber = currentGolferData?.golfLinkNo,
            markerFirstName = selectedPartner.firstName,
            markerLastName = selectedPartner.lastName,
            markerGLNumber = selectedPartner.golfLinkNumber,
            compType = gameData?.competitions?.firstOrNull()?.name,
            teeColor = gameData?.teeColour,
            isClubComp = true,
            isSubmitted = false,
            isApproved = false,
            mslMetaData = MslMetaData(isIncludeRoundOnSogo = includeRoundValue),
            createdDate = LocalDateTime.now()
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class PlayingPartnerUiState(
    val isLoading: Boolean = false,
    val isLetsPlayLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
