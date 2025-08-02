package com.sogo.golf.msl.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoundPreferencesImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : RoundPreferences {

    companion object {
        private const val ROUND_PREFS = "round_preferences"
        private const val KEY_INCLUDE_ROUND_ON_SOGO = "include_round_on_sogo"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences by lazy {
        try {
            EncryptedSharedPreferences.create(
                context,
                ROUND_PREFS,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            context.getSharedPreferences(ROUND_PREFS, Context.MODE_PRIVATE)
        }
    }

    override suspend fun setIncludeRoundOnSogo(include: Boolean) {
        prefs.edit()
            .putBoolean(KEY_INCLUDE_ROUND_ON_SOGO, include)
            .apply()
    }

    override suspend fun getIncludeRoundOnSogo(): Boolean {
        return prefs.getBoolean(KEY_INCLUDE_ROUND_ON_SOGO, true)
    }

    override suspend fun clearIncludeRoundOnSogo() {
        prefs.edit()
            .remove(KEY_INCLUDE_ROUND_ON_SOGO)
            .apply()
    }
}
