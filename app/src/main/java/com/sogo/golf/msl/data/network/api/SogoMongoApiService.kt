// app/src/main/java/com/sogo/golf/msl/data/network/api/SogoMongoApiService.kt
package com.sogo.golf.msl.data.network.api

import com.sogo.golf.msl.data.network.dto.mongodb.FeeDto
import com.sogo.golf.msl.data.network.dto.mongodb.RoundDto
import com.sogo.golf.msl.data.network.dto.mongodb.HoleScoreDto
import com.sogo.golf.msl.data.network.dto.mongodb.PlayingPartnerRoundDto
import com.sogo.golf.msl.data.network.dto.mongodb.SogoGolferDto
import com.sogo.golf.msl.data.network.dto.mongodb.TransactionDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
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

    @PATCH("rounds/{id}")
    suspend fun updateRound(
        @Path("id") roundId: String,
        @Body payload: RoundUpdatePayload
    ): Response<Unit>

    @PATCH("rounds/{id}")
    suspend fun updateRoundSubmissionStatus(
        @Path("id") roundId: String,
        @Body payload: RoundSubmissionUpdatePayload
    ): Response<Unit>

    @PATCH("rounds/{roundId}/holes/{holeNumber}")
    suspend fun updateHoleScore(
        @Path("roundId") roundId: String,
        @Path("holeNumber") holeNumber: Int,
        @Body payload: HoleScoreUpdatePayload
    ): Response<Unit>

    @PATCH("updateAllHoleScores/{roundId}")
    suspend fun updateAllHoleScores(
        @Path("roundId") roundId: String,
        @Body payload: BulkHoleScoreUpdatePayload
    ): Response<Unit>

    @PATCH("golfers/{golflinkNo}/tokenBalance")
    suspend fun updateGolferTokenBalance(
        @Path("golflinkNo") golflinkNo: String,
        @Body payload: TokenBalanceUpdatePayload
    ): Response<SogoGolferDto>

    @POST("transactions")
    suspend fun createTransaction(
        @Body transactionDto: TransactionDto
    ): Response<TransactionDto>

    @GET("transactions/by-golfer-date-competition")
    suspend fun getTransactionsByGolferDateCompetition(
        @Query("golferId") golferId: String,
        @Query("date") date: String,
        @Query("mainCompetitionId") mainCompetitionId: Int
    ): Response<List<TransactionDto>>

    @POST("golfers")
    suspend fun createGolfer(
        @Body request: CreateGolferRequestDto
    ): Response<SogoGolferDto>
}

data class TokenBalanceUpdatePayload(
    val tokenBalance: Int
)

data class TransactionDto(
    val entityId: String?,
    val transactionId: String,
    val golferId: String?,
    val golferEmail: String?,
    val amount: Int,
    val transactionType: String,
    val debitCreditType: String,
    val comment: String,
    val status: String,
    val mainCompetitionId: Int? = null
)

data class HoleScoreUpdatePayload(
    val strokes: Int,
    val score: Int,
    val playingPartnerStrokes: Int,
    val playingPartnerScore: Int
)

data class BulkHoleScoreUpdatePayload(
    val golfer: List<HoleScoreData>,
    val playingPartner: List<HoleScoreData>
)

data class HoleScoreData(
    val holeNumber: Int,
    val strokes: Int,
    val score: Int
)

data class RoundUpdatePayload(
    val holeScores: List<HoleScoreDto>? = null,
    val playingPartnerRound: PlayingPartnerRoundDto? = null,
    val isSubmitted: Boolean? = null
)

data class RoundSubmissionUpdatePayload(
    val isSubmitted: Boolean,
    val submittedTime: String
)

data class CreateGolferRequestDto(
    val authSystemUid: String,
    val country: String,
    val dateOfBirth: String,
    val deviceManufacturer: String? = null,
    val deviceModel: String? = null,
    val deviceOS: String? = null,
    val deviceOSVersion: String? = null,
    val deviceToken: String? = null,
    val email: String,
    val firstName: String,
    val gender: String,
    val golflinkNo: String,
    val isAcceptedSogoTermsAndConditions: Boolean = true,
    val lastName: String,
    val mobileNo: String,
    val postCode: String,
    val sogoAppVersion: String,
    val state: String
)
