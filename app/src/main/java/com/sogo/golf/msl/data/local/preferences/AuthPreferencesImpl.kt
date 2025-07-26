package com.sogo.golf.msl.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthPreferencesImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AuthPreferences {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    override suspend fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean("is_logged_in", isLoggedIn).apply()
    }

    override suspend fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    override suspend fun setFinishedRound(finished: Boolean) {
        prefs.edit().putBoolean("finished_round", finished).apply()
    }

    override suspend fun hasFinishedRound(): Boolean {
        return prefs.getBoolean("finished_round", false)
    }
}