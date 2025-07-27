package com.sogo.golf.msl.domain.model.msl

data class MslTokens(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Long, // Convert from string to long (seconds)
    val issuedAt: Long = System.currentTimeMillis()
) {
    fun isExpired(): Boolean {
        val expirationTime = issuedAt + (expiresIn * 1000)
        val bufferTime = 5 * 60 * 1000 // 5 minutes buffer
        return System.currentTimeMillis() >= (expirationTime - bufferTime)
    }

    fun getAuthorizationHeader(): String {
        return "$tokenType $accessToken"
    }
}