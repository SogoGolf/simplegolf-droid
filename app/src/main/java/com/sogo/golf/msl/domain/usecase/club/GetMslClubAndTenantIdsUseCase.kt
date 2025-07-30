// app/src/main/java/com/sogo/golf/msl/domain/usecase/club/GetMslClubAndTenantIdsUseCase.kt
package com.sogo.golf.msl.domain.usecase.club

import com.sogo.golf.msl.data.local.preferences.ClubPreferences
import com.sogo.golf.msl.domain.model.msl.SelectedClub
import javax.inject.Inject

class GetMslClubAndTenantIdsUseCase @Inject constructor(
    private val clubPreferences: ClubPreferences
) {
    suspend operator fun invoke(): SelectedClub? {
        val clubId = clubPreferences.getSelectedClubId()
        val tenantId = clubPreferences.getSelectedTenantId()
        val clubName = clubPreferences.getSelectedClubName()

        return if (clubId != null && tenantId != null) {
            SelectedClub(clubId = clubId, tenantId = tenantId, clubName = clubName)
        } else {
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
