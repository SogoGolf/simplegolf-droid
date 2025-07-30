package com.sogo.golf.msl.domain.model

data class AuthState(
    val isLoggedIn: Boolean = false,
    val hasActiveRound: Boolean = false
)
