package com.sogo.golf.msl.data.network.api

import com.sogo.golf.msl.data.network.dto.PostPrelimTokenRequestDto
import com.sogo.golf.msl.data.network.dto.PostPrelimTokenResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SogoApiService {

    @POST("v1/msl/token")
    suspend fun getPreliminaryToken(
        @Body request: PostPrelimTokenRequestDto
    ): Response<PostPrelimTokenResponseDto>
}