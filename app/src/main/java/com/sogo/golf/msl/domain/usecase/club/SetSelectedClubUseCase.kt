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
            clubPreferences.setSelectedClub(
                clubId = club.clubId,
                tenantId = club.tenantId
            )
            Result.success(Unit)
        } catch (e: Exception) {
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