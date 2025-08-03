package com.sogo.golf.msl.data.repository.remote

import android.util.Log
import com.sogo.golf.msl.data.network.NetworkChecker
import com.sogo.golf.msl.data.network.api.SogoMongoApiService
import com.sogo.golf.msl.data.network.api.HoleScoreUpdatePayload
import com.sogo.golf.msl.data.network.api.HoleScoreData
import com.sogo.golf.msl.data.network.api.BulkHoleScoreUpdatePayload
import com.sogo.golf.msl.data.network.api.RoundUpdatePayload
import com.sogo.golf.msl.data.network.api.RoundSubmissionUpdatePayload
import com.sogo.golf.msl.data.network.api.TokenBalanceUpdatePayload
import com.sogo.golf.msl.data.network.dto.mongodb.TransactionDto
import com.sogo.golf.msl.data.network.dto.mongodb.toDomainModel
import com.sogo.golf.msl.data.network.dto.mongodb.toDto
import com.sogo.golf.msl.data.repository.BaseRepository
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.NetworkError
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.model.mongodb.Fee
import com.sogo.golf.msl.domain.model.mongodb.SogoGolfer
import com.sogo.golf.msl.domain.repository.remote.SogoMongoRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SogoMongoRepositoryImpl @Inject constructor(
    private val sogoMongoApiService: SogoMongoApiService,
    private val networkChecker: NetworkChecker
) : BaseRepository(networkChecker), SogoMongoRepository {

    companion object {
        private const val TAG = "SogoMongoRepository"
    }

    override suspend fun getFees(): NetworkResult<List<Fee>> {
        return safeNetworkCall {
            Log.d(TAG, "Getting fees from SOGO Mongo API")

            val response = sogoMongoApiService.getFees()

            if (response.isSuccessful) {
                val rawFees = response.body()
                Log.d(TAG, "Raw response body: $rawFees")

                val fees = rawFees?.toDomainModel() ?: emptyList()
                Log.d(TAG, "Successfully retrieved ${fees.size} fees")
                fees.forEach { fee ->
                    Log.d(TAG, "Fee: ${fee.description} - ${fee.numberHoles} holes - $${fee.cost}")
                }
                fees
            } else {
                Log.e(TAG, "Failed to get fees: ${response.code()} - ${response.message()}")
                Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                throw Exception("Failed to get fees: ${response.message()}")
            }
        }
    }

    override suspend fun getSogoGolferByGolfLinkNo(golfLinkNo: String): NetworkResult<SogoGolfer> {
        return safeNetworkCall {
            Log.d(TAG, "Getting SogoGolfer from SOGO Mongo API for golfLinkNo: $golfLinkNo")

            val response = sogoMongoApiService.getSogoGolferByGolfLinkNo(golfLinkNo)

            if (response.isSuccessful) {
                val rawSogoGolfer = response.body()
                Log.d(TAG, "Raw response body: $rawSogoGolfer")

                val sogoGolfer = rawSogoGolfer?.toDomainModel()
                    ?: throw Exception("Empty SogoGolfer response")

                Log.d(TAG, "Successfully retrieved SogoGolfer: ${sogoGolfer.firstName} ${sogoGolfer.lastName}")
                Log.d(TAG, "SogoGolfer details - Email: ${sogoGolfer.email}, Club: ${sogoGolfer.club}, Handicap: ${sogoGolfer.handicap}")

                sogoGolfer
            } else {
                Log.e(TAG, "Failed to get SogoGolfer: ${response.code()} - ${response.message()}")
                Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                throw Exception("Failed to get SogoGolfer: ${response.message()}")
            }
        }
    }

    override suspend fun createRound(round: Round): NetworkResult<Round> {
        return safeNetworkCall {
            Log.d(TAG, "Creating round in SOGO Mongo API for golfer: ${round.golflinkNo}")
            
            val roundDto = round.toDto()
            val response = sogoMongoApiService.createRound(roundDto)
            
            if (response.isSuccessful) {
                val createdRoundDto = response.body()
                Log.d(TAG, "Successfully created round with ID: ${createdRoundDto?.id}")
                
                round.copy(isSynced = true)
            } else {
                Log.e(TAG, "Failed to create round: ${response.code()} - ${response.message()}")
                Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                throw Exception("Failed to create round: ${response.message()}")
            }
        }
    }

    override suspend fun deleteRound(roundId: String): NetworkResult<Unit> {
        return safeNetworkCall {
            Log.d(TAG, "Deleting round from SOGO Mongo API: $roundId")
            
            val response = sogoMongoApiService.deleteRound(roundId)
            
            if (response.isSuccessful) {
                Log.d(TAG, "Successfully deleted round: $roundId")
                Unit
            } else {
                Log.e(TAG, "Failed to delete round: ${response.code()} - ${response.message()}")
                Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                throw Exception("Failed to delete round: ${response.message()}")
            }
        }
    }

    override suspend fun updateHoleScore(
        roundId: String,
        holeNumber: Int,
        strokes: Int,
        score: Int,
        playingPartnerStrokes: Int,
        playingPartnerScore: Int
    ): NetworkResult<Unit> {
        return safeNetworkCall {
            Log.d(TAG, "Updating hole score for round $roundId, hole $holeNumber")
            
            val payload = HoleScoreUpdatePayload(strokes, score, playingPartnerStrokes, playingPartnerScore)
            val response = sogoMongoApiService.updateHoleScore(roundId, holeNumber, payload)
            
            if (response.isSuccessful) {
                Log.d(TAG, "Successfully updated hole score")
                Unit
            } else {
                Log.e(TAG, "Failed to update hole score: ${response.code()} - ${response.message()}")
                throw Exception("Failed to update hole score: ${response.message()}")
            }
        }
    }

    override suspend fun updateAllHoleScores(
        roundId: String,
        round: Round
    ): NetworkResult<Unit> {
        return safeNetworkCall {
            Log.d(TAG, "Bulk updating all hole scores for round $roundId")
            
            val golferHoleScores = round.holeScores.map { holeScore ->
                HoleScoreData(
                    holeNumber = holeScore.holeNumber,
                    strokes = holeScore.strokes,
                    score = holeScore.score.toInt()
                )
            }
            
            val partnerHoleScores = round.playingPartnerRound?.holeScores?.map { holeScore ->
                HoleScoreData(
                    holeNumber = holeScore.holeNumber,
                    strokes = holeScore.strokes,
                    score = holeScore.score.toInt()
                )
            } ?: emptyList()
            
            val payload = BulkHoleScoreUpdatePayload(
                golfer = golferHoleScores,
                playingPartner = partnerHoleScores
            )
            
            val response = sogoMongoApiService.updateAllHoleScores(roundId, payload)
            
            if (response.isSuccessful) {
                Log.d(TAG, "Successfully bulk updated all hole scores")
                Unit
            } else {
                Log.e(TAG, "Failed to bulk update hole scores: ${response.code()} - ${response.message()}")
                throw Exception("Failed to bulk update hole scores: ${response.message()}")
            }
        }
    }
    
    override suspend fun updateRound(roundId: String, round: Round): NetworkResult<Unit> {
        return safeNetworkCall {
            Log.d(TAG, "Updating round in SOGO Mongo API: $roundId")
            Log.d(TAG, "Main golfer hole scores: ${round.holeScores.size}")
            Log.d(TAG, "Partner round data: ${if (round.playingPartnerRound != null) "included" else "null"}")
            Log.d(TAG, "Partner hole scores: ${round.playingPartnerRound?.holeScores?.size ?: 0}")
            
            val holeScoresDto = round.holeScores.map { it.toDto() }
            val partnerRoundDto = round.playingPartnerRound?.toDto()
            val payload = RoundUpdatePayload(
                holeScores = holeScoresDto, 
                playingPartnerRound = partnerRoundDto
            )
            val response = sogoMongoApiService.updateRound(roundId, payload)
            
            if (response.isSuccessful) {
                Log.d(TAG, "Successfully updated round: $roundId")
                Unit
            } else {
                Log.e(TAG, "Failed to update round: ${response.code()} - ${response.message()}")
                Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                throw Exception("Failed to update round: ${response.message()}")
            }
        }
    }

    override suspend fun updateRoundSubmissionStatus(roundId: String, isSubmitted: Boolean): NetworkResult<Unit> {
        return safeNetworkCall {
            Log.d(TAG, "🔄 Updating round submission status in SOGO Mongo API: $roundId")
            Log.d(TAG, "🔄 Setting isSubmitted to: $isSubmitted")
            
            // Get current UTC time in ISO format (compatible with API level 24)
            val currentUtcTime = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.format(java.util.Date())
            Log.d(TAG, "🔄 Setting submittedTime to: $currentUtcTime")
            
            val payload = RoundSubmissionUpdatePayload(
                isSubmitted = isSubmitted,
                submittedTime = currentUtcTime
            )
            Log.d(TAG, "🔄 Sending minimal payload: {\"isSubmitted\": $isSubmitted, \"submittedTime\": \"$currentUtcTime\"}")
            
            val response = sogoMongoApiService.updateRoundSubmissionStatus(roundId, payload)
            
            if (response.isSuccessful) {
                Log.d(TAG, "✅ Successfully updated round submission status: $roundId")
                Log.d(TAG, "✅ Response code: ${response.code()}")
                Unit
            } else {
                Log.e(TAG, "❌ Failed to update round submission status: $roundId")
                Log.e(TAG, "❌ Response code: ${response.code()}")
                Log.e(TAG, "❌ Response message: ${response.message()}")
                Log.e(TAG, "❌ Response error body: ${response.errorBody()?.string()}")
                throw Exception("Failed to update round submission status: ${response.message()}")
            }
        }
    }

    override suspend fun updateGolferTokenBalance(golflinkNo: String, newBalance: Int): NetworkResult<SogoGolfer> {
        return try {
            val response = sogoMongoApiService.updateGolferTokenBalance(
                golflinkNo,
                TokenBalanceUpdatePayload(newBalance)
            )
            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    NetworkResult.Success(dto.toDomainModel())
                } ?: NetworkResult.Error(NetworkError.Unknown("Empty response body"))
            } else {
                NetworkResult.Error(NetworkError.ServerError)
            }
        } catch (e: Exception) {
            NetworkResult.Error(NetworkError.Unknown(e.message ?: "Network error"))
        }
    }

    override suspend fun createTransaction(
        entityId: String?,
        transactionId: String,
        golferId: String?,
        golferEmail: String?,
        amount: Int,
        transactionType: String,
        debitCreditType: String,
        comment: String,
        status: String,
        mainCompetitionId: Int?
    ): NetworkResult<Unit> {
        return try {
            val transactionDto = TransactionDto(
                entityId = entityId,
                transactionId = transactionId,
                golferId = golferId ?: "",
                golferEmail = golferEmail,
                amount = amount,
                transactionType = transactionType,
                debitCreditType = debitCreditType,
                comment = comment,
                status = status,
                mainCompetitionId = mainCompetitionId
            )
            val response = sogoMongoApiService.createTransaction(transactionDto)
            if (response.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(NetworkError.ServerError)
            }
        } catch (e: Exception) {
            NetworkResult.Error(NetworkError.Unknown(e.message ?: "Network error"))
        }
    }

    override suspend fun getTransactionsByGolferDateCompetition(
        golferId: String,
        date: String,
        mainCompetitionId: Int
    ): NetworkResult<List<com.sogo.golf.msl.data.network.dto.mongodb.TransactionDto>> {
        return try {
            val response = sogoMongoApiService.getTransactionsByGolferDateCompetition(
                golferId = golferId,
                date = date,
                mainCompetitionId = mainCompetitionId
            )
            if (response.isSuccessful) {
                NetworkResult.Success(response.body() ?: emptyList())
            } else {
                NetworkResult.Error(NetworkError.ServerError)
            }
        } catch (e: Exception) {
            NetworkResult.Error(NetworkError.Unknown(e.message ?: "Network error"))
        }
    }
}
