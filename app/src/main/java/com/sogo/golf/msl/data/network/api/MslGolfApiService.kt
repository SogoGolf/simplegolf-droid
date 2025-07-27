// app/src/main/java/com/sogo/golf/msl/data/network/api/MslGolfApiService.kt
package com.sogo.golf.msl.data.network.api

import com.sogo.golf.msl.data.network.dto.*
import retrofit2.Response
import retrofit2.http.*

interface GolfApiService {

    // Get clubs
    @GET("v2/{companyCode}/clubs")
    suspend fun getClubs(
        @Path("companyCode") companyCode: String
    ): Response<List<MslClubDto>>

    // Get golfer data
    @GET("{clubId}/golfer")
    suspend fun getGolfer(
        @Path("clubId") clubId: String
    ): Response<MslGolferDto>

    // Get game data
    @GET("v2/{clubId}/game")
    suspend fun getGame(
        @Path("clubId") clubId: String
    ): Response<MslGameDto>

    // Get competition data
    @GET("v2/{clubId}/competition")
    suspend fun getCompetition(
        @Path("clubId") clubId: String
    ): Response<MslCompetitionDto>

    // NEW: Marker endpoints
    @PUT("v2/{companyCode}/marker")
    suspend fun putMarker(
        @Path("companyCode") companyCode: String,
        @Body request: PutMarkerRequestDto
    ): Response<PutMarkerResponseDto>

    @HTTP(method = "DELETE", path = "v2/{companyCode}/marker", hasBody = true)
    suspend fun deleteMarker(
        @Path("companyCode") companyCode: String,
        @Body request: DeleteMarkerRequestDto
    ): Response<DeleteMarkerResponseDto>
}