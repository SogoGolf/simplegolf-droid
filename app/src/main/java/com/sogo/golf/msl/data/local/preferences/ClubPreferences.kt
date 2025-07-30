// app/src/main/java/com/sogo/golf/msl/data/local/preferences/ClubPreferences.kt
package com.sogo.golf.msl.data.local.preferences

interface ClubPreferences {
    suspend fun setSelectedClub(clubId: Int, tenantId: String, clubName: String? = null)
    suspend fun getSelectedClubId(): Int?
    suspend fun getSelectedTenantId(): String?
    suspend fun getSelectedClubName(): String?
    suspend fun clearSelectedClub()
    suspend fun hasSelectedClub(): Boolean
}
