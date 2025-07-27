package com.sogo.golf.msl.data.repository

import com.sogo.golf.msl.BuildConfig
import com.sogo.golf.msl.data.network.NetworkChecker
import com.sogo.golf.msl.data.network.dto.*
import com.sogo.golf.msl.data.network.mappers.*
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.msl.*
import com.sogo.golf.msl.domain.repository.MslRepository
import android.util.Log
import com.sogo.golf.msl.MslTokenManager
import com.sogo.golf.msl.data.network.api.GolfApiService
import com.sogo.golf.msl.data.network.api.MpsAuthApiService
import com.sogo.golf.msl.data.network.api.SogoApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MslRepositoryImpl @Inject constructor(
    private val golfApiService: GolfApiService,
    private val sogoApiService: SogoApiService,
    private val mpsAuthApiService: MpsAuthApiService,
    private val networkChecker: NetworkChecker,
    private val mslTokenManager: MslTokenManager
) : BaseRepository(networkChecker), MslRepository {

    companion object {
        private const val TAG = "MslRepository"
    }

    override suspend fun getClubs(): NetworkResult<List<MslClub>> {
        return safeNetworkCall {
            Log.d(TAG, "Getting clubs for company code: ${BuildConfig.MSL_COMPANY_CODE}")
            Log.d(TAG, "Using authorization: ${BuildConfig.SOGO_AUTHORIZATION}")

            val response = golfApiService.getClubs(
                companyCode = BuildConfig.MSL_COMPANY_CODE
            )

            if (response.isSuccessful) {
                val rawClubs = response.body()
                Log.d(TAG, "Raw response body: $rawClubs")

                val clubs = rawClubs?.toDomainModel() ?: emptyList()
                Log.d(TAG, "Successfully retrieved ${clubs.size} clubs")
                clubs.forEach { club ->
                    Log.d(TAG, "Club: ${club.name} (ID: ${club.clubId})")
                }
                clubs
            } else {
                Log.e(TAG, "Failed to get clubs: ${response.code()} - ${response.message()}")
                Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                throw Exception("Failed to get clubs: ${response.message()}")
            }
        }
    }

    override suspend fun getPreliminaryToken(clubId: String): NetworkResult<MslPreliminaryToken> {
        return safeNetworkCall {
            Log.d(TAG, "Getting preliminary token for club: $clubId")

            val request = PostPrelimTokenRequestDto(clubId = clubId)
            val response = sogoApiService.getPreliminaryToken(request)

            if (response.isSuccessful) {
                val prelimToken = response.body()?.toDomainModel()
                    ?: throw Exception("Empty preliminary token response")

                Log.d(TAG, "Successfully got preliminary token: ${prelimToken.token.take(10)}...")
                prelimToken
            } else {
                Log.e(TAG, "Failed to get preliminary token: ${response.code()} - ${response.message()}")
                Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                throw Exception("Failed to get preliminary token: ${response.message()}")
            }
        }
    }

    override suspend fun exchangeAuthCodeForTokens(
        authCode: String,
        preliminaryToken: String
    ): NetworkResult<MslTokens> {
        return safeNetworkCall {
            Log.d(TAG, "Exchanging auth code for tokens")

            val request = PostAuthTokenRequestDto(code = authCode)
            val response = mpsAuthApiService.exchangeAuthCode(
                preliminaryToken = "Bearer $preliminaryToken",
                request = request
            )

            if (response.isSuccessful) {
                val authTokens = response.body()?.toDomainModel()
                    ?: throw Exception("Empty auth token response")

                // Save tokens securely
                val mslTokens = MslTokens(
                    accessToken = authTokens.accessToken,
                    refreshToken = authTokens.refreshToken,
                    tokenType = authTokens.tokenType,
                    expiresIn = authTokens.expiresIn,
                    issuedAt = authTokens.issuedAt
                )
                mslTokenManager.saveTokens(mslTokens)

                Log.d(TAG, "Successfully exchanged auth code and saved tokens")
                authTokens
            } else {
                Log.e(TAG, "Failed to exchange auth code: ${response.code()} - ${response.message()}")
                Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                throw Exception("Failed to exchange auth code: ${response.message()}")
            }
        }
    }

    override suspend fun getGolfer(clubId: String): NetworkResult<MslGolfer> {
        return safeNetworkCall {
            Log.d(TAG, "Getting golfer data for club: $clubId")

            val authHeader = mslTokenManager.getAuthorizationHeader()
                ?: throw Exception("No access token available")

            val response = golfApiService.getGolfer(
                clubId = clubId,
            )

            if (response.isSuccessful) {
                val golfer = response.body()?.toDomainModel()
                    ?: throw Exception("Empty golfer response")

                Log.d(TAG, "Successfully retrieved golfer: ${golfer.firstName} ${golfer.surname}")
                golfer
            } else {
                Log.e(TAG, "Failed to get golfer: ${response.code()} - ${response.message()}")
                Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                throw Exception("Failed to get golfer: ${response.message()}")
            }
        }
    }

    override suspend fun refreshTokens(): NetworkResult<MslTokens> {
        return safeNetworkCall {
            Log.d(TAG, "Refreshing MSL tokens")

            val currentTokens = mslTokenManager.getTokens()
                ?: throw Exception("No refresh token available")

            val response = mpsAuthApiService.refreshToken(PostRefreshTokenRequestDto("Bearer ${currentTokens.refreshToken}"))

            if (response.isSuccessful) {
                val newTokens = response.body()?.toDomainModel()
                    ?: throw Exception("Empty refresh token response")

                // Save new tokens
                val mslTokens = MslTokens(
                    accessToken = newTokens.accessToken,
                    refreshToken = newTokens.refreshToken,
                    tokenType = newTokens.tokenType,
                    expiresIn = newTokens.expiresIn,
                    issuedAt = newTokens.issuedAt
                )
                mslTokenManager.saveTokens(mslTokens)

                Log.d(TAG, "Successfully refreshed tokens")
                newTokens
            } else {
                Log.e(TAG, "Failed to refresh tokens: ${response.code()} - ${response.message()}")
                Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                throw Exception("Failed to refresh tokens: ${response.message()}")
            }
        }
    }

    override suspend fun getGame(clubId: String): NetworkResult<MslGame> {
        return safeNetworkCall {
            Log.d(TAG, "Getting game data for game: $clubId")

            // Headers automatically added by GolfApiAuthInterceptor
            val response = golfApiService.getGame(clubId = clubId)

            if (response.isSuccessful) {
                val game = response.body()?.toDomainModel()
                    ?: throw Exception("Empty game response")

                Log.d(TAG, "Successfully retrieved game: ${game.mainCompetitionId}")
                game
            } else {
                Log.e(TAG, "Failed to get game: ${response.code()} - ${response.message()}")
                Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                throw Exception("Failed to get game: ${response.message()}")
            }
        }
    }
}