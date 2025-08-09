package com.sogo.golf.msl.features.sogo_home.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.mongodb.RoundDetail
import com.sogo.golf.msl.domain.usecase.round.GetRoundDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoundDetailViewModel @Inject constructor(
    private val getRoundDetailUseCase: GetRoundDetailUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "RoundDetailViewModel"
    }

    private val _uiState = MutableStateFlow(RoundDetailUiState())
    val uiState: StateFlow<RoundDetailUiState> = _uiState.asStateFlow()

    fun fetchRoundDetail(id: String) {
        viewModelScope.launch {
            Log.d(TAG, "Fetching round detail for id: $id")
            
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            when (val result = getRoundDetailUseCase(id)) {
                is NetworkResult.Success -> {
                    val roundDetail = result.data
                    Log.d(TAG, "Successfully fetched round detail with ${roundDetail.holeScores.size} hole scores")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        roundDetail = roundDetail,
                        error = null
                    )
                }
                is NetworkResult.Error -> {
                    val errorMessage = result.error.toUserMessage()
                    Log.e(TAG, "Failed to fetch round detail: $errorMessage")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
                is NetworkResult.Loading -> {
                    Log.d(TAG, "Loading round detail...")
                }
            }
        }
    }
}

data class RoundDetailUiState(
    val isLoading: Boolean = false,
    val roundDetail: RoundDetail? = null,
    val error: String? = null
)