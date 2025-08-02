package com.sogo.golf.msl.data.local.preferences

interface RoundPreferences {
    suspend fun setIncludeRoundOnSogo(include: Boolean)
    suspend fun getIncludeRoundOnSogo(): Boolean
    suspend fun clearIncludeRoundOnSogo()
}
