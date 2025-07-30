// app/src/main/java/com/sogo/golf/msl/domain/usecase/club/SetSelectedClubUseCase.kt
package com.sogo.golf.msl.domain.usecase.club

import com.sogo.golf.msl.data.local.preferences.ClubPreferences
import com.sogo.golf.msl.domain.model.msl.MslClub
import javax.inject.Inject

class SetSelectedClubUseCase @Inject constructor(
    private val clubPreferences: ClubPreferences
) {
    suspend operator fun invoke(club: MslClub): Result<Unit> {
        return try {
            android.util.Log.d("SetSelectedClubUseCase", "=== STORING CLUB ===")
            android.util.Log.d("SetSelectedClubUseCase", "Club ID: ${club.clubId}")
            android.util.Log.d("SetSelectedClubUseCase", "Tenant ID: ${club.tenantId}")
            android.util.Log.d("SetSelectedClubUseCase", "Club Name: '${club.name}'")
            android.util.Log.d("SetSelectedClubUseCase", "Club Name is null: ${club.name == null}")
            android.util.Log.d("SetSelectedClubUseCase", "Club Name is blank: ${club.name?.isBlank()}")
            
            clubPreferences.setSelectedClub(
                clubId = club.clubId,
                tenantId = club.tenantId,
                clubName = club.name
            )
            
            android.util.Log.d("SetSelectedClubUseCase", "✅ Club stored successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("SetSelectedClubUseCase", "❌ Failed to store club", e)
            Result.failure(e)
        }
    }

    suspend fun clearSelectedClub(): Result<Unit> {
        return try {
            clubPreferences.clearSelectedClub()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
