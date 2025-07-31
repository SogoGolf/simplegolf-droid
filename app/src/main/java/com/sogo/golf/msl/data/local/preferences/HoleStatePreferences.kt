package com.sogo.golf.msl.data.local.preferences

interface HoleStatePreferences {
    suspend fun saveCurrentHole(roundId: String, holeNumber: Int)
    suspend fun getCurrentHole(roundId: String): Int?
    suspend fun getLastScoredHole(roundId: String): Int?
    suspend fun saveLastScoredHole(roundId: String, holeNumber: Int)
    suspend fun clearHoleState(roundId: String)
    suspend fun hasHoleState(roundId: String): Boolean
}
