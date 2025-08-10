package com.sogo.golf.msl.data.network.api

import com.sogo.golf.msl.data.network.dto.mongodb.SogoLeaderboardRequestDto
import com.sogo.golf.msl.data.network.dto.mongodb.SogoLeaderboardResponseDto
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SogoGeneralApi {

    @POST("leaderboard")
    suspend fun getSogoLeaderboard(
        @Header("Ocp-Apim-Subscription-Key") apiKey: String,
        @Body request: SogoLeaderboardRequestDto
    ): List<SogoLeaderboardResponseDto>
}