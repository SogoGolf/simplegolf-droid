package com.sogo.golf.msl.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.usecase.game.GetGameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getGameUseCase: GetGameUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun getGame(clubId: String = "670229") { // Default game ID for testing
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingGame = true,
                gameErrorMessage = null,
                gameSuccessMessage = null
            )

            when (val result = getGameUseCase(clubId)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingGame = false,
                        gameData = result.data,
                        gameSuccessMessage = "Game data loaded successfully!"
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingGame = false,
                        gameErrorMessage = result.error.toUserMessage()
                    )
                }
                is NetworkResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            gameErrorMessage = null,
            gameSuccessMessage = null
        )
    }
}