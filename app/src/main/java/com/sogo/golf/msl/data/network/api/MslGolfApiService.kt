package com.sogo.golf.msl.data.network.api

import com.sogo.golf.msl.data.network.dto.*
import com.sogo.golf.msl.domain.model.msl.MslCompetition
import com.sogo.golf.msl.domain.model.msl.MslGame
import com.sogo.golf.msl.domain.model.msl.request_response.DeleteMarkerRequest
import com.sogo.golf.msl.domain.model.msl.request_response.DeleteMarkerResponse
import com.sogo.golf.msl.domain.model.msl.request_response.PutMarkerRequest
import com.sogo.golf.msl.domain.model.msl.request_response.PutMarkerResponse
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
    @GET("v2/{competitionId}/competition")
    suspend fun getCompetition(
        @Path("competitionId") competitionId: String
    ): Response<MslCompetitionDto>

    // Marker endpoints
    @PUT("v2/{companyCode}/marker")
    suspend fun putMarker(
        @Path("companyCode") companyCode: String,
        @Body request: PutMarkerRequestDto
    ): Response<PutMarkerResponseDto>

    @DELETE("v2/{companyCode}/marker")
    suspend fun deleteMarker(
        @Path("companyCode") companyCode: String,
        @Body request: DeleteMarkerRequestDto
    ): Response<DeleteMarkerResponseDto>
}