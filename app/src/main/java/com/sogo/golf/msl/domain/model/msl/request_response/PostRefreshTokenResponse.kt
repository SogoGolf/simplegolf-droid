package com.sogo.golf.msl.domain.model.msl.request_response


data class PostRefreshTokenResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: String,
    val refreshToken: String,
)


