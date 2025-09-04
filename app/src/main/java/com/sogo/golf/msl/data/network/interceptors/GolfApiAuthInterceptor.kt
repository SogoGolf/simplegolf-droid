package com.sogo.golf.msl.data.network.interceptors

import android.util.Log
import com.sogo.golf.msl.BuildConfig
import com.sogo.golf.msl.MslTokenManager
import com.sogo.golf.msl.data.network.api.MpsAuthApiService
import com.sogo.golf.msl.data.network.dto.PostRefreshTokenRequestDto
import com.sogo.golf.msl.data.network.mappers.toDomainModel
import com.sogo.golf.msl.domain.model.msl.MslTokens
import io.sentry.Sentry
import io.sentry.SentryAttribute
import io.sentry.SentryAttributes
import io.sentry.SentryLogLevel
import io.sentry.logger.SentryLogParameters
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
        private val refreshLock = Object()
        private var isRefreshing = false
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url

        // Only apply to golf API requests
        if (!url.host.contains("golf-api.micropower.com.au")) {
            return chain.proceed(originalRequest)
        }

        // Build request with current tokens
        val memberToken = mslTokenManager.getAuthorizationHeader()
        val requestWithAuth = originalRequest.newBuilder()
            .addHeader("Authorization", BuildConfig.SOGO_AUTHORIZATION)
            .apply {
                if (memberToken != null) addHeader("X-Member-Token", memberToken)
            }
            .build()

        var response = chain.proceed(requestWithAuth)

        if (response.code == 401 && memberToken != null) {
            Log.d(TAG, "🔄 Received 401, attempting forced token refresh")
            response.close()

            val refreshedToken = refreshTokenIfNeeded(force = true)

            if (refreshedToken != null && refreshedToken != memberToken) {
                Log.d(TAG, "✅ Token actually refreshed, retrying request with new token")
                val retryRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", BuildConfig.SOGO_AUTHORIZATION)
                    .addHeader("X-Member-Token", refreshedToken)
                    .build()
                return chain.proceed(retryRequest)
            } else {
                Sentry.logger().error("❌ Token refresh did not yield a new token; not retrying or retrying would be futile")
                Log.e(TAG, "❌ Token refresh did not yield a new token; not retrying or retrying would be futile")
            }
        }

        return response
    }

    private fun refreshTokenIfNeeded(force: Boolean = false): String? {
        synchronized(refreshLock) {
            val currentHeader = mslTokenManager.getAuthorizationHeader()
            val tokens = mslTokenManager.getTokens()

            tokens?.accessToken?.let { Log.d(TAG, it) }
            Log.d(TAG, "**************************************")
            tokens?.refreshToken?.let { Log.d(TAG, it) }

            if (!force && currentHeader != null && tokens != null && !tokens.isExpired()) {
                Log.d(TAG, "Token already valid; skipping refresh")
                return currentHeader
            }

            if (isRefreshing) {
                Log.d(TAG, "Another thread is refreshing token; waiting...")
                // Wait until the other refresh finishes (with timeout to avoid deadlock)
                try {
                    (refreshLock as java.lang.Object).wait(2000)
                } catch (ie: InterruptedException) {
                    Log.w(TAG, "Interrupted while waiting for token refresh")
                }
                // Return whatever is now stored (could still be old)
                return mslTokenManager.getAuthorizationHeader()
            }

            // We're the one to refresh
            isRefreshing = true
        }

        try {
            Log.d(TAG, "Starting token refresh...")
            val currentTokens = mslTokenManager.getTokens() ?: return null

            try {
                Sentry.logger().log(
                    SentryLogLevel.FATAL,
                    SentryLogParameters.create(
                        SentryAttributes.of(
                            SentryAttribute.stringAttribute("current_refresh_token", currentTokens.refreshToken),
                            SentryAttribute.stringAttribute("current_auth_token", currentTokens.accessToken),
                        )
                    ),
                    "Using this refresh token to get new auth token"
                )
            } catch (e: Exception) {
                //fail silently
            }

            val response = runBlocking {
                mpsAuthApiService.refreshToken(
                    PostRefreshTokenRequestDto(currentTokens.refreshToken)
                )
            }

            if (response.isSuccessful) {
                val newTokensDto = response.body()?.toDomainModel() ?: return null

                try {
                    Sentry.logger().log(
                        SentryLogLevel.FATAL,
                        SentryLogParameters.create(
                            SentryAttributes.of(
                                SentryAttribute.stringAttribute("new_refresh_token", newTokensDto.refreshToken),
                                SentryAttribute.stringAttribute("new_auth_token", newTokensDto.accessToken),
                            )
                        ),
                        "Using this refresh token to get new auth token"
                    )
                } catch (e: Exception) {
                    //fail silently
                }

                val mslTokens = MslTokens(
                    accessToken = newTokensDto.accessToken,
                    refreshToken = newTokensDto.refreshToken,
                    tokenType = newTokensDto.tokenType,
                    expiresIn = newTokensDto.expiresIn,
                    issuedAt = newTokensDto.issuedAt
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
            synchronized(refreshLock) {
                isRefreshing = false
                (refreshLock as java.lang.Object).notifyAll()
            }
        }
    }
}
