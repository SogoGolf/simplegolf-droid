// app/src/main/java/com/sogo/golf/msl/data/local/preferences/ClubPreferencesImpl.kt
package com.sogo.golf.msl.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClubPreferencesImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ClubPreferences {

    companion object {
        private const val CLUB_PREFS = "club_preferences"
        private const val KEY_SELECTED_CLUB_ID = "selected_club_id"
        private const val KEY_SELECTED_TENANT_ID = "selected_tenant_id"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences by lazy {
        try {
            EncryptedSharedPreferences.create(
                context,
                CLUB_PREFS,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback to regular SharedPreferences if encrypted fails
            context.getSharedPreferences(CLUB_PREFS, Context.MODE_PRIVATE)
        }
    }

    override suspend fun setSelectedClub(clubId: Int, tenantId: String) {
        prefs.edit()
            .putInt(KEY_SELECTED_CLUB_ID, clubId)
            .putString(KEY_SELECTED_TENANT_ID, tenantId)
            .apply()
    }

    override suspend fun getSelectedClubId(): Int? {
        val clubId = prefs.getInt(KEY_SELECTED_CLUB_ID, -1)
        return if (clubId == -1) null else clubId
    }

    override suspend fun getSelectedTenantId(): String? {
        return prefs.getString(KEY_SELECTED_TENANT_ID, null)
    }

    override suspend fun clearSelectedClub() {
        prefs.edit()
            .remove(KEY_SELECTED_CLUB_ID)
            .remove(KEY_SELECTED_TENANT_ID)
            .apply()
    }

    override suspend fun hasSelectedClub(): Boolean {
        return prefs.contains(KEY_SELECTED_CLUB_ID) &&
                prefs.contains(KEY_SELECTED_TENANT_ID)
    }
}