package com.sogo.golf.msl.domain.usecase.leaderboard

import android.util.Log
import com.sogo.golf.msl.BuildConfig
import com.sogo.golf.msl.data.network.api.SogoGeneralApi
import com.sogo.golf.msl.data.network.dto.mongodb.SogoLeaderboardRequestDto
import com.sogo.golf.msl.data.network.dto.mongodb.toDomain
import com.sogo.golf.msl.domain.model.NetworkError
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.mongodb.LeaderboardEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.EOFException
import javax.inject.Inject

class GetLeaderboardUseCase @Inject constructor(
    private val sogoGeneralApi: SogoGeneralApi
) {
    companion object {
        private const val TAG = "GetLeaderboardUseCase"
    }

    suspend operator fun invoke(
        from: String,
        to: String,
        topX: Int,
        numberHoles: Int,
        leaderboardIdentifier: String
    ): NetworkResult<List<LeaderboardEntry>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching leaderboard: from=$from, to=$to, topX=$topX, holes=$numberHoles, identifier=$leaderboardIdentifier")
            
            val request = SogoLeaderboardRequestDto(
                from = from,
                to = to,
                topX = topX,
                numberHoles = numberHoles,
                leaderboardIdentifier = leaderboardIdentifier
            )
            
            val leaderboardEntries = sogoGeneralApi.getSogoLeaderboard(
                apiKey = BuildConfig.SOGO_OCP_SUBSCRIPTION_KEY,
                request = request
            ).map { it.toDomain() }
            
            Log.d(TAG, "Successfully fetched ${leaderboardEntries.size} leaderboard entries")
            NetworkResult.Success(leaderboardEntries)
        } catch (e: EOFException) {
            // Server returned empty response instead of empty array - treat as empty leaderboard
            Log.i(TAG, "Empty response from server, treating as empty leaderboard")
            NetworkResult.Success(emptyList())
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching leaderboard", e)
            NetworkResult.Error(NetworkError.Unknown(e.message ?: "Unknown error"))
        }
    }
}