package com.sogo.golf.msl.data.network.interceptors

import com.sogo.golf.msl.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class SogoApiAuthInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val newRequest = originalRequest.newBuilder()
            .addHeader("Ocp-Apim-Subscription-Key", BuildConfig.SOGO_OCP_SUBSCRIPTION_KEY)
            .build()

        return chain.proceed(newRequest)
    }
}