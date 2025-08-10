package com.sogo.golf.msl.domain.model.mongodb

data class LeaderboardEntry(
    val entityId: String,
    val golferName: String,
    val golferState: String,
    val golferImageUrl: String? = null,
    val compScoreTotal: Int,
    val roundScores: List<Int> = emptyList(),
    val roundIds: List<String> = emptyList(),
    val leaderboardIdentifier: String,
    val rank: Int,
    val roundScoresString: String
)