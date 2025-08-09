package com.sogo.golf.msl.features.sogo_home.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.mongodb.RoundSummary
import com.sogo.golf.msl.domain.model.mongodb.SogoGolfer
import com.sogo.golf.msl.domain.usecase.game.GetLocalGameUseCase
import com.sogo.golf.msl.domain.usecase.msl_golfer.GetMslGolferUseCase
import com.sogo.golf.msl.domain.usecase.round.GetRoundsSummaryUseCase
import com.sogo.golf.msl.domain.usecase.sogo_golfer.GetSogoGolferUseCase
import com.sogo.golf.msl.domain.usecase.sogo_golfer.UpdateTokenBalanceUseCase
import com.sogo.golf.msl.domain.usecase.transaction.CreateTransactionUseCase
import com.revenuecat.purchases.models.StoreTransaction
import com.sogo.golf.msl.shared.utils.ObjectIdUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SogoGolfHomeViewModel @Inject constructor(
    private val getLocalGameUseCase: GetLocalGameUseCase,
    private val getSogoGolferUseCase: GetSogoGolferUseCase,
    private val getMslGolferUseCase: GetMslGolferUseCase,
    private val createTransactionUseCase: CreateTransactionUseCase,
    private val updateTokenBalanceUseCase: UpdateTokenBalanceUseCase,
    private val getRoundsSummaryUseCase: GetRoundsSummaryUseCase,
) : ViewModel() {

    companion object {
        private const val TAG = "SogoGolfHomeViewModel"
    }

    private val _uiState = MutableStateFlow(SogoGolfHomeUiState())
    val uiState: StateFlow<SogoGolfHomeUiState> = _uiState.asStateFlow()

    private val _purchaseTokenState = MutableStateFlow(PurchaseTokenState())
    val purchaseTokenState: StateFlow<PurchaseTokenState> = _purchaseTokenState.asStateFlow()
    
    private val _roundsSummaryState = MutableStateFlow(RoundsSummaryState())
    val roundsSummaryState: StateFlow<RoundsSummaryState> = _roundsSummaryState.asStateFlow()

    val currentGolfer = getMslGolferUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val localGame = getLocalGameUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val sogoGolfer = kotlinx.coroutines.flow.combine(currentGolfer, localGame) { golfer, game ->
        Log.d(TAG, "=== SOGO GOLFER FLOW DEBUG ===")
        Log.d(TAG, "golfer: $golfer")
        Log.d(TAG, "golfer.golfLinkNo: ${golfer?.golfLinkNo}")
        Log.d(TAG, "game: $game")
        Log.d(TAG, "game.golflinkNumber: ${game?.golflinkNumber}")
        
        // Use game data as fallback if golfer's golfLinkNo is empty
        val golfLinkNo = golfer?.golfLinkNo?.takeIf { it.isNotBlank() }
            ?: game?.golflinkNumber
        
        Log.d(TAG, "Final golfLinkNo to use: $golfLinkNo")
        golfLinkNo
    }.flatMapLatest { golfLinkNo ->
        if (!golfLinkNo.isNullOrBlank()) {
            Log.d(TAG, "Calling getSogoGolferUseCase with golfLinkNo: $golfLinkNo")
            getSogoGolferUseCase(golfLinkNo)
        } else {
            Log.d(TAG, "No valid golfLinkNo available - returning null")
            flowOf(null)
        }
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    init {
        initializeGolferData()
    }

    private fun initializeGolferData() {
        viewModelScope.launch {
            try {
                currentGolfer.collect { golfer ->
                    if (golfer != null) {
                        Log.d(TAG, "MSL Golfer loaded: ${golfer.firstName} ${golfer.surname}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing golfer data", e)
            }
        }
    }

    fun trackSogoTandCItemClicked(itemType: String) {
        Log.d(TAG, "SOGO T&C item clicked: $itemType")
    }

    fun trackLeaderboardsButtonClicked() {
        Log.d(TAG, "Leaderboards button clicked")
    }

    fun trackRoundsButtonClicked() {
        Log.d(TAG, "Rounds button clicked")
    }

    fun trackPurchaseTokensButtonClicked() {
        Log.d(TAG, "Purchase tokens button clicked")
    }

    fun trackAboutSogoButtonClicked() {
        Log.d(TAG, "About SOGO button clicked")
    }

    fun trackTokensAccountButtonClicked() {
        Log.d(TAG, "Tokens account button clicked")
    }

    suspend fun updateTokenBalance(transaction: StoreTransaction) {
        try {
            _purchaseTokenState.value = _purchaseTokenState.value.copy(isLoading = true)

            val currentBalance = sogoGolfer.value?.tokenBalance ?: 0

            val result = updateTokenBalanceUseCase(currentBalance, transaction, sogoGolfer.value)

            result.fold(
                onSuccess = { updatedGolfer ->
                    _purchaseTokenState.value = _purchaseTokenState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )

                    createTransaction(transaction)

                    Log.d(TAG, "Token balance updated successfully to ${updatedGolfer.tokenBalance}")
                },
                onFailure = { error ->
                    _purchaseTokenState.value = _purchaseTokenState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to update token balance: ${error.message}"
                    )
                    Log.e(TAG, "Failed to update token balance", error)
                }
            )
        } catch (e: Exception) {
            _purchaseTokenState.value = _purchaseTokenState.value.copy(
                isLoading = false,
                errorMessage = "Failed to update token balance: ${e.message}"
            )
            Log.e(TAG, "Exception updating token balance", e)
        }
    }

    enum class TransactionType {
        PURCHASE, ON_SIGN_UP, PLAY_ROUND // Add other transaction types as needed
    }

    enum class DebitCreditType {
        DEBIT, CREDIT // Add other debit/credit types as needed
    }

    suspend fun createTransaction(transaction: StoreTransaction) {

        val sogoGolferData = sogoGolfer.value ?: throw Exception("SogoGolfer is null")

        val transactionId = ObjectIdUtils.generateObjectId()

        val packageid = transaction.productIds.firstOrNull() ?: throw Exception("Package ID not found")

        //for now just hardcode the products to get the amount of the transaction
        val numTokens = when (packageid) {
            "com.sogo.golf.msl.5token" -> 5
            "com.sogo.golf.msl.10token" -> 10
            "com.sogo.golf.msl.20token" -> 20
            else -> {0}
        }

        createTransactionUseCase(
            tokens = numTokens,
            entityIdVal = sogoGolferData.entityId,
            transId = transactionId,
            sogoGolfer = sogoGolferData,
            transactionTypeVal = TransactionType.PURCHASE.toString(),
            debitCreditTypeVal = DebitCreditType.CREDIT.toString(),
            commentVal = "Purchase tokens",
            statusVal = "completed",
            mainCompetitionId = null
        ).fold(
            onSuccess = {
                android.util.Log.d("PlayingPartnerVM", "✅ Transaction created successfully")
            },
            onFailure = { error ->
                android.util.Log.e("PlayingPartnerVM", "❌ Failed to create transaction: ${error.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to process payment: ${error.message}"
                )
                return
            }
        )
    }



    fun clearPurchaseTokenState() {
        _purchaseTokenState.value = PurchaseTokenState()
    }

    fun setPurchaseTokensState(isInProgress: Boolean) {
        _purchaseTokenState.value = _purchaseTokenState.value.copy(isLoading = isInProgress)
    }
    
    fun fetchRoundsSummary() {
        viewModelScope.launch {
            Log.d(TAG, "=== FETCHING ROUNDS SUMMARY ===")
            
            // Set loading state
            _roundsSummaryState.value = _roundsSummaryState.value.copy(
                isLoading = true,
                error = null
            )
            
            // Get the golf link number from current golfer or game data
            val golfLinkNo = currentGolfer.value?.golfLinkNo?.takeIf { it.isNotBlank() }
                ?: localGame.value?.golflinkNumber
            
            if (golfLinkNo.isNullOrBlank()) {
                Log.e(TAG, "❌ Cannot fetch rounds summary: No golf link number available")
                _roundsSummaryState.value = _roundsSummaryState.value.copy(
                    isLoading = false,
                    error = "No golf link number available"
                )
                return@launch
            }
            
            Log.d(TAG, "Fetching rounds for golfLinkNo: $golfLinkNo")
            
            when (val result = getRoundsSummaryUseCase(golfLinkNo)) {
                is NetworkResult.Success -> {
                    val summaries = result.data
                    Log.d(TAG, "✅ Successfully fetched ${summaries.size} round summaries")
                    
                    // Sort rounds by date, most recent first
                    val sortedSummaries = summaries.sortedByDescending { it.roundDate }
                    
                    // Update state with fetched and sorted rounds
                    _roundsSummaryState.value = _roundsSummaryState.value.copy(
                        isLoading = false,
                        rounds = sortedSummaries,
                        error = null
                    )
                    
                    // Log details for debugging
                    sortedSummaries.forEachIndexed { index, summary ->
                        Log.d(TAG, "Round $index:")
                        Log.d(TAG, "  - Date: ${summary.roundDate}")
                        Log.d(TAG, "  - Golfer: ${summary.golferFirstName} ${summary.golferLastName}")
                        Log.d(TAG, "  - Club: ${summary.clubName}")
                        Log.d(TAG, "  - Score: ${summary.score}")
                        Log.d(TAG, "  - Holes: ${summary.countOfHoleScores}")
                        Log.d(TAG, "  - CompType: ${summary.compType}")
                        Log.d(TAG, "  - Handicap: ${summary.golfLinkHandicap}")
                        Log.d(TAG, "  - Scratch Rating: ${summary.scratchRating}")
                        Log.d(TAG, "  - Slope Rating: ${summary.slopeRating}")
                    }
                }
                is NetworkResult.Error -> {
                    val errorMessage = result.error.toUserMessage()
                    Log.e(TAG, "❌ Failed to fetch rounds summary: $errorMessage")
                    _roundsSummaryState.value = _roundsSummaryState.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
                is NetworkResult.Loading -> {
                    Log.d(TAG, "⏳ Loading rounds summary...")
                }
            }
        }
    }
}

data class SogoGolfHomeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class PurchaseTokenState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

data class RoundsSummaryState(
    val isLoading: Boolean = false,
    val rounds: List<RoundSummary> = emptyList(),
    val error: String? = null
)
