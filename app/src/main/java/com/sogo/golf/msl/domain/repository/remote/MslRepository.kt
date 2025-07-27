// app/src/main/java/com/sogo/golf/msl/domain/repository/remote/MslRepository.kt
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
    suspend fun getCompetition(clubId: String): NetworkResult<MslCompetition>

    // NEW: Marker API methods
    suspend fun selectMarker(playerGolfLinkNumber: String): NetworkResult<Unit>
    suspend fun removeMarker(playerGolfLinkNumber: String): NetworkResult<Unit>
}