package com.sogo.golf.msl.navigation

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.sogo.golf.msl.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class NavViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {

    // Existing back navigation code - keep everything that was already here
    private val _backNavDisabled = MutableStateFlow(false)
    val backNavDisabled: StateFlow<Boolean> = _backNavDisabled.asStateFlow()

    fun setBackNavDisabled(disabled: Boolean) {
        _backNavDisabled.value = disabled
    }

    // Add the simulate API functionality that was in Screen4
    private val _simulateError = MutableStateFlow(true)
    val simulateError: StateFlow<Boolean> = _simulateError.asStateFlow()

    fun setSimulateError(simulate: Boolean) {
        _simulateError.value = simulate
    }

    // Auth functionality
    val isLoggedIn: StateFlow<Boolean> = authManager.isLoggedIn

    fun login() {
        authManager.login()
    }

    fun logout(navController: NavController) {
        authManager.logout()
        // Clear the entire back stack and navigate to login
        navController.navigate("login") {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun isUserLoggedIn(): Boolean {
        return authManager.isUserLoggedIn()
    }

    // New: Finished Round functionality
    val finishedRound: StateFlow<Boolean> = authManager.finishedRound

    fun setFinishedRound(finished: Boolean) {
        authManager.setFinishedRound(finished)
    }

    fun isFinishedRound(): Boolean {
        return authManager.isFinishedRound()
    }
}