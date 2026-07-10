package com.sogo.golf.msl.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HoleStatePreferencesImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : HoleStatePreferences {

    companion object {
        private const val HOLE_STATE_PREFS = "hole_state_preferences"
        private const val KEY_CURRENT_HOLE = "current_hole_"
        private const val KEY_PACE_SNAPSHOTS = "pace_snapshots_"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences by lazy {
        try {
            EncryptedSharedPreferences.create(
                context,
                HOLE_STATE_PREFS,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            context.getSharedPreferences(HOLE_STATE_PREFS, Context.MODE_PRIVATE)
        }
    }

    override suspend fun saveCurrentHole(roundId: String, holeNumber: Int) {
        prefs.edit()
            .putInt(KEY_CURRENT_HOLE + roundId, holeNumber)
            .apply()
        
        android.util.Log.d("HoleStatePrefs", "✅ Saved current hole: $holeNumber for round: $roundId")
    }

    override suspend fun getCurrentHole(roundId: String): Int? {
        val holeNumber = prefs.getInt(KEY_CURRENT_HOLE + roundId, -1)
        return if (holeNumber == -1) null else holeNumber
    }

    override suspend fun clearCurrentHole(roundId: String) {
        prefs.edit()
            .remove(KEY_CURRENT_HOLE + roundId)
            .apply()
        
        android.util.Log.d("HoleStatePrefs", "🗑️ Cleared current hole for round: $roundId")
    }

    override suspend fun clearAllHoleStates() {
        prefs.edit().clear().apply()
        android.util.Log.d("HoleStatePrefs", "🗑️ Cleared ALL hole states")
    }

    override suspend fun savePaceSnapshots(roundId: String, snapshots: Map<Int, Long>) {
        // Encode as "hole:millis;hole:millis" — no serialization dependency needed.
        val encoded = snapshots.entries.joinToString(";") { "${it.key}:${it.value}" }
        prefs.edit().putString(KEY_PACE_SNAPSHOTS + roundId, encoded).apply()
    }

    override suspend fun getPaceSnapshots(roundId: String): Map<Int, Long> {
        val encoded = prefs.getString(KEY_PACE_SNAPSHOTS + roundId, null)
        if (encoded.isNullOrEmpty()) return emptyMap()
        return encoded.split(";").mapNotNull { entry ->
            val parts = entry.split(":")
            val hole = parts.getOrNull(0)?.toIntOrNull()
            val millis = parts.getOrNull(1)?.toLongOrNull()
            if (hole != null && millis != null) hole to millis else null
        }.toMap()
    }

    override suspend fun clearPaceSnapshots(roundId: String) {
        prefs.edit().remove(KEY_PACE_SNAPSHOTS + roundId).apply()
        android.util.Log.d("HoleStatePrefs", "🗑️ Cleared pace snapshots for round: $roundId")
    }
}
