package com.sogo.golf.msl.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sogo.golf.msl.domain.model.NetworkResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class BackgroundSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    //private val roundRepository: RoundRepositoryImpl
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
//            val unsyncedRounds = roundRepository.getUnsyncedRounds()
//
//            var syncErrors = 0
//            unsyncedRounds.forEach { round ->
//                when (roundRepository.syncRoundToServer(round.id)) {
//                    is NetworkResult.Success -> {
//                        // Mark as synced in local database
//                    }
//                    is NetworkResult.Error -> {
//                        syncErrors++
//                    }
//                    is NetworkResult.Loading -> { /* shouldn't happen */ }
//                }
//            }

            // Retry if some failed, success if all synced
//            if (syncErrors > 0) Result.retry() else Result.success()

            //todo: remove this just needed to compile
            Result.success()

        } catch (e: Exception) {
            Result.failure()
        }
    }
}