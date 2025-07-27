package com.sogo.golf.msl.data.network.interceptors

import com.sogo.golf.msl.BuildConfig
import com.sogo.golf.msl.MslTokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class MpsAuthInterceptor @Inject constructor(
    private val mslTokenManager: MslTokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()

        // Different auth tokens for different endpoints
        val authHeader = when {
            url.contains("/authcode") -> {
                // This will be overridden by the header parameter in the repository
                originalRequest.header("Authorization") ?: BuildConfig.SOGO_AUTHORIZATION
            }
            url.contains("/refresh") -> BuildConfig.SOGO_AUTHORIZATION
            else -> BuildConfig.SOGO_AUTHORIZATION
        }

        val newRequest = originalRequest.newBuilder()
            .header("Authorization", authHeader)
            .build()

        return chain.proceed(newRequest)
    }
}