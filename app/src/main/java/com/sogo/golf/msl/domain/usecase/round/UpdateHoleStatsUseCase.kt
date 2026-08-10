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
 * offline edit is carried by the bulk-sync recovery on reconnect, then awaits the targeted
 * remote PATCH when connected. SOGO-only (golfer's own card).
 */
class UpdateHoleStatsUseCase @Inject constructor(
    private val roundLocalDbRepository: RoundLocalDbRepository,
    private val sogoMongoRepository: SogoMongoRepository,
    private val networkChecker: NetworkChecker,
    private val roundSyncCoordinator: RoundSyncCoordinator
) {
    suspend operator fun invoke(
        round: Round,
        holeNumber: Int,
        stats: HoleStats,
        requireRemoteSync: Boolean = false
    ): HoleStatsSaveResult {
        return roundSyncCoordinator.withRoundSync {
            // Re-read only after acquiring the app-wide gate. Review can be open while another
            // writer finishes; basing this copy on Room prevents restoring its older snapshot.
            val authoritativeRound = roundLocalDbRepository.getRoundById(round.id)
                ?: error("Round ${round.id} is no longer available")
            require(authoritativeRound.holeScores.any { it.holeNumber == holeNumber }) {
                "Hole $holeNumber is not present in round ${round.id}"
            }

            val updatedHoleScores = authoritativeRound.holeScores.map { holeScore ->
                if (holeScore.holeNumber == holeNumber) holeScore.copy(stats = stats) else holeScore
            }
            val updatedRound = authoritativeRound.copy(
                holeScores = updatedHoleScores,
                lastUpdated = System.currentTimeMillis(),
                isSynced = false
            )

            // Local first — the source of truth for offline. Local failures reach the caller.
            roundLocalDbRepository.saveRound(updatedRound)
            Log.d("UpdateHoleStats", "✅ hole $holeNumber stats saved to Room")

            if (!networkChecker.isNetworkAvailable()) {
                Log.d("UpdateHoleStats", "⚠️ offline - deferring remote sync to bulk sync")
                if (requireRemoteSync) {
                    throw RequiredHoleStatsSyncException("Connect to the internet before submitting your stats.")
                }
                return@withRoundSync HoleStatsSaveResult(updatedRound, HoleStatsRemoteSync.Deferred)
            }

            when (val result = sogoMongoRepository.updateHoleStats(round.id, holeNumber, stats)) {
                is NetworkResult.Success -> {
                    Log.d("UpdateHoleStats", "✅ synced to MongoDB")
                    HoleStatsSaveResult(updatedRound, HoleStatsRemoteSync.Synced)
                }
                is NetworkResult.Error -> {
                    val message = result.error.toUserMessage()
                    Log.w("UpdateHoleStats", "⚠️ remote sync failed: $message")
                    if (requireRemoteSync) throw RequiredHoleStatsSyncException(message)
                    HoleStatsSaveResult(updatedRound, HoleStatsRemoteSync.Failed)
                }
                is NetworkResult.Loading -> {
                    val message = "Stats sync did not complete. Please try again."
                    if (requireRemoteSync) throw RequiredHoleStatsSyncException(message)
                    HoleStatsSaveResult(updatedRound, HoleStatsRemoteSync.Failed)
                }
            }
        }
    }
}

data class HoleStatsSaveResult(
    val round: Round,
    val remoteSync: HoleStatsRemoteSync
)

enum class HoleStatsRemoteSync {
    Synced,
    Deferred,
    Failed
}

class RequiredHoleStatsSyncException(message: String) : IllegalStateException(message)
