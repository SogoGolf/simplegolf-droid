// app/src/main/java/com/sogo/golf/msl/data/network/api/SogoMongoApiService.kt
package com.sogo.golf.msl.data.network.api

import com.sogo.golf.msl.data.network.dto.mongodb.FeeDto
import retrofit2.Response
import retrofit2.http.GET

interface SogoMongoApiService {

    @GET("fees")
    suspend fun getFees(): Response<List<FeeDto>>
}