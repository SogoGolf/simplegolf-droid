package com.sogo.golf.msl.features.sogo_home.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.mongodb.Competition
import com.sogo.golf.msl.domain.usecase.competition.GetCompetitionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NationalLeaderboardsViewModel @Inject constructor(
    private val getCompetitionsUseCase: GetCompetitionsUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "NationalLeaderboardsVM"
    }

    private val _uiState = MutableStateFlow(NationalLeaderboardsUiState())
    val uiState: StateFlow<NationalLeaderboardsUiState> = _uiState.asStateFlow()

    fun fetchCompetitions() {
        viewModelScope.launch {
            Log.d(TAG, "Fetching competitions")
            
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            when (val result = getCompetitionsUseCase()) {
                is NetworkResult.Success -> {
                    val competitions = result.data
                    Log.d(TAG, "Successfully fetched ${competitions.size} competitions")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        competitions = competitions,
                        error = null
                    )
                }
                is NetworkResult.Error -> {
                    val errorMessage = result.error.toUserMessage()
                    Log.e(TAG, "Failed to fetch competitions: $errorMessage")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
                is NetworkResult.Loading -> {
                    Log.d(TAG, "Loading competitions...")
                }
            }
        }
    }
}

data class NationalLeaderboardsUiState(
    val isLoading: Boolean = false,
    val competitions: List<Competition> = emptyList(),
    val error: String? = null
)