package com.sogo.golf.msl.domain.usecase.leaderboard

import android.util.Log
import com.sogo.golf.msl.data.network.api.SogoMongoApiService
import com.sogo.golf.msl.data.network.dto.mongodb.LeaderboardRequestDto
import com.sogo.golf.msl.data.network.dto.mongodb.toDomain
import com.sogo.golf.msl.domain.model.NetworkError
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.mongodb.LeaderboardEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetLeaderboardUseCase @Inject constructor(
    private val sogoMongoApiService: SogoMongoApiService
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

            // Leaderboard comes from our Mongo API (POST /leaderboard).
            // An empty board returns [] (200), not an error.
            val request = LeaderboardRequestDto(
                from = from,
                to = to,
                topX = topX,
                numberHoles = numberHoles,
                leaderboardIdentifier = leaderboardIdentifier
            )

            val response = sogoMongoApiService.getLeaderboard(request)
            if (response.isSuccessful) {
                val leaderboardEntries = response.body()?.map { it.toDomain() } ?: emptyList()
                Log.d(TAG, "Successfully fetched ${leaderboardEntries.size} leaderboard entries")
                NetworkResult.Success(leaderboardEntries)
            } else {
                Log.e(TAG, "Leaderboard request failed: ${response.code()} - ${response.message()}")
                NetworkResult.Error(NetworkError.Unknown("Leaderboard request failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching leaderboard", e)
            NetworkResult.Error(NetworkError.Unknown(e.message ?: "Unknown error"))
        }
    }
}
