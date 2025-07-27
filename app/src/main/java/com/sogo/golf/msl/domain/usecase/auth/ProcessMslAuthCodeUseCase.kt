package com.sogo.golf.msl.domain.usecase.auth

import android.util.Log
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.repository.AuthRepository
import com.sogo.golf.msl.domain.repository.MslRepository
import javax.inject.Inject

class ProcessMslAuthCodeUseCase @Inject constructor(
    private val mslRepository: MslRepository,
    private val authRepository: AuthRepository
) {
    companion object {
        private const val TAG = "ProcessMslAuthCode"
    }

    suspend operator fun invoke(authCode: String, clubId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Starting MSL authentication process")
            Log.d(TAG, "Club ID: $clubId, Auth code: ${authCode.take(10)}...")

            // Step 1: Get preliminary token
            Log.d(TAG, "Getting preliminary token for club: $clubId")
            val prelimTokenResult = mslRepository.getPreliminaryToken(clubId)

            val prelimToken = when (prelimTokenResult) {
                is NetworkResult.Success -> prelimTokenResult.data.token
                is NetworkResult.Error -> {
                    Log.e(TAG, "Failed to get preliminary token: ${prelimTokenResult.error}")
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

                    // Step 3: Get golfer data (optional - to verify the auth worked)
                    try {
                        val golferResult = mslRepository.getGolfer(clubId)
                        when (golferResult) {
                            is NetworkResult.Success -> {
                                Log.d(TAG, "Successfully retrieved golfer: ${golferResult.data.firstName} ${golferResult.data.surname}")
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
                    Result.failure(Exception("Failed to authenticate: ${tokensResult.error.toUserMessage()}"))
                }
                is NetworkResult.Loading -> {
                    Result.failure(Exception("Unexpected loading state"))
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during MSL authentication", e)
            Result.failure(e)
        }
    }
}