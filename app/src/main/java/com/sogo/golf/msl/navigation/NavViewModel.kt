package com.sogo.golf.msl.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.sogo.golf.msl.domain.usecase.auth.GetAuthStateUseCase
import com.sogo.golf.msl.domain.usecase.auth.LoginUseCase
import com.sogo.golf.msl.domain.usecase.auth.LogoutUseCase
import com.sogo.golf.msl.domain.usecase.auth.SetFinishedRoundUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val setFinishedRoundUseCase: SetFinishedRoundUseCase
) : ViewModel() {

    // Back navigation state
    private val _backNavDisabled = MutableStateFlow(false)
    val backNavDisabled: StateFlow<Boolean> = _backNavDisabled.asStateFlow()

    // Simulation state
    private val _simulateError = MutableStateFlow(true)
    val simulateError: StateFlow<Boolean> = _simulateError.asStateFlow()

    // Auth state from use case
    val authState = getAuthStateUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.sogo.golf.msl.domain.model.AuthState()
        )

    // Legacy StateFlow properties for compatibility - now using stateIn
    val isLoggedIn: StateFlow<Boolean> = authState
        .map { it.isLoggedIn }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val finishedRound: StateFlow<Boolean> = authState
        .map { it.hasFinishedRound }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun setBackNavDisabled(disabled: Boolean) {
        _backNavDisabled.value = disabled
    }

    fun setSimulateError(simulate: Boolean) {
        _simulateError.value = simulate
    }

    fun login() {
        viewModelScope.launch {
            loginUseCase()
        }
    }

    fun logout(navController: NavController) {
        viewModelScope.launch {
            logoutUseCase().onSuccess {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    fun setFinishedRound(finished: Boolean) {
        viewModelScope.launch {
            setFinishedRoundUseCase(finished)
        }
    }

    // Legacy methods for compatibility during migration
    fun isUserLoggedIn(): Boolean {
        return authState.value.isLoggedIn
    }

    fun isFinishedRound(): Boolean {
        return authState.value.hasFinishedRound
    }
}