// app/src/main/java/com/sogo/golf/msl/domain/usecase/club/GetMslClubAndTenantIdsUseCase.kt
package com.sogo.golf.msl.domain.usecase.club

import com.sogo.golf.msl.data.local.preferences.ClubPreferences
import com.sogo.golf.msl.domain.model.msl.SelectedClub
import javax.inject.Inject

class GetMslClubAndTenantIdsUseCase @Inject constructor(
    private val clubPreferences: ClubPreferences
) {
    suspend operator fun invoke(): SelectedClub? {
        android.util.Log.d("GetMslClubAndTenantIdsUseCase", "=== RETRIEVING SELECTED CLUB ===")
        
        val clubId = clubPreferences.getSelectedClubId()
        val tenantId = clubPreferences.getSelectedTenantId()
        val clubName = clubPreferences.getSelectedClubName()

        android.util.Log.d("GetMslClubAndTenantIdsUseCase", "Club ID: $clubId")
        android.util.Log.d("GetMslClubAndTenantIdsUseCase", "Tenant ID: $tenantId")
        android.util.Log.d("GetMslClubAndTenantIdsUseCase", "Club Name: '$clubName'")
        android.util.Log.d("GetMslClubAndTenantIdsUseCase", "Club Name is null: ${clubName == null}")

        return if (clubId != null && tenantId != null) {
            val selectedClub = SelectedClub(clubId = clubId, tenantId = tenantId, clubName = clubName)
            android.util.Log.d("GetMslClubAndTenantIdsUseCase", "✅ Returning SelectedClub: $selectedClub")
            selectedClub
        } else {
            android.util.Log.w("GetMslClubAndTenantIdsUseCase", "⚠️ No club selected (clubId=$clubId, tenantId=$tenantId)")
            null
        }
    }

    suspend fun getClubId(): Int? {
        return clubPreferences.getSelectedClubId()
    }

    suspend fun getTenantId(): String? {
        return clubPreferences.getSelectedTenantId()
    }

    suspend fun hasSelectedClub(): Boolean {
        return clubPreferences.hasSelectedClub()
    }
}
