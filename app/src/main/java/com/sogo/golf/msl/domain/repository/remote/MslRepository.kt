package com.sogo.golf.msl.domain.repository.remote

import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.msl.*

interface MslRepository {
    suspend fun getClubs(): NetworkResult<List<MslClub>>
    suspend fun getPreliminaryToken(clubId: String): NetworkResult<MslPreliminaryToken>
    suspend fun exchangeAuthCodeForTokens(authCode: String, preliminaryToken: String): NetworkResult<MslTokens>
    suspend fun getGolfer(clubId: String): NetworkResult<MslGolfer>
    suspend fun refreshTokens(): NetworkResult<MslTokens>
    suspend fun getGame(clubId: String): NetworkResult<MslGame>
}
