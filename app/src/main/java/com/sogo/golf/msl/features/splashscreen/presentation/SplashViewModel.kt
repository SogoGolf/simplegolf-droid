package com.sogo.golf.msl.features.splashscreen.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sogo.golf.msl.domain.usecase.auth.GetAuthStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getAuthStateUseCase: GetAuthStateUseCase
) : ViewModel() {

    val authState = getAuthStateUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.sogo.golf.msl.domain.model.AuthState()
        )

//    fun getNextDestination(): String {
//        return when {
//            !authState.value.isLoggedIn -> "login"
//            authState.value.hasActiveRound -> "playroundscreen"
//            else -> "homescreen"
//        }
//    }
}
