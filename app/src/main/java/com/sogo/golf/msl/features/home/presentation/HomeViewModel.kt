package com.sogo.golf.msl.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.repository.MslGolferLocalDbRepository
import com.sogo.golf.msl.domain.usecase.game.GetGameUseCase
import com.sogo.golf.msl.domain.usecase.msl_golfer.GetMslGolferUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getGameUseCase: GetGameUseCase,
    val getMslGolferUseCase: GetMslGolferUseCase,
    private val mslGolferLocalDbRepository: MslGolferLocalDbRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // ✅ GLOBAL GOLFER ACCESS - Available everywhere!
    val currentGolfer = getMslGolferUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )


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

    // ✅ TEST METHOD: Get golfer from LOCAL DATABASE ONLY
    fun getGolferFromLocalDatabase() {
        viewModelScope.launch {
            try {
                android.util.Log.d("HomeViewModel", "=== TESTING LOCAL DATABASE GOLFER ===")

                // Check if we have any golfer in database
                val hasGolfer = mslGolferLocalDbRepository.hasGolfer()
                android.util.Log.d("HomeViewModel", "Has golfer in database: $hasGolfer")

                if (!hasGolfer) {
                    android.util.Log.w("HomeViewModel", "❌ No golfer found in local database!")
                    _uiState.value = _uiState.value.copy(
                        gameErrorMessage = "No golfer found in local database. Please login first."
                    )
                    return@launch
                }

                // Get golfer from local database
                val golferFromDb = mslGolferLocalDbRepository.getCurrentGolfer().first()

                if (golferFromDb != null) {
                    android.util.Log.d("HomeViewModel", "✅ SUCCESS: Retrieved golfer from LOCAL DATABASE:")
                    android.util.Log.d("HomeViewModel", "  First Name: ${golferFromDb.firstName}")
                    android.util.Log.d("HomeViewModel", "  Last Name: ${golferFromDb.surname}")
                    android.util.Log.d("HomeViewModel", "  Golf Link No: ${golferFromDb.golfLinkNo}")
                    android.util.Log.d("HomeViewModel", "  Email: ${golferFromDb.email}")
                    android.util.Log.d("HomeViewModel", "  Handicap: ${golferFromDb.primary}")
                    android.util.Log.d("HomeViewModel", "  Country: ${golferFromDb.country}")
                    android.util.Log.d("HomeViewModel", "  Date of Birth: ${golferFromDb.dateOfBirth}")
                    android.util.Log.d("HomeViewModel", "  Mobile: ${golferFromDb.mobileNo}")
                    android.util.Log.d("HomeViewModel", "  State: ${golferFromDb.state}")
                    android.util.Log.d("HomeViewModel", "  Post Code: ${golferFromDb.postCode}")
                    android.util.Log.d("HomeViewModel", "  Gender: ${golferFromDb.gender}")

                    _uiState.value = _uiState.value.copy(
                        gameSuccessMessage = "✅ Golfer retrieved from LOCAL DATABASE: ${golferFromDb.firstName} ${golferFromDb.surname} (Check logs for details)"
                    )
                } else {
                    android.util.Log.w("HomeViewModel", "❌ Golfer exists in database but returned null from getCurrentGolfer()")
                    _uiState.value = _uiState.value.copy(
                        gameErrorMessage = "Golfer exists but returned null. Check database setup."
                    )
                }

            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "❌ ERROR retrieving golfer from local database", e)
                _uiState.value = _uiState.value.copy(
                    gameErrorMessage = "Error retrieving golfer from local database: ${e.message}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            gameErrorMessage = null,
            gameSuccessMessage = null
        )
    }

    // Example method showing how to use golfer data
    fun getGolferSummary(): String {
        val golfer = currentGolfer.value
        return when {
            golfer == null -> "No golfer data available"
            else -> "Welcome ${golfer.firstName} ${golfer.surname} (Handicap: ${golfer.primary})"
        }
    }
}