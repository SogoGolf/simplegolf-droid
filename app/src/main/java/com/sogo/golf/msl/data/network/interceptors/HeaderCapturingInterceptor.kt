package com.sogo.golf.msl.data.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeaderCapturingInterceptor @Inject constructor() : Interceptor {

    private val threadLocalAuthHeader = ThreadLocal<String?>()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()

        if (url.contains("/refresh")) {
            val authHeader = request.header("Authorization")
            threadLocalAuthHeader.set(authHeader)
        }

        return chain.proceed(request)
    }

    fun getLastCapturedAuthHeader(): String? {
        return threadLocalAuthHeader.get()
    }

    fun clearCapturedHeader() {
        threadLocalAuthHeader.remove()
    }
}
