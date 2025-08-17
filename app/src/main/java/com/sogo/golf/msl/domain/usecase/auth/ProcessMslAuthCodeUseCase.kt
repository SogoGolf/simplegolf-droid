package com.sogo.golf.msl.domain.usecase.auth

import android.util.Log
import com.sogo.golf.msl.analytics.AnalyticsManager
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.repository.MslGolferLocalDbRepository
import com.sogo.golf.msl.domain.repository.remote.AuthRepository
import com.sogo.golf.msl.domain.repository.remote.MslRepository
import com.sogo.golf.msl.utils.JwtTokenDecoder
import javax.inject.Inject

class ProcessMslAuthCodeUseCase @Inject constructor(
    private val mslRepository: MslRepository,
    private val authRepository: AuthRepository,
    private val mslGolferLocalDbRepository: MslGolferLocalDbRepository,
    private val analyticsManager: AnalyticsManager,
    private val jwtTokenDecoder: JwtTokenDecoder
) {
    companion object {
        private const val TAG = "ProcessMslAuthCode"
    }

    suspend operator fun invoke(authCode: String, clubId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Starting MSL authentication process")
            Log.d(TAG, "Club ID: $clubId, Auth code: ${authCode.take(10)}...")

            // Track login initiation
            analyticsManager.trackEvent(
                AnalyticsManager.EVENT_LOGIN_INITIATED,
                mapOf(
                    "club_id" to clubId,
                    "timestamp" to System.currentTimeMillis()
                )
            )

            // Step 1: Get preliminary token
            Log.d(TAG, "Getting preliminary token for club: $clubId")
            val prelimTokenResult = mslRepository.getPreliminaryToken(clubId)

            val prelimToken = when (prelimTokenResult) {
                is NetworkResult.Success -> prelimTokenResult.data.token
                is NetworkResult.Error -> {
                    Log.e(TAG, "Failed to get preliminary token: ${prelimTokenResult.error}")
                    
                    // Track login failure
                    analyticsManager.trackEvent(
                        AnalyticsManager.EVENT_LOGIN_FAILED,
                        mapOf(
                            "error_type" to "preliminary_token_failure",
                            "error_message" to prelimTokenResult.error.toUserMessage(),
                            "club_id" to clubId
                        )
                    )
                    
                    return Result.failure(Exception("Failed to get preliminary token: ${prelimTokenResult.error.toUserMessage()}"))
                }
                is NetworkResult.Loading -> {
                    return Result.failure(Exception("Unexpected loading state"))
                }
            }

            Log.d(TAG, "Got preliminary token: ${prelimToken.take(10)}...")

            // Step 2: Exchange auth code for access tokens
            Log.d(TAG, "Exchanging auth code for tokens")
            val tokensResult = mslRepository.exchangeAuthCodeForTokens(authCode, prelimToken)

            when (tokensResult) {
                is NetworkResult.Success -> {
                    Log.d(TAG, "Successfully got access tokens")
                    
                    // Decode the access token to get user data
                    val tokenClaims = try {
                        jwtTokenDecoder.decodeMslToken(tokensResult.data.accessToken)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to decode access token", e)
                        null
                    }
                    
                    // Just log token data for now - don't set any user properties yet
                    if (tokenClaims != null) {
                        Log.d(TAG, "Got token data - Golfer ID: ${tokenClaims.golferId}, Member ID: ${tokenClaims.memberId}, Club ID: ${tokenClaims.clubId}")
                    } else {
                        Log.d(TAG, "Failed to decode token - will identify user when golfer data is retrieved")
                    }

                    // Step 3: Get golfer data (optional - to verify the auth worked)
                    try {
                        val golferResult = mslRepository.getGolfer(clubId)
                        when (golferResult) {
                            is NetworkResult.Success -> {
                                val golfer = golferResult.data
                                Log.d(TAG, "Successfully retrieved golfer: ${golfer.firstName} ${golfer.surname}")

                                // ✅ SAVE GOLFER TO DATABASE FOR GLOBAL ACCESS
                                Log.d(TAG, "Saving golfer to database...")
                                mslGolferLocalDbRepository.saveGolfer(golfer)
                                Log.d(TAG, "✅ Golfer saved to database successfully")
                                
                                // Set user ID to golflink number now that we have it
                                analyticsManager.setUserId(golfer.golfLinkNo)
                                
                                // Identify user with all properties now that we have complete data
                                val userProperties = mutableMapOf<String, Any>()
                                tokenClaims?.golferId?.let { userProperties["golfer_id"] = it }
                                tokenClaims?.memberId?.let { userProperties["member_id"] = it }
                                tokenClaims?.clubId?.let { userProperties["club_id"] = it }
                                userProperties["golflink_number"] = golfer.golfLinkNo
                                golfer.email?.let { userProperties["email"] = it }
                                userProperties["first_name"] = golfer.firstName
                                userProperties["surname"] = golfer.surname
                                analyticsManager.identifyUser(userProperties)
                                
                                // NOW track login_completed with full golfer data including golflink
                                val completedEventProperties = mutableMapOf<String, Any>()
                                tokenClaims?.golferId?.let { completedEventProperties["golfer_id"] = it }
                                tokenClaims?.memberId?.let { completedEventProperties["member_id"] = it }
                                tokenClaims?.clubId?.let { completedEventProperties["club_id"] = it }
                                golfer.golfLinkNo.let { completedEventProperties["golflink_number"] = it }
                                golfer.email?.let { completedEventProperties["email"] = it }
                                golfer.firstName.let { completedEventProperties["first_name"] = it }
                                golfer.surname.let { completedEventProperties["surname"] = it }
                                completedEventProperties["login_timestamp"] = System.currentTimeMillis()
                                
                                analyticsManager.trackEvent(AnalyticsManager.EVENT_LOGIN_COMPLETED, completedEventProperties)
                                Log.d(TAG, "Tracked login_completed with golflink: ${golfer.golfLinkNo}")

                            }
                            is NetworkResult.Error -> {
                                Log.w(TAG, "Could not retrieve golfer data: ${golferResult.error}")
                                // Don't fail the whole process - tokens are still valid
                            }
                            is NetworkResult.Loading -> {
                                // Ignore
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error getting golfer data", e)
                        // Don't fail the whole process
                    }

                    // Step 4: Update auth state
                    Log.d(TAG, "Updating auth state to logged in")
                    authRepository.login()

                    Log.d(TAG, "✅ MSL authentication completed successfully")
                    Result.success(Unit)
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "Failed to exchange auth code: ${tokensResult.error}")
                    
                    // Track login failure
                    analyticsManager.trackEvent(
                        AnalyticsManager.EVENT_LOGIN_FAILED,
                        mapOf(
                            "error_type" to "token_exchange_failure",
                            "error_message" to tokensResult.error.toUserMessage(),
                            "club_id" to clubId
                        )
                    )
                    
                    Result.failure(Exception("Failed to authenticate: ${tokensResult.error.toUserMessage()}"))
                }
                is NetworkResult.Loading -> {
                    Result.failure(Exception("Unexpected loading state"))
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during MSL authentication", e)
            
            // Track unexpected login failure
            analyticsManager.trackEvent(
                AnalyticsManager.EVENT_LOGIN_FAILED,
                mapOf(
                    "error_type" to "unexpected_error",
                    "error_message" to (e.message ?: "Unknown error"),
                    "club_id" to clubId
                )
            )
            
            Result.failure(e)
        }
    }
}