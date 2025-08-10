package com.sogo.golf.msl.data.network.dto.mongodb

data class SogoLeaderboardRequestDto(
    val from: String,
    val to: String,
    val topX: Int,
    val numberHoles: Int,
    val leaderboardIdentifier: String
)