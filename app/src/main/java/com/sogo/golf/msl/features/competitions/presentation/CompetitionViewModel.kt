package com.sogo.golf.msl.features.competitions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.repository.MslCompetitionLocalDbRepository
import com.sogo.golf.msl.domain.repository.MslGameLocalDbRepository
import com.sogo.golf.msl.domain.usecase.msl_golfer.GetMslGolferUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompetitionViewModel @Inject constructor(
    private val competitionRepository: MslCompetitionLocalDbRepository,
    private val getMslGolferUseCase: GetMslGolferUseCase,
    private val gameRepository: MslGameLocalDbRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompetitionUiState())
    val uiState: StateFlow<CompetitionUiState> = _uiState.asStateFlow()

    // Always observe local competition data (works offline)
    val currentCompetition = competitionRepository.getCompetition()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val localGame = gameRepository.getGame()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )


    // ✅ ADD GOLFER STATEFLOW
    val currentGolfer = getMslGolferUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val allCompetitions = competitionRepository.getAllCompetitions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Example method: Fetch competition data from server
    fun fetchCompetitionData(competitionId: String = "default-comp-2024") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            when (val result = competitionRepository.fetchAndSaveCompetition(competitionId)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Competition data updated! Found ${result.data.players.size} players."
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.error.toUserMessage()
                    )
                }
                is NetworkResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    // Example method: Refresh current competition
    fun refreshCompetition() {
        val competitionId = "weekend-tournament-2024"
        fetchCompetitionData(competitionId)
    }

    // Example method: Clear all competitions (useful for testing/logout)
    fun clearAllCompetitions() {
        viewModelScope.launch {
            competitionRepository.clearAllCompetitions()
            _uiState.value = _uiState.value.copy(
                successMessage = "All competitions cleared"
            )
        }
    }

    // Example method: Get competition summary
    fun getCompetitionSummary(): String {
        val competition = currentCompetition.value
        return when {
            competition == null -> "No competition data available"
            competition.players.isEmpty() -> "Competition loaded but no players found"
            else -> {
                val playerCount = competition.players.size
                val competitionName = competition.players.firstOrNull()?.competitionName ?: "Unknown"
                val competitionType = competition.players.firstOrNull()?.competitionType ?: "Unknown"
                "Competition: $competitionName ($competitionType) with $playerCount players"
            }
        }
    }

    // Example method: Get player names
    fun getPlayerNames(): List<String> {
        return currentCompetition.value?.players?.map { player ->
            "${player.firstName ?: ""} ${player.lastName ?: ""}".trim()
        } ?: emptyList()
    }

    // Clear messages
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    // Debug function to check what's in the database
    fun debugDatabaseContents() {
        viewModelScope.launch {
            try {
                val unsynced = competitionRepository.getUnsyncedCompetitions()
                android.util.Log.d("CompetitionViewModel", "Unsynced competitions: ${unsynced.size}")
                unsynced.forEachIndexed { index, comp ->
                    android.util.Log.d("CompetitionViewModel", "Competition $index: ${comp.players.size} players")
                }

                _uiState.value = _uiState.value.copy(
                    successMessage = "Database contains ${unsynced.size} competitions"
                )
            } catch (e: Exception) {
                android.util.Log.e("CompetitionViewModel", "Error checking database", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error checking database: ${e.message}"
                )
            }
        }
    }
}