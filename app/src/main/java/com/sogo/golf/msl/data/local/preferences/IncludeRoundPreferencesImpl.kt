package com.sogo.golf.msl.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncludeRoundPreferencesImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : IncludeRoundPreferences {

    companion object {
        private const val INCLUDE_ROUND_PREFS = "include_round_preferences"
        private const val KEY_INCLUDE_ROUND = "include_round_on_sogo"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences by lazy {
        try {
            EncryptedSharedPreferences.create(
                context,
                INCLUDE_ROUND_PREFS,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            context.getSharedPreferences(INCLUDE_ROUND_PREFS, Context.MODE_PRIVATE)
        }
    }

    override suspend fun setIncludeRound(include: Boolean) {
        prefs.edit().putBoolean(KEY_INCLUDE_ROUND, include).apply()
    }

    override suspend fun getIncludeRound(): Boolean {
        return prefs.getBoolean(KEY_INCLUDE_ROUND, false)
    }

    override suspend fun clearIncludeRound() {
        prefs.edit().remove(KEY_INCLUDE_ROUND).apply()
    }
}
