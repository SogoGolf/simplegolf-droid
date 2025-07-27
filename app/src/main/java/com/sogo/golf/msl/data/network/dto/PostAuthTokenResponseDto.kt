package com.sogo.golf.msl.data.network.dto

import com.google.gson.annotations.SerializedName

data class PostAuthTokenResponseDto(
    @SerializedName("access_Token") val accessToken: String,
    @SerializedName("token_Type") val tokenType: String,
    @SerializedName("expires_In") val expiresIn: String,
    @SerializedName("refresh_Token") val refreshToken: String
)