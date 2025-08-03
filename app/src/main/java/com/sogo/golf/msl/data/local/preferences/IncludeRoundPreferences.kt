package com.sogo.golf.msl.data.local.preferences

interface IncludeRoundPreferences {
    suspend fun setIncludeRound(include: Boolean)
    suspend fun getIncludeRound(): Boolean
    suspend fun clearIncludeRound()
}
