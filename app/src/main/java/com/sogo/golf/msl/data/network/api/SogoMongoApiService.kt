// app/src/main/java/com/sogo/golf/msl/data/network/api/SogoMongoApiService.kt
package com.sogo.golf.msl.data.network.api

import com.sogo.golf.msl.data.network.dto.mongodb.FeeDto
import com.sogo.golf.msl.data.network.dto.mongodb.SogoGolferDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SogoMongoApiService {

    @GET("fees")
    suspend fun getFees(): Response<List<FeeDto>>

    @GET("golfers")
    suspend fun getSogoGolferByGolfLinkNo(
        @Query("golflinkNo") golfLinkNo: String
    ): Response<SogoGolferDto>

}