package com.sogo.golf.msl.features.home.presentation

import com.sogo.golf.msl.domain.model.msl.MslGame

data class HomeUiState(
    val isLoadingGame: Boolean = false,
    val gameData: MslGame? = null,
    val gameErrorMessage: String? = null,
    val gameSuccessMessage: String? = null
)