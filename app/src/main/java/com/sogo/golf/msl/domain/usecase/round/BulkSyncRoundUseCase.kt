package com.sogo.golf.msl.domain.usecase.round

import android.util.Log
import com.sogo.golf.msl.data.network.NetworkChecker
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.repository.RoundLocalDbRepository
import com.sogo.golf.msl.domain.repository.remote.SogoMongoRepository
import javax.inject.Inject

class BulkSyncRoundUseCase @Inject constructor(
    private val roundLocalDbRepository: RoundLocalDbRepository,
    private val sogoMongoRepository: SogoMongoRepository,
    private val networkChecker: NetworkChecker,
    private val getActiveTodayRoundUseCase: GetActiveTodayRoundUseCase,
    private val roundSyncCoordinator: RoundSyncCoordinator
) {
    suspend operator fun invoke(): Boolean {
        try {
            Log.d("BulkSyncRound", "🔄 Starting bulk sync check...")
            
            // Check if network is available
            if (!networkChecker.isNetworkAvailable()) {
                Log.d("BulkSyncRound", "⚠️ No network available - skipping bulk sync")
                return false
            }
            
            return roundSyncCoordinator.withRoundSync {
                // This read deliberately happens after acquiring the app-wide gate. Every
                // whole-Round writer below therefore starts from the latest committed Room row.
                val activeRound = getActiveTodayRoundUseCase()
                if (activeRound == null) {
                    Log.d("BulkSyncRound", "ℹ️ No active round found - skipping bulk sync")
                    return@withRoundSync false
                }

                if (activeRound.isSynced) {
                    Log.d("BulkSyncRound", "✅ Round already synced - skipping bulk sync")
                    return@withRoundSync false
                }

                Log.d("BulkSyncRound", "🚀 Performing bulk sync for round ${activeRound.id}")

                when (val result = sogoMongoRepository.updateAllHoleScores(activeRound.id, activeRound)) {
                    is NetworkResult.Success -> {
                        Log.d("BulkSyncRound", "✅ Bulk sync successful")

                        // Keep the guard for any Room writer outside this coordinator. If one
                        // appears, leave the latest row dirty so a later bulk pass self-heals.
                        val latestRound = roundLocalDbRepository.getRoundById(activeRound.id)
                        if (!canMarkBulkSnapshotSynced(activeRound, latestRound)) {
                            Log.d("BulkSyncRound", "↪️ Round changed during sync; leaving latest copy dirty for retry")
                            return@withRoundSync false
                        }

                        val syncedRound = requireNotNull(latestRound).copy(
                            isSynced = true,
                            lastUpdated = System.currentTimeMillis()
                        )
                        roundLocalDbRepository.saveRound(syncedRound)
                        true
                    }
                    is NetworkResult.Error -> {
                        Log.w("BulkSyncRound", "⚠️ Bulk sync failed (silent): ${result.error}")
                        false
                    }
                    is NetworkResult.Loading -> false
                }
            }
        } catch (e: Exception) {
            Log.w("BulkSyncRound", "⚠️ Bulk sync failed (silent)", e)
            return false
        }
    }
}

internal fun canMarkBulkSnapshotSynced(snapshot: Round, latest: Round?): Boolean =
    latest != null && latest.id == snapshot.id && latest.lastUpdated == snapshot.lastUpdated
