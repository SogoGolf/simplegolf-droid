package com.sogo.golf.msl.features.sogo_home.presentation.state

import com.sogo.golf.msl.domain.model.mongodb.SogoGolfer

data class SogoGolferDataState(
    val sogoGolfer: SogoGolfer? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
