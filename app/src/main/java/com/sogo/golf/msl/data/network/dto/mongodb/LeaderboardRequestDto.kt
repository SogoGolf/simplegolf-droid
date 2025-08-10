package com.sogo.golf.msl.data.network.dto.mongodb

import com.google.gson.annotations.SerializedName

data class LeaderboardRequestDto(
    @SerializedName("from")
    val from: String,
    
    @SerializedName("to")
    val to: String,
    
    @SerializedName("topX")
    val topX: Int,
    
    @SerializedName("numberHoles")
    val numberHoles: Int,
    
    @SerializedName("leaderboardIdentifier")
    val leaderboardIdentifier: String
)