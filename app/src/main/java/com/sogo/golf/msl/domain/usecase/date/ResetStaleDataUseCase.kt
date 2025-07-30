package com.sogo.golf.msl.domain.usecase.date

import com.sogo.golf.msl.data.local.preferencesdata.GameDataTimestampPreferences
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.repository.MslCompetitionLocalDbRepository
import com.sogo.golf.msl.domain.repository.MslGameLocalDbRepository
import com.sogo.golf.msl.domain.repository.MslGolferLocalDbRepository
import com.sogo.golf.msl.domain.repository.remote.AuthRepository
import com.sogo.golf.msl.domain.usecase.club.GetMslClubAndTenantIdsUseCase
import com.sogo.golf.msl.domain.usecase.competition.FetchAndSaveCompetitionUseCase
import com.sogo.golf.msl.domain.usecase.game.FetchAndSaveGameUseCase
import com.sogo.golf.msl.shared.utils.DateUtils
import javax.inject.Inject

class ResetStaleDataUseCase @Inject constructor(
    private val mslGameLocalDbRepository: MslGameLocalDbRepository,
    private val mslCompetitionLocalDbRepository: MslCompetitionLocalDbRepository,
    private val mslGolferLocalDbRepository: MslGolferLocalDbRepository,
    private val fetchAndSaveGameUseCase: FetchAndSaveGameUseCase,
    private val fetchAndSaveCompetitionUseCase: FetchAndSaveCompetitionUseCase,
    private val getMslClubAndTenantIdsUseCase: GetMslClubAndTenantIdsUseCase,
    private val gameDataTimestampPreferences: GameDataTimestampPreferences,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            android.util.Log.d("ResetStaleData", "=== RESETTING STALE DATA ===")

            // Step 1: Clear all local data
            android.util.Log.d("ResetStaleData", "🗑️ Step 1: Clearing local data...")
            mslGameLocalDbRepository.clearAllGames()
            mslCompetitionLocalDbRepository.clearAllCompetitions()
            // Note: Keep golfer data as it's user-specific, not date-specific

            // Step 2: Refresh active round state (check database for today's rounds)
            android.util.Log.d("ResetStaleData", "🔄 Step 2: Refreshing active round state...")
            authRepository.refreshActiveRoundState()

            // Step 3: Fetch fresh data for today
            android.util.Log.d("ResetStaleData", "📥 Step 3: Fetching fresh data...")
            val selectedClub = getMslClubAndTenantIdsUseCase()

            if (selectedClub?.clubId != null) {
                val clubIdStr = selectedClub.clubId.toString()

                // Fetch game data
                when (val gameResult = fetchAndSaveGameUseCase(clubIdStr)) {
                    is NetworkResult.Success -> {
                        android.util.Log.d("ResetStaleData", "✅ Fresh game data fetched successfully")
                    }
                    is NetworkResult.Error -> {
                        android.util.Log.w("ResetStaleData", "⚠️ Failed to fetch fresh game data: ${gameResult.error}")
                        // Continue with competition fetch even if game fails
                    }
                    is NetworkResult.Loading -> { /* Ignore */ }
                }

                // Fetch competition data
                when (val competitionResult = fetchAndSaveCompetitionUseCase(clubIdStr)) {
                    is NetworkResult.Success -> {
                        android.util.Log.d("ResetStaleData", "✅ Fresh competition data fetched successfully")
                    }
                    is NetworkResult.Error -> {
                        android.util.Log.w("ResetStaleData", "⚠️ Failed to fetch fresh competition data: ${competitionResult.error}")
                    }
                    is NetworkResult.Loading -> { /* Ignore */ }
                }

                // Step 4: Update stored date to today
                android.util.Log.d("ResetStaleData", "📅 Step 4: Updating stored date...")
                val todayDate = DateUtils.getTodayDateString()
                gameDataTimestampPreferences.saveGameDataDate(todayDate)

                android.util.Log.d("ResetStaleData", "✅ Stale data reset completed successfully")
                Result.success(Unit)

            } else {
                android.util.Log.w("ResetStaleData", "⚠️ No club selected, cannot fetch fresh data")
                Result.failure(Exception("No club selected for fresh data fetch"))
            }

        } catch (e: Exception) {
            android.util.Log.e("ResetStaleData", "❌ Error resetting stale data", e)
            Result.failure(e)
        }
    }
}
