package com.sogo.golf.msl.data.network.interceptors

import android.util.Log
import com.sogo.golf.msl.BuildConfig
import com.sogo.golf.msl.MslTokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class GolfApiAuthInterceptor @Inject constructor(
    private val mslTokenManager: MslTokenManager
) : Interceptor {

    companion object {
        private const val TAG = "GolfApiAuthInterceptor"
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

            return chain.proceed(newRequest)
        } else {
            Log.d(TAG, "❌ Not a golf API request - proceeding without headers")
            return chain.proceed(originalRequest)
        }
    }
}