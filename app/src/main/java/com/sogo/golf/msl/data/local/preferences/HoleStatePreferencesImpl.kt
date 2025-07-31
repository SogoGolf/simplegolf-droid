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
}
