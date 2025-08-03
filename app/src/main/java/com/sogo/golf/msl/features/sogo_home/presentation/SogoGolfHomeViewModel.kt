package com.sogo.golf.msl.features.sogo_home.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.mongodb.SogoGolfer
import com.sogo.golf.msl.domain.usecase.game.GetGameUseCase
import com.sogo.golf.msl.domain.usecase.msl_golfer.GetMslGolferUseCase
import com.sogo.golf.msl.domain.usecase.sogo_golfer.GetSogoGolferUseCase
import com.sogo.golf.msl.domain.usecase.sogo_golfer.UpdateTokenBalanceUseCase
import com.sogo.golf.msl.domain.usecase.transaction.CreateTransactionUseCase
import com.revenuecat.purchases.models.StoreTransaction
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
    private val getGameUseCase: GetGameUseCase,
    private val getSogoGolferUseCase: GetSogoGolferUseCase,
    private val getMslGolferUseCase: GetMslGolferUseCase,
    private val createTransactionUseCase: CreateTransactionUseCase,
    private val updateTokenBalanceUseCase: UpdateTokenBalanceUseCase,
) : ViewModel() {

    companion object {
        private const val TAG = "SogoGolfHomeViewModel"
    }

    private val _uiState = MutableStateFlow(SogoGolfHomeUiState())
    val uiState: StateFlow<SogoGolfHomeUiState> = _uiState.asStateFlow()

    private val _purchaseTokenState = MutableStateFlow(PurchaseTokenState())
    val purchaseTokenState: StateFlow<PurchaseTokenState> = _purchaseTokenState.asStateFlow()

    val currentGolfer = getMslGolferUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

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

    fun updateTokenBalance(transaction: StoreTransaction) {
        viewModelScope.launch {
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
    }

    fun clearPurchaseTokenState() {
        _purchaseTokenState.value = PurchaseTokenState()
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
