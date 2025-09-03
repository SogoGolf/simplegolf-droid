package com.sogo.golf.msl.data.repository.remote

import com.sogo.golf.msl.BuildConfig
import com.sogo.golf.msl.data.network.NetworkChecker
import com.sogo.golf.msl.data.network.dto.*
import com.sogo.golf.msl.data.network.mappers.*
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.msl.*
import com.sogo.golf.msl.domain.repository.remote.MslRepository
import android.util.Log
import com.sogo.golf.msl.MslTokenManager
import com.sogo.golf.msl.data.network.api.GolfApiService
import com.sogo.golf.msl.data.network.api.MpsAuthApiService
import com.sogo.golf.msl.data.network.api.SogoApiService
import com.sogo.golf.msl.data.repository.BaseRepository
import com.sogo.golf.msl.domain.usecase.club.GetMslClubAndTenantIdsUseCase
import io.sentry.Sentry
import io.sentry.Sentry.logger
import io.sentry.SentryLogLevel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MslRepositoryImpl @Inject constructor(
    private val golfApiService: GolfApiService,
    private val sogoApiService: SogoApiService,
    private val mpsAuthApiService: MpsAuthApiService,
    private val networkChecker: NetworkChecker,
    private val mslTokenManager: MslTokenManager,
    private val getMslClubAndTenantIdsUseCase: GetMslClubAndTenantIdsUseCase,
    ) : BaseRepository(networkChecker, mslTokenManager), MslRepository {

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
                val rawGolferDto = response.body()

                // ✅ LOG THE RAW API RESPONSE
                Log.d(TAG, "=== RAW MSL GOLFER API RESPONSE ===")
                Log.d(TAG, "Raw DTO: $rawGolferDto")
                Log.d(TAG, "Raw primary field: ${rawGolferDto?.primary}")

                val golfer = rawGolferDto?.toDomainModel()
                    ?: throw Exception("Empty golfer response")

                // ✅ LOG THE MAPPED DOMAIN MODEL
                Log.d(TAG, "=== AFTER MAPPING TO DOMAIN MODEL ===")
                Log.d(TAG, "Domain handicap: ${golfer.primary}")

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

    override suspend fun getCompetition(clubId: String): NetworkResult<MslCompetition> {
        return safeNetworkCall {
            Log.d(TAG, "Getting competition data for club: $clubId")

            // Headers automatically added by GolfApiAuthInterceptor
            val response = golfApiService.getCompetition(clubId = clubId)

            if (response.isSuccessful) {
                val rawCompetitionDto = response.body()

                // ✅ LOG THE RAW API RESPONSE
                Log.d(TAG, "=== RAW MSL COMPETITION API RESPONSE ===")
                Log.d(TAG, "Raw DTO: $rawCompetitionDto")
                Log.d(TAG, "Players count: ${rawCompetitionDto?.players?.size}")

                val competition = rawCompetitionDto?.toDomainModel()
                    ?: throw Exception("Empty competition response")

                // ✅ LOG THE MAPPED DOMAIN MODEL
                Log.d(TAG, "=== AFTER MAPPING TO DOMAIN MODEL ===")
                Log.d(TAG, "Competition players count: ${competition.players.size}")
                competition.players.forEach { player ->
                    Log.d(TAG, "Player: ${player.firstName} ${player.lastName} - ${player.competitionName}")
                }

                Log.d(TAG, "Successfully retrieved competition with ${competition.players.size} players")
                competition
            } else {
                Log.e(TAG, "Failed to get competition: ${response.code()} - ${response.message()}")
                Log.e(TAG, "Response body: ${response.errorBody()?.string()}")

                throw Exception("Failed to get competition: ${response.message()}")
            }
        }
    }

    // NEW: Select Marker implementation
    override suspend fun selectMarker(playerGolfLinkNumber: String): NetworkResult<Unit> {
        return safeNetworkCall {
            Log.d(TAG, "Selecting marker for player: $playerGolfLinkNumber")

            val request = PutMarkerRequestDto(playerGolfLinkNumber = playerGolfLinkNumber)

            val selectedClub = getMslClubAndTenantIdsUseCase()
                ?: throw Exception("No club selected. Please login again.")
            val clubIdStr = selectedClub.clubId.toString()

            val response = golfApiService.putMarker(
                companyCode = clubIdStr,
                request = request
            )

            if (response.isSuccessful) {
                val markerResponse = response.body()

                Log.d(TAG, "=== PUT MARKER API RESPONSE ===")
                Log.d(TAG, "Raw response: $markerResponse")

                if (markerResponse?.errorMessage == null) {
                    Log.d(TAG, "✅ Successfully selected marker for player: $playerGolfLinkNumber")
                    Unit // Return Unit for success
                } else {
                    Log.e(TAG, "❌ API returned error: ${markerResponse.errorMessage}")
                    throw Exception("Failed to select marker: ${markerResponse.errorMessage}")
                }
            } else {
                Log.e(TAG, "❌ Failed to select marker: ${response.code()} - ${response.message()}")
                Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                throw Exception("Failed to select marker: ${response.message()}")
            }
        }
    }

    // NEW: Remove Marker implementation
    override suspend fun removeMarker(playerGolfLinkNumber: String): NetworkResult<Unit> {
        return safeNetworkCall {
            Log.d(TAG, "Removing marker for player: $playerGolfLinkNumber")

            val selectedClub = getMslClubAndTenantIdsUseCase()
                ?: throw Exception("No club selected. Please login again.")
            val clubIdStr = selectedClub.clubId.toString()

            val request = DeleteMarkerRequestDto(playerGolfLinkNumber = playerGolfLinkNumber)
            val response = golfApiService.deleteMarker(
                companyCode = clubIdStr,
                request = request
            )

            if (response.isSuccessful) {
                val markerResponse = response.body()

                Log.d(TAG, "=== DELETE MARKER API RESPONSE ===")
                Log.d(TAG, "Raw response: $markerResponse")

                if (markerResponse?.errorMessage == null) {
                    Log.d(TAG, "✅ Successfully removed marker for player: $playerGolfLinkNumber")
                    Unit // Return Unit for success
                } else {
                    Log.e(TAG, "❌ API returned error: Request to delete marker failed.. ${markerResponse.errorMessage}")
                    logger().log(SentryLogLevel.ERROR, "DELETE marker API returned error - playerGolfLinkNumber: $playerGolfLinkNumber, clubId: $clubIdStr, errorMessage: ${markerResponse.errorMessage}")
                    Unit
                }
            } else {
                Log.e(TAG, "❌ Failed to remove marker: ${response.code()} - ${response.message()}")
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Response body: $errorBody")
                logger().log(SentryLogLevel.ERROR, "DELETE marker HTTP request failed - playerGolfLinkNumber: $playerGolfLinkNumber, clubId: $clubIdStr, httpCode: ${response.code()}, httpMessage: ${response.message()}, responseBody: $errorBody")
                Unit
            }
        }
    }

    override suspend fun postMslScores(clubId: String, scores: com.sogo.golf.msl.domain.model.msl.v2.ScoresContainer): NetworkResult<com.sogo.golf.msl.domain.model.msl.v2.ScoresResponse> {
        return safeNetworkCall {
            Log.d(TAG, "Submitting MSL scores for club: $clubId")

            val response = golfApiService.postMslScores(
                clubId = clubId,
                scores = scores
            )

            if (response.isSuccessful) {
                val scoresResponse = response.body()
                    ?: throw Exception("Empty scores response")

                Log.d(TAG, "✅ Successfully submitted MSL scores")
                scoresResponse
            } else {
                Log.e(TAG, "❌ Failed to submit MSL scores: ${response.code()} - ${response.message()}")
                Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                throw Exception("Failed to submit MSL scores: ${response.message()}")
            }
        }
    }
}
