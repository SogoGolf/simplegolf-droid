package com.sogo.golf.msl.data.network.api

import com.sogo.golf.msl.data.network.dto.*
import retrofit2.Response
import retrofit2.http.*

interface MslApiService {

    // Step 1: Get list of clubs with proper authorization
    @GET("v2/{companyCode}/clubs")
    suspend fun getClubs(
        @Path("companyCode") companyCode: String,
        @Header("Authorization") authorization: String
    ): Response<List<MslClubDto>>

    // Step 2: Get preliminary token (Sogo API)
    @POST("msl/token")
    @Headers("Ocp-Apim-Subscription-Key: f128c9a7885d4820b9604f185dfe310f")
    suspend fun getPreliminaryToken(
        @Body request: PostPrelimTokenRequestDto
    ): Response<PostPrelimTokenResponseDto>

    // Step 3: Exchange auth code for access token (MPS API)
    @POST("security/authorisation/authcode")
    suspend fun exchangeAuthCode(
        @Header("Authorization") preliminaryToken: String,
        @Body request: PostAuthTokenRequestDto
    ): Response<PostAuthTokenResponseDto>

    // Step 4: Get golfer data
    @GET("{clubId}/golfer")
    suspend fun getGolfer(
        @Path("clubId") clubId: String
    ): Response<MslGolferDto>

    // Token refresh endpoint
    @POST("security/authorisation/refresh")
    suspend fun refreshToken(
        @Header("Authorization") refreshToken: String
    ): Response<PostAuthTokenResponseDto>
}