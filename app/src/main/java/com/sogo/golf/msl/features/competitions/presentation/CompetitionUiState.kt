package com.sogo.golf.msl.features.competitions.presentation

data class CompetitionUiState(
    val isLoading: Boolean = false,
    val isDataFetching: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
