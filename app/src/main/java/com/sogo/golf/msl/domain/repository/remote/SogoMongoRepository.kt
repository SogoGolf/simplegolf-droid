package com.sogo.golf.msl.domain.repository.remote

import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.model.mongodb.Fee
import com.sogo.golf.msl.domain.model.mongodb.SogoGolfer

interface SogoMongoRepository {
    suspend fun getFees(): NetworkResult<List<Fee>>
    suspend fun getSogoGolferByGolfLinkNo(golfLinkNo: String): NetworkResult<SogoGolfer>
    suspend fun createRound(round: Round): NetworkResult<Round>
    suspend fun deleteRound(roundId: String): NetworkResult<Unit>
    
    suspend fun updateHoleScore(
        roundId: String, 
        holeNumber: Int, 
        strokes: Int, 
        score: Int, 
        playingPartnerStrokes: Int, 
        playingPartnerScore: Int
    ): NetworkResult<Unit>
    
    suspend fun updateAllHoleScores(
        roundId: String,
        round: Round
    ): NetworkResult<Unit>
    
    suspend fun updateRound(roundId: String, round: Round): NetworkResult<Unit>
    
    suspend fun updateRoundSubmissionStatus(roundId: String, isSubmitted: Boolean): NetworkResult<Unit>
    suspend fun updateGolferTokenBalance(golflinkNo: String, newBalance: Int): NetworkResult<SogoGolfer>
    suspend fun createTransaction(
        entityId: String?,
        transactionId: String,
        golferId: String?,
        golferEmail: String?,
        amount: Int,
        transactionType: String,
        debitCreditType: String,
        comment: String,
        status: String,
        mainCompetitionId: Int? = null
    ): NetworkResult<Unit>

    suspend fun getTransactionsByGolferDateCompetition(
        golferId: String,
        date: String,
        mainCompetitionId: Int
    ): NetworkResult<List<com.sogo.golf.msl.data.network.dto.mongodb.TransactionDto>>
}
