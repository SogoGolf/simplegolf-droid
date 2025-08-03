package com.sogo.golf.msl.features.playing_partner.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revenuecat.purchases.paywalls.components.common.ComponentOverride
import com.sogo.golf.msl.data.network.NetworkChecker
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
import com.sogo.golf.msl.domain.model.PlayingPartnerRound
import com.sogo.golf.msl.domain.model.HoleScore
import com.sogo.golf.msl.domain.model.MslMetaData
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.mongodb.SogoGolfer
import com.sogo.golf.msl.domain.model.msl.MslCompetition
import com.sogo.golf.msl.domain.model.msl.MslPlayer
import org.threeten.bp.LocalDateTime
import java.util.UUID
import java.util.Locale
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
import com.sogo.golf.msl.domain.model.msl.MslGame
import com.sogo.golf.msl.domain.model.msl.SelectedClub
import com.sogo.golf.msl.domain.repository.MslGolferLocalDbRepository
import com.sogo.golf.msl.domain.repository.MslCompetitionLocalDbRepository
import com.sogo.golf.msl.domain.repository.remote.MslRepository
import com.sogo.golf.msl.domain.usecase.sogo_golfer.FetchAndSaveSogoGolferUseCase
import com.sogo.golf.msl.domain.usecase.round.CreateRoundUseCase
import com.sogo.golf.msl.domain.usecase.transaction.CheckExistingTransactionUseCase
import com.sogo.golf.msl.domain.usecase.transaction.CreateTransactionUseCase
import com.sogo.golf.msl.data.local.preferences.IncludeRoundPreferences
import com.sogo.golf.msl.shared.utils.ObjectIdUtils
import kotlinx.coroutines.launch

@HiltViewModel
class PlayingPartnerViewModel @Inject constructor(
    private val networkChecker: NetworkChecker,
    private val getMslGolferUseCase: GetMslGolferUseCase,
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
    private val updateTokenBalanceUseCase: com.sogo.golf.msl.domain.usecase.sogo_golfer.UpdateTokenBalanceUseCase
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
            Log.d("cc", test.toString())
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
                    android.util.Log.d("PlayingPartnerVM", "=== MSL GOLFER LOADED ===")
                    android.util.Log.d("PlayingPartnerVM", "Golfer: ${golfer.firstName} ${golfer.surname} (${golfer.golfLinkNo})")
                } else {
                    android.util.Log.d("PlayingPartnerVM", "⚠️ MSL Golfer is null")
                }
            }
        }
        
        viewModelScope.launch {
            sogoGolfer.collect { sogo ->
                if (sogo != null) {
                    android.util.Log.d("PlayingPartnerVM", "=== SOGO GOLFER LOADED ===")
                    android.util.Log.d("PlayingPartnerVM", "SogoGolfer: ${sogo.firstName} ${sogo.lastName}")
                    android.util.Log.d("PlayingPartnerVM", "Token Balance: ${sogo.tokenBalance}")
                    android.util.Log.d("PlayingPartnerVM", "Entity ID: ${sogo.entityId}")
                } else {
                    android.util.Log.d("PlayingPartnerVM", "⚠️ Sogo Golfer is null")
                }
            }
        }
        
        viewModelScope.launch {
            localGame.collect { game ->
                if (game != null) {
                    android.util.Log.d("PlayingPartnerVM", "=== GAME DATA LOADED ===")
                    android.util.Log.d("PlayingPartnerVM", "Game: ${game.bookingTime}, Partners: ${game.playingPartners.size}")
                } else {
                    android.util.Log.d("PlayingPartnerVM", "⚠️ Game data is null")
                }
            }
        }
        
        viewModelScope.launch {
            localCompetition.collect { competition ->
                if (competition != null) {
                    android.util.Log.d("PlayingPartnerVM", "=== COMPETITION DATA LOADED ===")
                    android.util.Log.d("PlayingPartnerVM", "Competition: ${competition.players.size} players")
                } else {
                    android.util.Log.d("PlayingPartnerVM", "⚠️ Competition data is null")
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
            if (!networkChecker.isNetworkAvailable()) {
                // Start with loading state to acknowledge the gesture
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                // Give the UI time to register the refresh gesture
                kotlinx.coroutines.delay(100) // Small delay

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No internet connection"
                )

                return false
            }

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

                // Step 1: Verify we have required data from Room
                val currentGolferData = currentGolfer.value
                val gameData = localGame.value
                val sogoGolferData = sogoGolfer.value
                val competitionData = localCompetition.value

                android.util.Log.d("PlayingPartnerVM", "🔍 Checking Room data availability...")
                android.util.Log.d("PlayingPartnerVM", "Current golfer: ${currentGolferData?.firstName} ${currentGolferData?.surname}")
                android.util.Log.d("PlayingPartnerVM", "Game data: ${gameData?.bookingTime}")
                android.util.Log.d("PlayingPartnerVM", "Sogo golfer: ${sogoGolferData?.firstName} (tokens: ${sogoGolferData?.tokenBalance})")
                android.util.Log.d("PlayingPartnerVM", "Competition data: ${competitionData?.players?.size ?: 0} players")

                if (currentGolferData == null) {
                    _uiState.value = _uiState.value.copy(
                        isLetsPlayLoading = false,
                        errorMessage = "Current golfer data not available. Please refresh the app."
                    )
                    return@launch
                }

                if (gameData == null) {
                    _uiState.value = _uiState.value.copy(
                        isLetsPlayLoading = false,
                        errorMessage = "Game data not available. Please refresh the app."
                    )
                    return@launch
                }

                if (sogoGolferData == null) {
                    _uiState.value = _uiState.value.copy(
                        isLetsPlayLoading = false,
                        errorMessage = "Sogo golfer data not available. Please refresh the app."
                    )
                    return@launch
                }

                // Step 2: Call PUT marker API
                android.util.Log.d("PlayingPartnerVM", "🔄 Step 2: Calling PUT marker API...")
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

                val selectedClub = getMslClubAndTenantIdsUseCase()
                var clubIdStr = ""
                if (selectedClub?.clubId != null) {
                    clubIdStr = selectedClub.clubId.toString()
                }

                //now we need to refetch the competition since the selected marker data will be there
                android.util.Log.d("PlayingPartnerVM", "🔄 Step 3: Refreshing competition data...")
                when (val competitionResult = fetchAndSaveCompetitionUseCase(clubIdStr)) {
                    is NetworkResult.Success -> {
                        android.util.Log.d("PlayingPartnerVM", "✅ Competition data refreshed successfully")
                    }
                    is NetworkResult.Error -> {
                        android.util.Log.w("PlayingPartnerVM", "⚠️ Failed to refresh competition data: ${competitionResult.error}")
                        _uiState.value = _uiState.value.copy(
                            isLetsPlayLoading = false,
                            errorMessage = "Failed to re-fetch msl competition data: ${competitionResult.error.toUserMessage()}"
                        )
                        return@launch
                    }
                    is NetworkResult.Loading -> { /* Ignore */ }
                }

                //we should also refetch the game since it has who is marking who data which we will need in playroundviewmodel
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
                        return@launch
                    }
                    is NetworkResult.Loading -> { /* Ignore */ }
                }


                // Step 3: Get fresh competition data from Room after refetch
                android.util.Log.d("PlayingPartnerVM", "🔄 Step 3: Getting fresh competition data from Room...")
                val freshCompetitionData = localCompetition.value
                android.util.Log.d("PlayingPartnerVM", "Fresh competition data: ${freshCompetitionData?.players?.size ?: 0} players")

                // Step 4: Handle fee charging with duplicate prevention
                android.util.Log.d("PlayingPartnerVM", "🔄 Step 4: Checking for duplicate transactions and handling fees...")
                val includeRoundValue = _includeRound.value
                val currentTokenCost = tokenCost.value
                
                if (includeRoundValue && currentTokenCost > 0) {
                    android.util.Log.d("PlayingPartnerVM", "💰 Fee required: $currentTokenCost tokens")
                    
                    // Get mainCompetitionId from game data
                    val mainCompetitionId = gameData.mainCompetitionId
                    if (mainCompetitionId == null) {
                        android.util.Log.e("PlayingPartnerVM", "❌ No mainCompetitionId found in game data")
                        _uiState.value = _uiState.value.copy(
                            isLetsPlayLoading = false,
                            errorMessage = "Game data missing competition ID. Please refresh and try again."
                        )
                        return@launch
                    }
                    
                    // Check for existing transactions
                    android.util.Log.d("PlayingPartnerVM", "🔍 Checking for existing transactions for golfer ${sogoGolferData.id}, competition $mainCompetitionId")
                    checkExistingTransactionUseCase(sogoGolferData.id, mainCompetitionId).fold(
                        onSuccess = { hasExistingTransaction ->
                            if (hasExistingTransaction) {
                                android.util.Log.d("PlayingPartnerVM", "✅ Existing transaction found - skipping fee charge")
                            } else {
                                android.util.Log.d("PlayingPartnerVM", "💳 No existing transaction - charging fee")
                                
                                // Check sufficient balance
                                if (sogoGolferData.tokenBalance < currentTokenCost) {
                                    android.util.Log.e("PlayingPartnerVM", "❌ Insufficient token balance: ${sogoGolferData.tokenBalance} < $currentTokenCost")
                                    _uiState.value = _uiState.value.copy(
                                        isLetsPlayLoading = false,
                                        errorMessage = "Insufficient token balance. Please purchase more tokens."
                                    )
                                    return@launch
                                }
                                
                                // Create transaction
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
                                        android.util.Log.d("PlayingPartnerVM", "✅ Transaction created successfully")
                                        
                                        // Update token balance locally and on server
                                        val newBalance = sogoGolferData.tokenBalance - currentTokenCost.toInt()
                                        android.util.Log.d("PlayingPartnerVM", "💰 Updating token balance: ${sogoGolferData.tokenBalance} -> $newBalance")
                                        
                                        viewModelScope.launch {
                                            updateTokenBalanceUseCase(newBalance, sogoGolferData).fold(
                                                onSuccess = { updatedGolfer ->
                                                    android.util.Log.d("PlayingPartnerVM", "✅ Token balance updated successfully to ${updatedGolfer.tokenBalance}")
                                                },
                                                onFailure = { balanceError ->
                                                    android.util.Log.e("PlayingPartnerVM", "❌ Failed to update token balance: ${balanceError.message}")
                                                    // Continue with round creation even if balance update fails
                                                    // The transaction was already created successfully
                                                }
                                            )
                                        }
                                    },
                                    onFailure = { error ->
                                        android.util.Log.e("PlayingPartnerVM", "❌ Failed to create transaction: ${error.message}")
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
                            android.util.Log.e("PlayingPartnerVM", "❌ Failed to check existing transactions: ${error.message}")
                            _uiState.value = _uiState.value.copy(
                                isLetsPlayLoading = false,
                                errorMessage = "Failed to verify payment status: ${error.message}"
                            )
                            return@launch
                        }
                    )
                } else {
                    android.util.Log.d("PlayingPartnerVM", "ℹ️ No fee required (include round: $includeRoundValue, cost: $currentTokenCost)")
                }

                // Step 5: Create Round object using fresh Room data
                android.util.Log.d("PlayingPartnerVM", "🔄 Step 5: Creating Round object from fresh Room data...")
                val round = createRoundFromRoomData(selectedPartner, currentGolferData, gameData, sogoGolferData, freshCompetitionData, selectedClub)

                // Step 6: Save Round to Room
                android.util.Log.d("PlayingPartnerVM", "🔄 Step 6: Saving Round to database...")
                roundRepository.saveRound(round)
                android.util.Log.d("PlayingPartnerVM", "✅ Round saved to database")

                // Step 7: Save Round to MongoDB API
                android.util.Log.d("PlayingPartnerVM", "🔄 Step 7: Syncing Round to MongoDB...")
                when (val createRoundResult = createRoundUseCase(round)) {
                    is NetworkResult.Success -> {
                        android.util.Log.d("PlayingPartnerVM", "✅ Round synced to MongoDB successfully")
                        val syncedRound = round.copy(isSynced = true)
                        roundRepository.saveRound(syncedRound)
                    }
                    is NetworkResult.Error -> {
                        android.util.Log.w("PlayingPartnerVM", "⚠️ Failed to sync round to MongoDB: ${createRoundResult.error}")
                    }
                    is NetworkResult.Loading -> { /* Ignore */ }
                }

                _uiState.value = _uiState.value.copy(
                    isLetsPlayLoading = false,
                    successMessage = "Ready to play!"
                )

                // Step 8: Navigate to PlayRound screen
                android.util.Log.d("PlayingPartnerVM", "🔄 Step 8: Navigating to PlayRound screen...")
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

    private fun createRoundFromRoomData(
        selectedPartner: MslPlayingPartner,
        currentGolferData: com.sogo.golf.msl.domain.model.msl.MslGolfer,
        gameData: MslGame,
        sogoGolferData: SogoGolfer,
        competitionData: MslCompetition?,
        selectedClub: SelectedClub?
    ): Round {
        val includeRoundValue = _includeRound.value

        android.util.Log.d("PlayingPartnerVM", "📝 Creating Round with data:")
        android.util.Log.d("PlayingPartnerVM", "  - Golfer: ${currentGolferData.firstName} ${currentGolferData.surname}")
        android.util.Log.d("PlayingPartnerVM", "  - Game: ${gameData.bookingTime}")
        android.util.Log.d("PlayingPartnerVM", "  - Sogo tokens: ${sogoGolferData.tokenBalance}")
        android.util.Log.d("PlayingPartnerVM", "  - Competition: ${competitionData?.players?.size ?: 0} players")
        android.util.Log.d("PlayingPartnerVM", "  - Include round: $includeRoundValue")

        val golfer = competitionData?.players?.find { it.golfLinkNumber == currentGolferData.golfLinkNo }

        val playingPartnerRound = createPlayingPartnerRound(
            selectedPartner = selectedPartner,
            gameData = gameData,
            competitionData = competitionData,
            sogoGolferData = sogoGolferData
        )

        val holeScores = createHoleScores(competitionData)

        return Round(
            id = ObjectIdUtils.generateObjectId(),
            uuid = UUID.randomUUID().toString(),
            entityId = sogoGolferData.entityId,
            roundPlayedOff = gameData.gaHandicap,
            dailyHandicap = gameData.dailyHandicap?.toDouble(),
            golfLinkHandicap = gameData.gaHandicap,
            golflinkNo = currentGolferData.golfLinkNo,
            roundDate = gameData.bookingTime?.toLocalDate()?.atStartOfDay(),
            startTime = gameData.bookingTime,
            finishTime = null,
            scratchRating = golfer?.scratchRating?.toFloat(),
            slopeRating = golfer?.slopeRating?.toFloat(),
            submittedTime = null,
            compScoreTotal = 0,
            roundType = "competition",
            clubId = null,  //////////////////////////////////////
            clubName = selectedClub?.clubName,
            golferId = sogoGolferData.id,
            golferFirstName = sogoGolferData.firstName,
            golferLastName = sogoGolferData.lastName,
            golferGLNumber = sogoGolferData.golfLinkNo,
            markerFirstName = selectedPartner.firstName,
            markerLastName = selectedPartner.lastName,
            markerGLNumber = selectedPartner.golfLinkNumber,
            compType = gameData.competitions.firstOrNull()?.name?.lowercase(Locale.ROOT),
            teeColor = gameData.teeColourName?.lowercase(),
            isClubComp = true,
            isSubmitted = false,
            isApproved = false,
            holeScores = holeScores,
            playingPartnerRound = playingPartnerRound,
            mslMetaData = MslMetaData(isIncludeRoundOnSogo = includeRoundValue),
            createdDate = LocalDateTime.now()
        )
    }

    private fun createHoleScores(competitionData: MslCompetition?, specificPlayer: MslPlayer? = null): List<HoleScore> {
        val playerToUse = specificPlayer ?: competitionData?.players?.firstOrNull()
        val holes = playerToUse?.holes ?: emptyList()

        android.util.Log.d("PlayingPartnerVM", "🏌️ Creating hole scores for ${holes.size} holes")
        holes.forEach { hole ->
            android.util.Log.d("PlayingPartnerVM", "  - Hole ${hole.holeNumber}: par=${hole.par}, distance=${hole.distance}")
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
        android.util.Log.d("PlayingPartnerVM", "📝 Creating PlayingPartnerRound for: ${selectedPartner.firstName} ${selectedPartner.lastName}")

        //val golferGender = competitionData?.players?.find { it.golfLinkNumber == selectedPartner.golfLinkNumber }

        val partnerGolfer = competitionData?.players?.find { it.golfLinkNumber == selectedPartner.golfLinkNumber }
        val holeScores = createHoleScores(competitionData, partnerGolfer)

        return PlayingPartnerRound(
            uuid = null,
            entityId = sogoGolferData.entityId,
            dailyHandicap = selectedPartner.dailyHandicap.toFloat(),
            golfLinkHandicap = selectedPartner.dailyHandicap.toFloat(),
            roundDate = gameData.bookingTime?.toLocalDate()?.atStartOfDay(),
            roundType = "competition",
            startTime = gameData.bookingTime,
            finishTime = null,
            submittedTime = null,
            scratchRating = partnerGolfer?.scratchRating?.toFloat(),
            slopeRating = partnerGolfer?.slopeRating?.toFloat(),
            compScoreTotal = 0,
            teeColor = gameData.teeColourName?.lowercase(),
            compType = gameData.competitions.firstOrNull()?.name?.lowercase(Locale.ROOT),
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
            createdDate = LocalDateTime.now(),
            updateDate = null,
            deleteDate = null
        )
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
    val isLetsPlayLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
