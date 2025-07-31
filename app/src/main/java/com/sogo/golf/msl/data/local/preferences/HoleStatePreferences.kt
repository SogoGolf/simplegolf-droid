package com.sogo.golf.msl.data.local.preferences

interface HoleStatePreferences {
    suspend fun saveCurrentHole(roundId: String, holeNumber: Int)
    suspend fun getCurrentHole(roundId: String): Int?
    suspend fun clearCurrentHole(roundId: String)
}
