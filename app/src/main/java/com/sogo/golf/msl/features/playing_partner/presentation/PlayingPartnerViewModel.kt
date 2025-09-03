package com.sogo.golf.msl.features.playing_partner.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sogo.golf.msl.analytics.AnalyticsManager
import com.sogo.golf.msl.data.local.preferences.IncludeRoundPreferences
import com.sogo.golf.msl.data.network.NetworkChecker
import com.sogo.golf.msl.domain.model.HoleScore
import com.sogo.golf.msl.domain.model.MslMetaData
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.PlayingPartnerRound
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.model.StateInfo
import com.sogo.golf.msl.domain.model.mongodb.SogoGolfer
import com.sogo.golf.msl.domain.model.msl.MslCompetition
import com.sogo.golf.msl.domain.model.msl.MslGame
import com.sogo.golf.msl.domain.model.msl.MslPlayer
import com.sogo.golf.msl.domain.model.msl.MslPlayingPartner
import com.sogo.golf.msl.domain.model.msl.SelectedClub
import com.sogo.golf.msl.domain.repository.MslCompetitionLocalDbRepository
import com.sogo.golf.msl.domain.repository.MslGameLocalDbRepository
import com.sogo.golf.msl.domain.repository.MslGolferLocalDbRepository
import com.sogo.golf.msl.domain.repository.RoundLocalDbRepository
import com.sogo.golf.msl.domain.repository.remote.MslRepository
import com.sogo.golf.msl.domain.usecase.club.GetMslClubAndTenantIdsUseCase
import com.sogo.golf.msl.domain.usecase.competition.FetchAndSaveCompetitionUseCase
import com.sogo.golf.msl.domain.usecase.fees.GetFeesUseCase
import com.sogo.golf.msl.domain.usecase.game.FetchAndSaveGameUseCase
import com.sogo.golf.msl.domain.usecase.marker.SelectMarkerUseCase
import com.sogo.golf.msl.domain.usecase.msl_golfer.GetMslGolferUseCase
import com.sogo.golf.msl.domain.usecase.round.CreateRoundUseCase
import com.sogo.golf.msl.domain.usecase.sogo_golfer.FetchAndSaveSogoGolferUseCase
import com.sogo.golf.msl.domain.usecase.sogo_golfer.GetSogoGolferUseCase
import com.sogo.golf.msl.domain.usecase.transaction.CheckExistingTransactionUseCase
import com.sogo.golf.msl.domain.usecase.app.GetAppVersionUseCase
import com.sogo.golf.msl.domain.usecase.app.GetStateInfoUseCase
import com.sogo.golf.msl.domain.usecase.transaction.CreateTransactionUseCase
import com.sogo.golf.msl.shared.utils.ObjectIdUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PlayingPartnerViewModel @Inject constructor(
    private val networkChecker: NetworkChecker,
    private val getMslGolferUseCase: GetMslGolferUseCase,
    private val mslGolferRepository: MslGolferLocalDbRepository,
    private val gameRepository: MslGameLocalDbRepository,
    private val competitionRepository: MslCompetitionLocalDbRepository,
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
    private val createRoundUseCase: CreateRoundUseCase,
    private val checkExistingTransactionUseCase: CheckExistingTransactionUseCase,
    private val createTransactionUseCase: CreateTransactionUseCase,
    private val includeRoundPreferences: IncludeRoundPreferences,
    private val updateTokenBalanceUseCase: com.sogo.golf.msl.domain.usecase.sogo_golfer.UpdateTokenBalanceUseCase,
    private val analyticsManager: AnalyticsManager,
    private val getAppVersionUseCase: GetAppVersionUseCase,
    private val getStateInfoUseCase: GetStateInfoUseCase
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

    // Always observe local competition data (works offline)
    val localCompetition = competitionRepository.getCompetition()
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
//    val mslFees = getFeesUseCase.getFeesByEntityName("msl")
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5000),
//            initialValue = emptyList()
//        )

    // Include round state - load from SharedPreferences
    private val _includeRound = MutableStateFlow(true)
    val includeRound: StateFlow<Boolean> = _includeRound.asStateFlow()

    // Token cost from SharedPreferences
    private val _tokenCost = MutableStateFlow(0.0)
    val tokenCost: StateFlow<Double> = _tokenCost.asStateFlow()

    init {
        // Load include round preference and token cost on initialization
        viewModelScope.launch {
            _includeRound.value = includeRoundPreferences.getIncludeRound()
            val test = includeRoundPreferences.getRoundCost()
            _tokenCost.value = if (_includeRound.value) {
                includeRoundPreferences.getRoundCost()
            } else {
                0.0
            }
        }
    }

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

    init {
        viewModelScope.launch {
            currentGolfer.collect { golfer ->
                if (golfer != null) {
                    Log.d("PlayingPartnerVM", "=== MSL GOLFER LOADED ===")
                    Log.d("PlayingPartnerVM", "Golfer: ${golfer.firstName} ${golfer.surname} (${golfer.golfLinkNo})")
                } else {
                    Log.d("PlayingPartnerVM", "⚠️ MSL Golfer is null")
                }
            }
        }
        
        viewModelScope.launch {
            sogoGolfer.collect { sogo ->
                if (sogo != null) {
                    Log.d("PlayingPartnerVM", "=== SOGO GOLFER LOADED ===")
                    Log.d("PlayingPartnerVM", "SogoGolfer: ${sogo.firstName} ${sogo.lastName}")
                    Log.d("PlayingPartnerVM", "Token Balance: ${sogo.tokenBalance}")
                    Log.d("PlayingPartnerVM", "Entity ID: ${sogo.entityId}")
                } else {
                    Log.d("PlayingPartnerVM", "⚠️ Sogo Golfer is null")
                }
            }
        }
        
        viewModelScope.launch {
            localGame.collect { game ->
                if (game != null) {
                    Log.d("PlayingPartnerVM", "=== GAME DATA LOADED ===")
                    Log.d("PlayingPartnerVM", "Game: ${game.bookingTime}, Partners: ${game.playingPartners.size}")
                } else {
                    Log.d("PlayingPartnerVM", "⚠️ Game data is null")
                }
            }
        }
        
        viewModelScope.launch {
            localCompetition.collect { competition ->
                if (competition != null) {
                    Log.d("PlayingPartnerVM", "=== COMPETITION DATA LOADED ===")
                    Log.d("PlayingPartnerVM", "Competition: ${competition.players.size} players")
                } else {
                    Log.d("PlayingPartnerVM", "⚠️ Competition data is null")
                }
            }
        }
    }

    fun setIncludeRound(include: Boolean) {
        _includeRound.value = include
        viewModelScope.launch {
            includeRoundPreferences.setIncludeRound(include)
            // Update token cost when include round changes
            _tokenCost.value = if (include) {
                includeRoundPreferences.getRoundCost()
            } else {
                0.0
            }
        }
    }

    // Method to select a playing partner
    fun selectPartner(partner: MslPlayingPartner) {
        val currentSelection = _selectedPartner.value
        
        _selectedPartner.value = if (currentSelection == partner) {
            // Deselect if same partner is tapped again
            trackPlayingPartnerDeselected(partner)
            null
        } else {
            // Select the new partner
            trackPlayingPartnerSelected(partner)
            partner
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
            if (!networkChecker.isNetworkAvailable()) {
                // Start with loading state to acknowledge the gesture
                _uiState.value = _uiState.value.copy(isLoading = true, isRefreshLoading = true, errorMessage = null)

                // Give the UI time to register the refresh gesture
                kotlinx.coroutines.delay(100) // Small delay

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshLoading = false,
                    errorMessage = "No internet connection"
                )

                return false
            }

            // Get the current club ID
            val selectedClub = getMslClubAndTenantIdsUseCase()
            if (selectedClub?.clubId != null) {
                val clubIdStr = selectedClub.clubId.toString()
                
                _uiState.value = _uiState.value.copy(isLoading = true, isRefreshLoading = true, errorMessage = null)
                Log.d("PlayingPartnerVM", "🔄 Starting data refresh...")
                
                var allSuccessful = true
                
                // Step 1: Refresh golfer data from API and save to local DB
                Log.d("PlayingPartnerVM", "🔄 Step 1: Refreshing golfer data...")
                when (val golferResult = mslRepository.getGolfer(clubIdStr)) {
                    is NetworkResult.Success -> {
                        Log.d("PlayingPartnerVM", "✅ Fresh golfer data retrieved, saving to DB...")
                        mslGolferLocalDbRepository.saveGolfer(golferResult.data)
                        Log.d("PlayingPartnerVM", "✅ Golfer data saved to local DB")
                    }
                    is NetworkResult.Error -> {
                        Log.w("PlayingPartnerVM", "⚠️ Failed to refresh golfer data: ${golferResult.error}")
                        allSuccessful = false
                    }
                    is NetworkResult.Loading -> { /* Ignore */ }
                }
                
                // Step 2: Refresh game data from API and save to local DB
                Log.d("PlayingPartnerVM", "🔄 Step 2: Refreshing game data...")
                when (val gameResult = fetchAndSaveGameUseCase(clubIdStr)) {
                    is NetworkResult.Success -> {
                        Log.d("PlayingPartnerVM", "✅ Game data refreshed successfully")
                        Log.d("PlayingPartnerVM", "Updated playing partners: ${gameResult.data.playingPartners.size}")
                    }
                    is NetworkResult.Error -> {
                        Log.w("PlayingPartnerVM", "⚠️ Failed to refresh game data: ${gameResult.error}")
                        _uiState.value = _uiState.value.copy(
                            //errorMessage = "Failed to refresh game data: ${gameResult.error.toUserMessage()}"
                            errorMessage = "${gameResult.error.toUserMessage()} Please log out of the app and back in to refresh data"
                        )

                        allSuccessful = false
                    }
                    is NetworkResult.Loading -> { /* Ignore */ }
                }
                
                // Step 3: Refresh competition data from API and save to local DB
                Log.d("PlayingPartnerVM", "🔄 Step 3: Refreshing competition data...")
                when (val competitionResult = fetchAndSaveCompetitionUseCase(clubIdStr)) {
                    is NetworkResult.Success -> {
                        Log.d("PlayingPartnerVM", "✅ Competition data refreshed successfully")
                    }
                    is NetworkResult.Error -> {
                        Log.w("PlayingPartnerVM", "⚠️ Failed to refresh competition data: ${competitionResult.error}")
                        allSuccessful = false
                    }
                    is NetworkResult.Loading -> { /* Ignore */ }
                }
                
                // Step 4: Refresh Sogo golfer data (includes token balance)
                Log.d("PlayingPartnerVM", "🔄 Step 4: Refreshing Sogo golfer data...")
                val currentUser = currentGolfer.value
                if (currentUser?.golfLinkNo != null) {
                    when (val sogoGolferResult = fetchAndSaveSogoGolferUseCase(currentUser.golfLinkNo)) {
                        is NetworkResult.Success -> {
                            Log.d("PlayingPartnerVM", "✅ Sogo golfer data refreshed successfully (Token balance: ${sogoGolferResult.data.tokenBalance})")
                        }
                        is NetworkResult.Error -> {
                            Log.w("PlayingPartnerVM", "⚠️ Failed to refresh Sogo golfer data: ${sogoGolferResult.error}")
                            allSuccessful = false
                        }
                        is NetworkResult.Loading -> { /* Ignore */ }
                    }
                } else {
                    Log.w("PlayingPartnerVM", "⚠️ No golf link number available for current golfer - skipping Sogo golfer refresh")
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshLoading = false,
                    successMessage = if (allSuccessful) "Data refreshed successfully" else null
                )
                
                Log.d("PlayingPartnerVM", "✅ All data refresh operations completed")
                allSuccessful
            } else {
                Log.w("PlayingPartnerVM", "⚠️ No club selected, cannot refresh data")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshLoading = false,
                    errorMessage = "No club selected"
                )
                false
            }
        } catch (e: Exception) {
            Log.w("PlayingPartnerVM", "⚠️ Exception while refreshing data", e)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isRefreshLoading = false,
                errorMessage = "Refresh failed: ${e.message}"
            )
            Sentry.captureException(e)
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

                // Step 1: Verify we have required data from Room
                val currentGolferData = currentGolfer.value
                val gameData = localGame.value
                val sogoGolferData = sogoGolfer.value
                val competitionData = localCompetition.value

                Log.d("PlayingPartnerVM", "🔍 Checking Room data availability...")
                Log.d("PlayingPartnerVM", "Current golfer: ${currentGolferData?.firstName} ${currentGolferData?.surname}")
                Log.d("PlayingPartnerVM", "Game data: ${gameData?.bookingTime}")
                Log.d("PlayingPartnerVM", "Sogo golfer: ${sogoGolferData?.firstName} (tokens: ${sogoGolferData?.tokenBalance})")
                Log.d("PlayingPartnerVM", "Competition data: ${competitionData?.players?.size ?: 0} players")

                if (currentGolferData == null) {
                    _uiState.value = _uiState.value.copy(
                        isLetsPlayLoading = false,
                        errorMessage = "Current golfer data not available. Please refresh the app."
                    )
                    Sentry.captureMessage("Current golfer data not available (tapped Play button). Can not continue.")
                    return@launch
                }

                if (gameData == null) {
                    _uiState.value = _uiState.value.copy(
                        isLetsPlayLoading = false,
                        errorMessage = "Game data not available. Please refresh the app."
                    )
                    Sentry.captureMessage("Game data not available (tapped Play button). Can not continue.")
                    return@launch
                }

                if (sogoGolferData == null) {
                    _uiState.value = _uiState.value.copy(
                        isLetsPlayLoading = false,
                        errorMessage = "Sogo golfer data not available. Please refresh the app."
                    )
                    Sentry.captureMessage("Sogo golfer data not available (tapped Play button). Can not continue.")
                    return@launch
                }

                // Step 2: Call PUT marker API
                Log.d("PlayingPartnerVM", "🔄 Step 2: Calling PUT marker API...")
                when (val markerResult = selectMarkerUseCase(selectedPartner.golfLinkNumber)) {
                    is NetworkResult.Success -> {
                        Log.d("PlayingPartnerVM", "✅ Marker selected successfully")
                    }
                    is NetworkResult.Error -> {
                        Log.e("PlayingPartnerVM", "❌ Failed to select marker: ${markerResult.error}")
                        _uiState.value = _uiState.value.copy(
                            isLetsPlayLoading = false,
                            errorMessage = "Failed to select marker: ${markerResult.error.toUserMessage()}"
                        )
                        Sentry.captureMessage("Call PUT marker failed.")
                        return@launch
                    }
                    is NetworkResult.Loading -> { /* Ignore */ }
                }

                val selectedClub = getMslClubAndTenantIdsUseCase()
                var clubIdStr = ""
                if (selectedClub?.clubId != null) {
                    clubIdStr = selectedClub.clubId.toString()
                }

                //now we need to refetch the competition since the selected marker data will be there
                Log.d("PlayingPartnerVM", "🔄 Step 3: Refreshing competition data...")
                when (val competitionResult = fetchAndSaveCompetitionUseCase(clubIdStr)) {
                    is NetworkResult.Success -> {
                        Log.d("PlayingPartnerVM", "✅ Competition data refreshed successfully")
                    }
                    is NetworkResult.Error -> {
                        Log.w("PlayingPartnerVM", "⚠️ Failed to refresh competition data: ${competitionResult.error}")
                        _uiState.value = _uiState.value.copy(
                            isLetsPlayLoading = false,
                            errorMessage = "Failed to re-fetch msl competition data: ${competitionResult.error.toUserMessage()}"
                        )
                        return@launch
                    }
                    is NetworkResult.Loading -> { /* Ignore */ }
                }

                //we should also refetch the game since it has who is marking who data which we will need in playroundviewmodel
                Log.d("PlayingPartnerVM", "🔄 Step 2: Refreshing game data...")
                when (val gameResult = fetchAndSaveGameUseCase(clubIdStr)) {
                    is NetworkResult.Success -> {
                        Log.d("PlayingPartnerVM", "✅ Game data refreshed successfully")
                        Log.d("PlayingPartnerVM", "Updated playing partners: ${gameResult.data.playingPartners.size}")
                    }
                    is NetworkResult.Error -> {
                        Log.w("PlayingPartnerVM", "⚠️ Failed to refresh game data: ${gameResult.error}")
                        _uiState.value = _uiState.value.copy(
//                            errorMessage = "Failed to refresh game data: ${gameResult.error.toUserMessage()}"
                            errorMessage = "${gameResult.error.toUserMessage()} Please log out of the app and back in to refresh data"
                        )
                        return@launch
                    }
                    is NetworkResult.Loading -> { /* Ignore */ }
                }


                // Step 3: Get fresh competition data from Room after refetch
                Log.d("PlayingPartnerVM", "🔄 Step 3: Getting fresh competition data from Room...")
                val freshCompetitionData = localCompetition.value
                Log.d("PlayingPartnerVM", "Fresh competition data: ${freshCompetitionData?.players?.size ?: 0} players")

                // Initialize transaction ID for round creation (will be set if payment occurs)
                var roundTransactionId: String? = null

                // Step 4: Handle fee charging with duplicate prevention
                Log.d("PlayingPartnerVM", "🔄 Step 4: Checking for duplicate transactions and handling fees...")
                val includeRoundValue = _includeRound.value
                val currentTokenCost = tokenCost.value
                
                if (includeRoundValue && currentTokenCost > 0) {
                    Log.d("PlayingPartnerVM", "💰 Fee required: $currentTokenCost tokens")
                    
                    // Get mainCompetitionId from game data
                    val mainCompetitionId = gameData.mainCompetitionId
                    if (mainCompetitionId == null) {
                        Log.e("PlayingPartnerVM", "❌ No mainCompetitionId found in game data")
                        _uiState.value = _uiState.value.copy(
                            isLetsPlayLoading = false,
                            errorMessage = "Game data missing competition ID. Please refresh and try again."
                        )
                        Sentry.captureMessage("Game data missing competition ID. Can not continue.")
                        return@launch
                    }
                    
                    // Check for existing transactions
                    Log.d("PlayingPartnerVM", "🔍 Checking for existing transactions for golfer ${sogoGolferData.id}, competition $mainCompetitionId")
                    checkExistingTransactionUseCase(sogoGolferData.id, mainCompetitionId).fold(
                        onSuccess = { hasExistingTransaction ->
                            if (hasExistingTransaction) {
                                Log.d("PlayingPartnerVM", "✅ Existing transaction found - skipping fee charge")
                            } else {
                                Log.d("PlayingPartnerVM", "💳 No existing transaction - charging fee")
                                
                                // Check sufficient balance
                                if (sogoGolferData.tokenBalance < currentTokenCost) {
                                    Log.e("PlayingPartnerVM", "❌ Insufficient token balance: ${sogoGolferData.tokenBalance} < $currentTokenCost")
                                    _uiState.value = _uiState.value.copy(
                                        isLetsPlayLoading = false,
                                        errorMessage = "Insufficient token balance. Please purchase more tokens."
                                    )
                                    return@launch
                                }
                                
                                // Create transaction and capture ID for round creation
                                val transactionId = ObjectIdUtils.generateObjectId()
                                createTransactionUseCase(
                                    tokens = currentTokenCost.toInt(),
                                    entityIdVal = sogoGolferData.entityId,
                                    transId = transactionId,
                                    sogoGolfer = sogoGolferData,
                                    transactionTypeVal = "play_round",
                                    debitCreditTypeVal = "debit",
                                    commentVal = "Round fee for competition $mainCompetitionId",
                                    statusVal = "completed",
                                    mainCompetitionId = mainCompetitionId
                                ).fold(
                                    onSuccess = {
                                        Log.d("PlayingPartnerVM", "✅ Transaction created successfully")
                                        roundTransactionId = transactionId
                                        
                                        // Update token balance locally and on server
                                        val newBalance = sogoGolferData.tokenBalance - currentTokenCost.toInt()
                                        Log.d("PlayingPartnerVM", "💰 Updating token balance: ${sogoGolferData.tokenBalance} -> $newBalance")
                                        
                                        viewModelScope.launch {
                                            updateTokenBalanceUseCase(newBalance, sogoGolferData).fold(
                                                onSuccess = { updatedGolfer ->
                                                    Log.d("PlayingPartnerVM", "✅ Token balance updated successfully to ${updatedGolfer.tokenBalance}")
                                                },
                                                onFailure = { balanceError ->
                                                    Log.e("PlayingPartnerVM", "❌ Failed to update token balance: ${balanceError.message}")
                                                    // Continue with round creation even if balance update fails
                                                    // The transaction was already created successfully
                                                }
                                            )
                                        }
                                    },
                                    onFailure = { error ->
                                        Log.e("PlayingPartnerVM", "❌ Failed to create transaction: ${error.message}")
                                        _uiState.value = _uiState.value.copy(
                                            isLetsPlayLoading = false,
                                            errorMessage = "Failed to process payment: ${error.message}"
                                        )
                                        return@launch
                                    }
                                )
                            }
                        },
                        onFailure = { error ->
                            Log.e("PlayingPartnerVM", "❌ Failed to check existing transactions: ${error.message}")
                            _uiState.value = _uiState.value.copy(
                                isLetsPlayLoading = false,
                                errorMessage = "Failed to verify payment status: ${error.message}"
                            )
                            return@launch
                        }
                    )
                } else {
                    Log.d("PlayingPartnerVM", "ℹ️ No fee required (include round: $includeRoundValue, cost: $currentTokenCost)")
                }

                // Step 5: Create Round object using fresh Room data
                Log.d("PlayingPartnerVM", "🔄 Step 5: Creating Round object from fresh Room data...")
                val round = createRoundFromRoomData(selectedPartner, currentGolferData, gameData, sogoGolferData, freshCompetitionData, selectedClub, roundTransactionId)

                // Step 6: Save Round to Room
                Log.d("PlayingPartnerVM", "🔄 Step 6: Saving Round to database...")
                roundRepository.saveRound(round)
                Log.d("PlayingPartnerVM", "✅ Round saved to database")

                // Step 7: Save Round to MongoDB API
                Log.d("PlayingPartnerVM", "🔄 Step 7: Syncing Round to MongoDB...")
                when (val createRoundResult = createRoundUseCase(round)) {
                    is NetworkResult.Success -> {
                        Log.d("PlayingPartnerVM", "✅ Round synced to MongoDB successfully")
                        val syncedRound = round.copy(isSynced = true)
                        roundRepository.saveRound(syncedRound)
                    }
                    is NetworkResult.Error -> {
                        Log.w("PlayingPartnerVM", "⚠️ Failed to sync round to MongoDB: ${createRoundResult.error}")
                        Sentry.captureException(Exception("Failed to save round to MongoDB"))
                    }
                    is NetworkResult.Loading -> { /* Ignore */ }
                }

                _uiState.value = _uiState.value.copy(
                    isLetsPlayLoading = false,
                    successMessage = "Ready to play!"
                )

                // Step 8: Navigate to PlayRound screen
                Log.d("PlayingPartnerVM", "🔄 Step 8: Navigating to PlayRound screen...")

                // Track round started event
                trackRoundStarted(selectedPartner)
                
                onNavigateToPlayRound()

            } catch (e: Exception) {
                Log.e("PlayingPartnerVM", "❌ Exception in Let's Play flow", e)
                _uiState.value = _uiState.value.copy(
                    isLetsPlayLoading = false,
                    errorMessage = "Let's Play failed: ${e.message}"
                )
                Sentry.captureException(e)
            }
        }
    }

    private suspend fun createRoundFromRoomData(
        selectedPartner: MslPlayingPartner,
        currentGolferData: com.sogo.golf.msl.domain.model.msl.MslGolfer,
        gameData: MslGame,
        sogoGolferData: SogoGolfer,
        competitionData: MslCompetition?,
        selectedClub: SelectedClub?,
        transactionId: String? = null
    ): Round {
        val includeRoundValue = _includeRound.value

        // Use current local time (will be converted to UTC when sending to MongoDB)
        val nowUtc = LocalDateTime.now()

        Log.d("PlayingPartnerVM", "📝 Creating Round with data:")
        Log.d("PlayingPartnerVM", "  - Golfer: ${currentGolferData.firstName} ${currentGolferData.surname}")
        Log.d("PlayingPartnerVM", "  - Game: ${gameData.bookingTime}")
        Log.d("PlayingPartnerVM", "  - Sogo tokens: ${sogoGolferData.tokenBalance}")
        Log.d("PlayingPartnerVM", "  - Competition: ${competitionData?.players?.size ?: 0} players")
        Log.d("PlayingPartnerVM", "  - Include round: $includeRoundValue")

        val golfer = competitionData?.players?.find { it.golfLinkNumber == currentGolferData.golfLinkNo }
        
        Log.d("PlayingPartnerVM", "📝 Looking for current golfer in competition data:")
        Log.d("PlayingPartnerVM", "  - Searching for GL#: ${currentGolferData.golfLinkNo}")
        Log.d("PlayingPartnerVM", "  - Found golfer: ${if (golfer != null) "${golfer.firstName} ${golfer.lastName}" else "NOT FOUND"}")
        if (golfer == null) {
            Log.w("PlayingPartnerVM", "⚠️ Current golfer not found in competition players list")
            competitionData?.players?.forEach { player ->
                Log.d("PlayingPartnerVM", "  Available player: ${player.firstName} ${player.lastName} (GL#: ${player.golfLinkNumber})")
            }
        }

        val playingPartnerRound = createPlayingPartnerRound(
            selectedPartner = selectedPartner,
            gameData = gameData,
            competitionData = competitionData,
            sogoGolferData = sogoGolferData
        )

        val holeScores = createHoleScores(competitionData, golfer)

        // Get partner's email from MslGolfer data
        val partnerMslGolfer = selectedPartner.golfLinkNumber?.let { golfLinkNo ->
            mslGolferRepository.getGolferByGolfLinkNo(golfLinkNo)
        }
        val partnerEmail = partnerMslGolfer?.email

        return Round(
            clubId = null,
            clubName = selectedClub?.clubName,
            clubState = getStateInfoUseCase(sogoGolferData.state?.shortName),
            clubUuid = null,
            comment = null,
            compScoreTotal = 0,
            compType = golfer?.scoreType?.lowercase(Locale.ROOT) ?: "unknown",
            courseId = null,
            courseUuid = null,
            createdDate = nowUtc,
            dailyHandicap = gameData.dailyHandicap?.toDouble(),
            entityId = sogoGolferData.entityId,
            finishTime = null,
            golferEmail = sogoGolferData.email,
            golferFirstName = sogoGolferData.firstName,
            golferGLNumber = sogoGolferData.golfLinkNo,
            golferGender = sogoGolferData.gender,
            golferImageUrl = null,
            golferLastName = sogoGolferData.lastName,
            golferId = sogoGolferData.id,

            golfLinkHandicap = gameData.gaHandicap,
            golflinkNo = currentGolferData.golfLinkNo,

            holeScores = holeScores,

            id = ObjectIdUtils.generateObjectId(),

            isAbandoned = false,
            isApproved = false,
            isClubComp = true,
            isClubSubmitted = false,
            isDeleted = false,
            isMarkedForReview = false,
            isSubmitted = false,
            isValidated = false,

            markerEmail = partnerEmail,
            markerFirstName = selectedPartner.firstName,
            markerGLNumber = selectedPartner.golfLinkNumber,
            markerLastName = selectedPartner.lastName,

            mslMetaData = MslMetaData(isIncludeRoundOnSogo = includeRoundValue),

            playingPartnerRound = playingPartnerRound,

            roundApprovalSignatureUrl = null,
            roundApprovedBy = null,
            roundDate = nowUtc,
            roundPlayedOff = gameData.gaHandicap,
            roundRefCode = null,
            roundType = "competition",
            scorecardUrl = null,
            scratchRating = golfer?.scratchRating?.toFloat(),
            slopeRating = golfer?.slopeRating?.toFloat(),
            sogoAppVersion = getAppVersionUseCase(),
            startTime = nowUtc,

            submittedTime = null,

            teeColor = golfer?.teeColourName?.lowercase() ?: gameData.teeColourName?.lowercase(),

            thirdPartyScorecardId = null,

            transactionId = transactionId,
            updateDate = null,
            updateUserId = null,
            uuid = null,
            whsBackScoreMaximumScore = null,
            whsBackScorePar = null,
            whsBackScoreStableford = null,
            whsBackScoreStroke = null,
            whsFrontScoreMaximumScore = null,
            whsFrontScorePar = null,
            whsFrontScoreStableford = null,
            whsFrontScoreStroke = null
        )
    }

    private fun createHoleScores(competitionData: MslCompetition?, specificPlayer: MslPlayer? = null): List<HoleScore> {
        // Only use the specific player's data if provided, don't fall back to first player
        val holes = specificPlayer?.holes ?: emptyList()

        Log.d("PlayingPartnerVM", "🏌️ Creating hole scores for player: ${specificPlayer?.firstName} ${specificPlayer?.lastName}")
        Log.d("PlayingPartnerVM", "🏌️ Number of holes: ${holes.size}")
        holes.forEach { hole ->
            Log.d("PlayingPartnerVM", "  - Hole ${hole.holeNumber}: par=${hole.par}, distance=${hole.distance}, indexes=${hole.strokeIndexes}")
        }
        
        if (holes.isEmpty()) {
            Log.w("PlayingPartnerVM", "⚠️ No hole data found for player")
        }
        
        return holes.map { holeData ->
            HoleScore(
                holeNumber = holeData.holeNumber,
                par = holeData.par,
                index1 = holeData.strokeIndexes.getOrNull(0) ?: 0,
                index2 = holeData.strokeIndexes.getOrNull(1) ?: 0,
                index3 = holeData.strokeIndexes.getOrNull(2) ?: 0,
                meters = holeData.distance,
                strokes = 0,
                score = 0f,
            )
        }
    }

    private fun createPlayingPartnerRound(
        selectedPartner: MslPlayingPartner,
        gameData: MslGame,
        competitionData: MslCompetition?,
        sogoGolferData: SogoGolfer
    ): PlayingPartnerRound {
        Log.d("PlayingPartnerVM", "📝 Creating PlayingPartnerRound for: ${selectedPartner.firstName} ${selectedPartner.lastName}")

        // Use current local time (will be converted to UTC when sending to MongoDB)
        val nowUtc = LocalDateTime.now()

        val partnerGolfer = competitionData?.players?.find { it.golfLinkNumber == selectedPartner.golfLinkNumber }
        
        Log.d("PlayingPartnerVM", "📝 Looking for playing partner in competition data:")
        Log.d("PlayingPartnerVM", "  - Searching for GL#: ${selectedPartner.golfLinkNumber}")
        Log.d("PlayingPartnerVM", "  - Found partner: ${if (partnerGolfer != null) "${partnerGolfer.firstName} ${partnerGolfer.lastName}" else "NOT FOUND"}")
        if (partnerGolfer == null) {
            Log.w("PlayingPartnerVM", "⚠️ Playing partner not found in competition players list")
        }

        val holeScores = createHoleScores(competitionData, partnerGolfer)

        return PlayingPartnerRound(
            uuid = null,
            entityId = sogoGolferData.entityId,
            dailyHandicap = selectedPartner.dailyHandicap.toFloat(),
            golfLinkHandicap = selectedPartner.dailyHandicap.toFloat(),
            roundDate = nowUtc,
            roundType = "competition",
            startTime = nowUtc,
            finishTime = null,
            submittedTime = null,
            scratchRating = partnerGolfer?.scratchRating?.toFloat(),
            slopeRating = partnerGolfer?.slopeRating?.toFloat(),
            compScoreTotal = 0,
            teeColor = partnerGolfer?.teeColourName?.lowercase() ?: gameData.teeColourName?.lowercase(),
            compType = partnerGolfer?.scoreType?.lowercase(Locale.ROOT) ?: "unknown",
            isSubmitted = false,
            golferId = null, /////////////////////////////////////// todo:
            golferFirstName = selectedPartner.firstName,
            golferLastName = selectedPartner.lastName,
            golferGLNumber = selectedPartner.golfLinkNumber,
            golflinkNo = selectedPartner.golfLinkNumber,
            golferEmail = sogoGolferData.email,
            golferImageUrl = null,
            golferGender = partnerGolfer?.gender?.lowercase(),
            holeScores = holeScores,
            roundApprovalSignatureUrl = null,
            createdDate = nowUtc,
            updateDate = null,
            deleteDate = null
        )
    }

    fun trackPlayingPartnerScreenViewed() {
        val eventProperties = mutableMapOf<String, Any>()
        
        // Get playing partners data from local game
        localGame.value?.playingPartners?.let { partners ->
            if (partners.isNotEmpty()) {
                val partnersData = partners.map { partner ->
                    mapOf(
                        "firstName" to (partner.firstName ?: ""),
                        "lastName" to (partner.lastName ?: ""),
                        "golfLinkNumber" to (partner.golfLinkNumber ?: ""),
                        "markedByGolfLinkNumber" to (partner.markedByGolfLinkNumber ?: "")
                    )
                }
                eventProperties["playing_partners"] = partnersData
                eventProperties["partner_count"] = partners.size
            } else {
                eventProperties["partner_count"] = 0
            }
        } ?: run {
            eventProperties["partner_count"] = 0
        }
        
        analyticsManager.trackEvent(AnalyticsManager.EVENT_PLAYING_PARTNER_SCREEN_VIEWED, eventProperties)
    }

    private fun trackPlayingPartnerSelected(partner: MslPlayingPartner) {
        val eventProperties = mutableMapOf<String, Any>()
        eventProperties["partner_name"] = "${partner.firstName ?: ""} ${partner.lastName ?: ""}".trim()
        partner.golfLinkNumber?.let { eventProperties["golf_link_number"] = it }
        partner.markedByGolfLinkNumber?.let { eventProperties["marked_by_golf_link_number"] = it }
        
        analyticsManager.trackEvent(AnalyticsManager.EVENT_PLAYING_PARTNER_SELECTED, eventProperties)
    }

    private fun trackPlayingPartnerDeselected(partner: MslPlayingPartner) {
        val eventProperties = mutableMapOf<String, Any>()
        eventProperties["partner_name"] = "${partner.firstName ?: ""} ${partner.lastName ?: ""}".trim()
        partner.golfLinkNumber?.let { eventProperties["golf_link_number"] = it }
        partner.markedByGolfLinkNumber?.let { eventProperties["marked_by_golf_link_number"] = it }
        
        analyticsManager.trackEvent(AnalyticsManager.EVENT_PLAYING_PARTNER_DESELECTED, eventProperties)
    }

    private fun trackRoundStarted(selectedPartner: MslPlayingPartner) {
        val eventProperties = mutableMapOf<String, Any>()
        eventProperties["selected_partner_name"] = "${selectedPartner.firstName ?: ""} ${selectedPartner.lastName ?: ""}".trim()
        selectedPartner.golfLinkNumber?.let { eventProperties["selected_partner_golf_link_number"] = it }
        
        analyticsManager.trackEvent(AnalyticsManager.EVENT_ROUND_STARTED, eventProperties)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class PlayingPartnerUiState(
    val isLoading: Boolean = false,
    val isRefreshLoading: Boolean = false,
    val isLetsPlayLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
