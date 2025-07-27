package com.sogo.golf.msl.data.network.dto

import com.google.gson.annotations.SerializedName

data class PostAuthTokenRequestDto(
    @SerializedName("Code") val code: String
)