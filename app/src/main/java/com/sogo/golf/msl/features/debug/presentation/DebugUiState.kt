package com.sogo.golf.msl.features.debug.presentation

import com.sogo.golf.msl.domain.model.mongodb.Fee
import com.sogo.golf.msl.domain.model.msl.MslCompetition
import com.sogo.golf.msl.domain.model.msl.MslGame

data class DebugUiState(
    val isLoadingGame: Boolean = false,
    val gameData: MslGame? = null,
    val gameErrorMessage: String? = null,
    val gameSuccessMessage: String? = null,

    // Competition state
    val isLoadingCompetition: Boolean = false,
    val competitionData: MslCompetition? = null,
    val competitionErrorMessage: String? = null,
    val competitionSuccessMessage: String? = null,

    // Fees state
    val isLoadingFees: Boolean = false,
    val feesData: List<Fee> = emptyList(),
    val feesErrorMessage: String? = null,
    val feesSuccessMessage: String? = null
)