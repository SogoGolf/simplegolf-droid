package com.sogo.golf.msl

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MslTokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("tokens", Context.MODE_PRIVATE)

    private val _authToken = MutableStateFlow(getAuthToken())
    val authToken: StateFlow<String> = _authToken.asStateFlow()

    private fun getAuthToken(): String {
        return prefs.getString("auth_token", "") ?: ""
    }

}