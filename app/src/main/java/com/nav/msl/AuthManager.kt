package com.nav.msl

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(getLoginStatus())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _finishedRound = MutableStateFlow(getFinishedRoundStatus())
    val finishedRound: StateFlow<Boolean> = _finishedRound.asStateFlow()

    private fun getLoginStatus(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    private fun getFinishedRoundStatus(): Boolean {
        return prefs.getBoolean("finished_round", false)
    }

    fun login() {
        prefs.edit().putBoolean("is_logged_in", true).apply()
        _isLoggedIn.value = true
    }

    fun logout() {
        prefs.edit().putBoolean("is_logged_in", false).apply()
        _isLoggedIn.value = false
        // Also reset finished round on logout
        setFinishedRound(false)
    }

    fun setFinishedRound(finished: Boolean) {
        prefs.edit().putBoolean("finished_round", finished).apply()
        _finishedRound.value = finished
    }

    fun isUserLoggedIn(): Boolean {
        return getLoginStatus()
    }

    fun isFinishedRound(): Boolean {
        return getFinishedRoundStatus()
    }
}