// app/src/main/java/com/sogo/golf/msl/features/home/presentation/HomeUiState.kt
package com.sogo.golf.msl.features.home.presentation

// Simplified HomeUiState - debug functionality moved to DebugUiState
data class HomeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)