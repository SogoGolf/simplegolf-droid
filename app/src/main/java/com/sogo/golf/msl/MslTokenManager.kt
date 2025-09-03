package com.sogo.golf.msl

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.sogo.golf.msl.domain.model.msl.MslTokens
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MslTokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TOKENS_PREFS = "msl_tokens"
        private const val KEY_MSL_TOKENS = "msl_tokens_json"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_REFRESH_FAILED = "refresh_failed"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs: SharedPreferences by lazy {
        try {
            EncryptedSharedPreferences.create(
                context,
                TOKENS_PREFS,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback to regular SharedPreferences if encrypted fails
            context.getSharedPreferences(TOKENS_PREFS, Context.MODE_PRIVATE)
        }
    }

    private val gson = Gson()

    private val _authToken = MutableStateFlow(getAuthToken())
    val authToken: StateFlow<String> = _authToken.asStateFlow()

    /**
     * Save MSL tokens securely
     */
    fun saveTokens(tokens: MslTokens) {
        val tokensJson = gson.toJson(tokens)
        encryptedPrefs.edit()
            .putString(KEY_MSL_TOKENS, tokensJson)
            .apply()

        // Update the auth token StateFlow
        _authToken.value = tokens.accessToken
    }

    /**
     * Get stored MSL tokens
     */
    fun getTokens(): MslTokens? {
        val tokensJson = encryptedPrefs.getString(KEY_MSL_TOKENS, null)
        return if (tokensJson != null) {
            try {
                gson.fromJson(tokensJson, MslTokens::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    /**
     * Get authorization header for API calls
     */
    fun getAuthorizationHeader(): String? {
        val tokens = getTokens()
        return if (tokens != null && !tokens.isExpired()) {
            "${tokens.tokenType} ${tokens.accessToken}"
        } else {
            null
        }
    }

    /**
     * Check if tokens are available and valid
     */
    fun hasValidTokens(): Boolean {
        val tokens = getTokens()
        return tokens != null && !tokens.isExpired()
    }

    /**
     * Clear all stored tokens
     */
    fun clearTokens() {
        encryptedPrefs.edit()
            .remove(KEY_MSL_TOKENS)
            .remove(KEY_AUTH_TOKEN)
            .apply()

        _authToken.value = ""
    }

    /**
     * Legacy method - get auth token as string
     */
    private fun getAuthToken(): String {
        return encryptedPrefs.getString(KEY_AUTH_TOKEN, "") ?: ""
    }

    /**
     * Legacy method - save auth token
     */
    fun saveAuthToken(token: String) {
        encryptedPrefs.edit()
            .putString(KEY_AUTH_TOKEN, token)
            .apply()

        _authToken.value = token
    }

    /**
     * Mark that token refresh failed
     */
    fun markRefreshFailed() {
        encryptedPrefs.edit()
            .putBoolean(KEY_REFRESH_FAILED, true)
            .apply()
    }

    /**
     * Check if token refresh failed
     */
    fun hasRefreshFailed(): Boolean {
        return encryptedPrefs.getBoolean(KEY_REFRESH_FAILED, false)
    }

    /**
     * Clear refresh failed flag
     */
    fun clearRefreshFailedFlag() {
        encryptedPrefs.edit()
            .remove(KEY_REFRESH_FAILED)
            .apply()
    }
}
