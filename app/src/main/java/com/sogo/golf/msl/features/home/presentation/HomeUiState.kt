package com.sogo.golf.msl.features.home.presentation

import com.sogo.golf.msl.domain.model.msl.MslCompetition
import com.sogo.golf.msl.domain.model.msl.MslGame

data class HomeUiState(
    val isLoadingGame: Boolean = false,
    val gameData: MslGame? = null,
    val gameErrorMessage: String? = null,
    val gameSuccessMessage: String? = null,

    // NEW: Competition state
    val isLoadingCompetition: Boolean = false,
    val competitionData: MslCompetition? = null,
    val competitionErrorMessage: String? = null,
    val competitionSuccessMessage: String? = null
)