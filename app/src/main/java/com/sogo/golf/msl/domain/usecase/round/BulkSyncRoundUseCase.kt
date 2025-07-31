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
    private val getActiveTodayRoundUseCase: GetActiveTodayRoundUseCase
) {
    suspend operator fun invoke(): Boolean {
        try {
            Log.d("BulkSyncRound", "🔄 Starting bulk sync check...")
            
            // Check if network is available
            if (!networkChecker.isNetworkAvailable()) {
                Log.d("BulkSyncRound", "⚠️ No network available - skipping bulk sync")
                return false
            }
            
            // Check if there's an active round
            val activeRound = getActiveTodayRoundUseCase()
            if (activeRound == null) {
                Log.d("BulkSyncRound", "ℹ️ No active round found - skipping bulk sync")
                return false
            }
            
            // Check if round needs syncing
            if (activeRound.isSynced) {
                Log.d("BulkSyncRound", "✅ Round already synced - skipping bulk sync")
                return false
            }
            
            Log.d("BulkSyncRound", "🚀 Performing bulk sync for round ${activeRound.id}")
            
            // Perform bulk sync
            when (val result = sogoMongoRepository.updateAllHoleScores(activeRound.id, activeRound)) {
                is NetworkResult.Success -> {
                    Log.d("BulkSyncRound", "✅ Bulk sync successful")
                    
                    // Mark round as synced
                    val syncedRound = activeRound.copy(
                        isSynced = true,
                        lastUpdated = System.currentTimeMillis()
                    )
                    roundLocalDbRepository.saveRound(syncedRound)
                    
                    return true
                }
                is NetworkResult.Error -> {
                    Log.w("BulkSyncRound", "⚠️ Bulk sync failed (silent): ${result.error}")
                    return false
                }
                is NetworkResult.Loading -> {
                    return false
                }
            }
        } catch (e: Exception) {
            Log.w("BulkSyncRound", "⚠️ Bulk sync failed (silent)", e)
            return false
        }
    }
}
