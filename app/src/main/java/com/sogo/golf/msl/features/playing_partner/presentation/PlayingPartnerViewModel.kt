package com.sogo.golf.msl.features.playing_partner.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PlayingPartnerViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PlayingPartnerUiState())
    val uiState: StateFlow<PlayingPartnerUiState> = _uiState.asStateFlow()

    // Minimal state flows to satisfy the screen requirements
    private val _currentGolfer = MutableStateFlow<com.sogo.golf.msl.domain.model.msl.MslGolfer?>(null)
    val currentGolfer: StateFlow<com.sogo.golf.msl.domain.model.msl.MslGolfer?> = _currentGolfer.asStateFlow()

    private val _localGame = MutableStateFlow<com.sogo.golf.msl.domain.model.msl.MslGame?>(null)
    val localGame: StateFlow<com.sogo.golf.msl.domain.model.msl.MslGame?> = _localGame.asStateFlow()

    private val _sogoGolfer = MutableStateFlow<com.sogo.golf.msl.domain.model.mongodb.SogoGolfer?>(null)
    val sogoGolfer: StateFlow<com.sogo.golf.msl.domain.model.mongodb.SogoGolfer?> = _sogoGolfer.asStateFlow()

    private val _tokenCost = MutableStateFlow(0.0)
    val tokenCost: StateFlow<Double> = _tokenCost.asStateFlow()

    private val _canProceed = MutableStateFlow(false)
    val canProceed: StateFlow<Boolean> = _canProceed.asStateFlow()

    private val _includeRound = MutableStateFlow(true)
    val includeRound: StateFlow<Boolean> = _includeRound.asStateFlow()

    fun setIncludeRound(include: Boolean) {
        _includeRound.value = include
    }
}

data class PlayingPartnerUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
