package com.sogo.golf.msl.domain.usecase.round

import android.util.Log
import com.sogo.golf.msl.data.network.NetworkChecker
import com.sogo.golf.msl.domain.model.HoleStats
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.repository.RoundLocalDbRepository
import com.sogo.golf.msl.domain.repository.remote.SogoMongoRepository
import javax.inject.Inject

/**
 * Persists a hole's SOGO [HoleStats] locally-first (Room), marks the round unsynced so an
 * offline edit is carried by the bulk-sync recovery on reconnect, then fire-and-forgets the
 * remote PATCH when connected. Mirrors [UpdateHoleScoreUseCase]. SOGO-only (golfer's own card).
 */
class UpdateHoleStatsUseCase @Inject constructor(
    private val roundLocalDbRepository: RoundLocalDbRepository,
    private val sogoMongoRepository: SogoMongoRepository,
    private val networkChecker: NetworkChecker
) {
    suspend operator fun invoke(round: Round, holeNumber: Int, stats: HoleStats) {
        try {
            val updatedHoleScores = round.holeScores.map { holeScore ->
                if (holeScore.holeNumber == holeNumber) holeScore.copy(stats = stats) else holeScore
            }
            val updatedRound = round.copy(
                holeScores = updatedHoleScores,
                lastUpdated = System.currentTimeMillis(),
                isSynced = false
            )

            // Local first — the source of truth for offline.
            roundLocalDbRepository.saveRound(updatedRound)
            Log.d("UpdateHoleStats", "✅ hole $holeNumber stats saved to Room")

            // Remote sync (fire-and-forget). Offline edits ride the bulk sync on reconnect.
            if (networkChecker.isNetworkAvailable()) {
                when (val result = sogoMongoRepository.updateHoleStats(round.id, holeNumber, stats)) {
                    is NetworkResult.Success -> Log.d("UpdateHoleStats", "✅ synced to MongoDB")
                    is NetworkResult.Error -> Log.w("UpdateHoleStats", "⚠️ remote sync failed (silent): ${result.error}")
                    is NetworkResult.Loading -> {}
                }
            } else {
                Log.d("UpdateHoleStats", "⚠️ offline - deferring remote sync to bulk sync")
            }
        } catch (e: Exception) {
            Log.e("UpdateHoleStats", "❌ Error saving hole stats", e)
        }
    }
}
