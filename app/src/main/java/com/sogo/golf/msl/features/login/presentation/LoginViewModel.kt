package com.sogo.golf.msl.features.login.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    companion object {
        private const val TAG = "LoginViewModel"
    }

    private val _authSuccessEvent = MutableSharedFlow<Unit>()
    val authSuccessEvent: SharedFlow<Unit> = _authSuccessEvent

    val authUrl: String
        get() = "https://id.micropower.com.au/goldencreekgolfclub?returnUrl=msl://success"

    fun handleUrlRedirect(url: String) {
        Log.d(TAG, "=== URL REDIRECT RECEIVED ===")
        Log.d(TAG, "Full URL: $url")

        if (url.startsWith("msl://success")) {
            viewModelScope.launch {
                _authSuccessEvent.emit(Unit)
            }
        }
    }
}