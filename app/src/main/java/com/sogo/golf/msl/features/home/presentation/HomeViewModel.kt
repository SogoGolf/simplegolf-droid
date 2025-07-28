// app/src/main/java/com/sogo/golf/msl/features/home/presentation/HomeViewModel.kt
package com.sogo.golf.msl.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sogo.golf.msl.domain.usecase.competition.GetLocalCompetitionUseCase
import com.sogo.golf.msl.domain.usecase.game.GetLocalGameUseCase
import com.sogo.golf.msl.domain.usecase.msl_golfer.GetMslGolferUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val getMslGolferUseCase: GetMslGolferUseCase,
    private val getLocalGameUseCase: GetLocalGameUseCase,
    private val getLocalCompetitionUseCase: GetLocalCompetitionUseCase
) : ViewModel() {

    // ✅ GLOBAL GOLFER ACCESS - Available everywhere!
    val currentGolfer = getMslGolferUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // ✅ GLOBAL GAME ACCESS FROM LOCAL DATABASE
    val localGame = getLocalGameUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // StateFlow for local competition data
    val localCompetition = getLocalCompetitionUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Example method showing how to use golfer data
    fun getGolferSummary(): String {
        val golfer = currentGolfer.value
        return when {
            golfer == null -> "No golfer data available"
            else -> "Welcome ${golfer.firstName} ${golfer.surname} (Handicap: ${golfer.primary})"
        }
    }

    // Example method showing how to use competition data
    fun getCompetitionSummary(): String {
        val competition = localCompetition.value
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

    // Example method showing how to use game data
    fun getGameSummary(): String {
        val game = localGame.value
        return when {
            game == null -> "No game data available"
            else -> {
                val partnersCount = game.playingPartners.size
                val competitionsCount = game.competitions.size
                "Game: Competition ${game.mainCompetitionId}, Hole ${game.startingHoleNumber}, $partnersCount partners, $competitionsCount competitions"
            }
        }
    }
}