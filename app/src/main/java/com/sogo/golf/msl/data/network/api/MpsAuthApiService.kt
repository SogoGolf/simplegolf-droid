package com.sogo.golf.msl.data.network.api

import retrofit2.Response
import com.sogo.golf.msl.data.network.dto.PostAuthTokenRequestDto
import com.sogo.golf.msl.data.network.dto.PostAuthTokenResponseDto
import com.sogo.golf.msl.data.network.dto.PostRefreshTokenRequestDto
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST


interface MpsAuthApiService {

    // Exchange auth code - uses specific preliminary token
    @POST("v1/security/authorisation/authcode")
    suspend fun exchangeAuthCode(
        @Header("Authorization") preliminaryToken: String,
        @Body request: PostAuthTokenRequestDto
    ): Response<PostAuthTokenResponseDto>

    // Refresh token - uses SOGO_AUTHORIZATION
    @POST("v1/security/authorisation/token/refresh")
    suspend fun refreshToken(
        @Body request: PostRefreshTokenRequestDto
    ): Response<PostAuthTokenResponseDto>
}