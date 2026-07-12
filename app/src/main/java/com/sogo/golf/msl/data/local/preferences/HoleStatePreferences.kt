package com.sogo.golf.msl.data.local.preferences

interface HoleStatePreferences {
    suspend fun saveCurrentHole(roundId: String, holeNumber: Int)
    suspend fun getCurrentHole(roundId: String): Int?
    suspend fun clearCurrentHole(roundId: String)
    suspend fun clearAllHoleStates()

    /** Per-hole pace snapshots (hole -> wall-clock millis it was completed). */
    suspend fun savePaceSnapshots(roundId: String, snapshots: Map<Int, Long>)
    suspend fun getPaceSnapshots(roundId: String): Map<Int, Long>
    suspend fun clearPaceSnapshots(roundId: String)
}
