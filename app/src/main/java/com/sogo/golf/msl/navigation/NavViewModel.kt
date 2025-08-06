package com.sogo.golf.msl.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.sogo.golf.msl.domain.usecase.auth.GetAuthStateUseCase
import com.sogo.golf.msl.domain.usecase.auth.LoginUseCase
import com.sogo.golf.msl.domain.usecase.auth.LogoutUseCase
import com.sogo.golf.msl.domain.usecase.auth.SetFinishedRoundUseCase
import com.sogo.golf.msl.domain.usecase.database.LogDatabaseCountsUseCase
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
    private val setFinishedRoundUseCase: SetFinishedRoundUseCase,
    private val logDatabaseCountsUseCase: LogDatabaseCountsUseCase
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

    val hasActiveRound: StateFlow<Boolean> = authState
        .map { it.hasActiveRound }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // Logout confirmation dialog state
    private val _showLogoutConfirmation = MutableStateFlow(false)
    val showLogoutConfirmation: StateFlow<Boolean> = _showLogoutConfirmation.asStateFlow()

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

    // Show logout confirmation dialog and log database counts
    fun requestLogout() {
        viewModelScope.launch {
            // Log database counts before showing confirmation
            logDatabaseCountsUseCase("BEFORE LOGOUT")
            // Show confirmation dialog
            _showLogoutConfirmation.value = true
        }
    }

    // Confirm logout - actually perform the logout
    fun confirmLogout(navController: NavController) {
        viewModelScope.launch {
            logoutUseCase().onSuccess {
                // Log database counts after logout
                logDatabaseCountsUseCase("AFTER LOGOUT")
                
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
        _showLogoutConfirmation.value = false
    }

    // Cancel logout confirmation
    fun cancelLogout() {
        _showLogoutConfirmation.value = false
    }

    // Legacy logout method for backward compatibility
    @Deprecated("Use requestLogout() instead")
    fun logout(navController: NavController) {
        requestLogout()
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

    fun hasActiveRound(): Boolean {
        return authState.value.hasActiveRound
    }
}
