package com.sogo.golf.msl.data.local.preferences

interface AuthPreferences {
    suspend fun setLoggedIn(isLoggedIn: Boolean)
    suspend fun isLoggedIn(): Boolean
    suspend fun setFinishedRound(finished: Boolean)
    suspend fun hasFinishedRound(): Boolean
}