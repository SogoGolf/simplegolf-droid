// app/src/main/java/com/sogo/golf/msl/data/network/api/SogoMongoApiService.kt
package com.sogo.golf.msl.data.network.api

import com.sogo.golf.msl.data.network.dto.mongodb.FeeDto
import com.sogo.golf.msl.data.network.dto.mongodb.RoundDto
import com.sogo.golf.msl.data.network.dto.mongodb.SogoGolferDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SogoMongoApiService {

    @GET("fees")
    suspend fun getFees(): Response<List<FeeDto>>

    @GET("golfers")
    suspend fun getSogoGolferByGolfLinkNo(
        @Query("golflinkNo") golfLinkNo: String
    ): Response<SogoGolferDto>

    @POST("round")
    suspend fun createRound(
        @Body roundDto: RoundDto
    ): Response<RoundDto>
    
    @DELETE("rounds/{id}")
    suspend fun deleteRound(@Path("id") roundId: String): Response<Unit>

}
