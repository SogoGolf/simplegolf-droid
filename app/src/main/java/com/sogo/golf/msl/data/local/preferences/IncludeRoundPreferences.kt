package com.sogo.golf.msl.data.local.preferences

interface IncludeRoundPreferences {
    suspend fun setIncludeRound(include: Boolean)
    suspend fun getIncludeRound(): Boolean
    suspend fun clearIncludeRound()
    
    suspend fun setRoundCost(cost: Double)
    suspend fun getRoundCost(): Double
    suspend fun clearRoundCost()
    
    suspend fun clearAllPreferences()
}
