package com.sogo.golf.msl.data.network.interceptors

import com.sogo.golf.msl.BuildConfig
import com.sogo.golf.msl.MslTokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class GolfApiAuthInterceptor @Inject constructor(
    private val mslTokenManager: MslTokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val memberToken = mslTokenManager.getAuthorizationHeader()

        val newRequest = originalRequest.newBuilder()
            .addHeader("Authorization", BuildConfig.SOGO_AUTHORIZATION)
            .apply {
                if (memberToken != null) {
                    addHeader("X-Member-Token", memberToken)
                }
            }
            .build()

        return chain.proceed(newRequest)
    }
}