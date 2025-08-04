package com.sogo.golf.msl.data.network.interceptors

import android.util.Log
import com.sogo.golf.msl.BuildConfig
import com.sogo.golf.msl.MslTokenManager
import com.sogo.golf.msl.data.network.api.MpsAuthApiService
import com.sogo.golf.msl.data.network.dto.PostRefreshTokenRequestDto
import com.sogo.golf.msl.data.network.mappers.toDomainModel
import com.sogo.golf.msl.domain.model.msl.MslTokens
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class GolfApiAuthInterceptor @Inject constructor(
    private val mslTokenManager: MslTokenManager,
    private val mpsAuthApiService: MpsAuthApiService
) : Interceptor {

    companion object {
        private const val TAG = "GolfApiAuthInterceptor"
        private val refreshLock = Any()
        private var isRefreshing = false
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url

        Log.d(TAG, "=== INTERCEPTOR DEBUG ===")
        Log.d(TAG, "Request URL: $url")
        Log.d(TAG, "Host: ${url.host}")
        Log.d(TAG, "Contains golf-api.micropower.com.au: ${url.host.contains("golf-api.micropower.com.au")}")

        // Check if this is a golf API request
        if (url.host.contains("golf-api.micropower.com.au")) {
            Log.d(TAG, "✅ This is a golf API request - adding headers")

            val memberToken = mslTokenManager.getAuthorizationHeader()
            Log.d(TAG, "Member token available: ${memberToken != null}")
            Log.d(TAG, "SOGO_AUTHORIZATION: ${BuildConfig.SOGO_AUTHORIZATION.take(20)}...")
            if (memberToken != null) {
                Log.d(TAG, "Member token: ${memberToken.take(20)}...")
            }

            val newRequest = originalRequest.newBuilder()
                .addHeader("Authorization", BuildConfig.SOGO_AUTHORIZATION)
                .apply {
                    if (memberToken != null) {
                        addHeader("X-Member-Token", memberToken)
                    }
                }
                .build()

            Log.d(TAG, "Headers added to request:")
            newRequest.headers.forEach { (name, value) ->
                Log.d(TAG, "$name: ${if (name.contains("Auth") || name.contains("Token")) value.take(20) + "..." else value}")
            }

            val response = chain.proceed(newRequest)

            // Check if we got a 401 and need to refresh token
            if (response.code == 401 && memberToken != null) {
                Log.d(TAG, "🔄 Received 401, attempting token refresh")
                response.close()

                val refreshedToken = refreshTokenIfNeeded()
                if (refreshedToken != null) {
                    Log.d(TAG, "✅ Token refreshed successfully, retrying request")
                    
                    // Retry with new token
                    val retryRequest = originalRequest.newBuilder()
                        .addHeader("Authorization", BuildConfig.SOGO_AUTHORIZATION)
                        .addHeader("X-Member-Token", refreshedToken)
                        .build()
                    
                    return chain.proceed(retryRequest)
                } else {
                    Log.e(TAG, "❌ Token refresh failed")
                }
            }

            return response
        } else {
            Log.d(TAG, "❌ Not a golf API request - proceeding without headers")
            return chain.proceed(originalRequest)
        }
    }

    private fun refreshTokenIfNeeded(): String? {
        synchronized(refreshLock) {
            // Check if another thread already refreshed the token
            val currentToken = mslTokenManager.getAuthorizationHeader()
            val tokens = mslTokenManager.getTokens()
            if (currentToken != null && tokens != null && !tokens.isExpired()) {
                Log.d(TAG, "Token already refreshed by another thread")
                return currentToken
            }

            if (isRefreshing) {
                Log.d(TAG, "Token refresh already in progress, waiting...")
                // Wait for refresh to complete (simple approach)
                Thread.sleep(1000)
                return mslTokenManager.getAuthorizationHeader()
            }

            isRefreshing = true
            try {
                Log.d(TAG, "Starting token refresh...")
                val currentTokens = mslTokenManager.getTokens()
                    ?: return null

                val response = runBlocking {
                    mpsAuthApiService.refreshToken(PostRefreshTokenRequestDto(currentTokens.refreshToken))
                }

                if (response.isSuccessful) {
                    val newTokens = response.body()?.toDomainModel()
                        ?: return null

                    // Save new tokens
                    val mslTokens = MslTokens(
                        accessToken = newTokens.accessToken,
                        refreshToken = newTokens.refreshToken,
                        tokenType = newTokens.tokenType,
                        expiresIn = newTokens.expiresIn,
                        issuedAt = newTokens.issuedAt
                    )
                    mslTokenManager.saveTokens(mslTokens)

                    Log.d(TAG, "✅ Token refresh successful")
                    return mslTokenManager.getAuthorizationHeader()
                } else {
                    Log.e(TAG, "❌ Token refresh failed: ${response.code()} - ${response.message()}")
                    return null
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Token refresh exception: ${e.message}")
                return null
            } finally {
                isRefreshing = false
            }
        }
    }
}
