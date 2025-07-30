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
}
