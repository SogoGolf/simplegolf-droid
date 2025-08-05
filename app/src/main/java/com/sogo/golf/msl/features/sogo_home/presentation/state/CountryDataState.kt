package com.sogo.golf.msl.features.sogo_home.presentation.state

data class CountryDataState(
    val country: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
