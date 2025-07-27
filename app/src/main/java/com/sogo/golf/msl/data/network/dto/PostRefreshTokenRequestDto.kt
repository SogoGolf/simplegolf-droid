package com.sogo.golf.msl.data.network.dto

import com.google.gson.annotations.SerializedName

data class PostRefreshTokenRequestDto(
    @SerializedName("Refresh_Token") val refreshToken: String
)