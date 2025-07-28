package com.sogo.golf.msl.data.local.preferencesdata

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface GameDataTimestampPreferences {
    suspend fun saveGameDataDate(date: String)
    suspend fun getGameDataDate(): String?
    suspend fun clearGameDataDate()
    suspend fun hasGameDataDate(): Boolean
}

@Singleton
class GameDataTimestampPreferencesImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : GameDataTimestampPreferences {

    companion object {
        private const val GAME_DATA_PREFS = "game_data_timestamp"
        private const val KEY_GAME_DATA_DATE = "game_data_date"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences by lazy {
        try {
            EncryptedSharedPreferences.create(
                context,
                GAME_DATA_PREFS,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback to regular SharedPreferences if encrypted fails
            context.getSharedPreferences(GAME_DATA_PREFS, Context.MODE_PRIVATE)
        }
    }

    override suspend fun saveGameDataDate(date: String) {
        prefs.edit()
            .putString(KEY_GAME_DATA_DATE, date)
            .apply()

        android.util.Log.d("GameDataTimestamp", "✅ Saved game data date: $date")
    }

    override suspend fun getGameDataDate(): String? {
        val date = prefs.getString(KEY_GAME_DATA_DATE, null)
        android.util.Log.d("GameDataTimestamp", "📅 Retrieved game data date: $date")
        return date
    }

    override suspend fun clearGameDataDate() {
        prefs.edit()
            .remove(KEY_GAME_DATA_DATE)
            .apply()

        android.util.Log.d("GameDataTimestamp", "🗑️ Cleared game data date")
    }

    override suspend fun hasGameDataDate(): Boolean {
        return prefs.contains(KEY_GAME_DATA_DATE)
    }
}