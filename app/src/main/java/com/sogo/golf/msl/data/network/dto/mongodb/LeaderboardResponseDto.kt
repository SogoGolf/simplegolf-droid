package com.sogo.golf.msl.data.network.dto.mongodb

import com.google.gson.annotations.SerializedName
import com.sogo.golf.msl.domain.model.mongodb.LeaderboardEntry

data class LeaderboardResponseDto(
    @SerializedName("entityId")
    val entityId: String,
    
    @SerializedName("golferName")
    val golferName: String,
    
    @SerializedName("golferState")
    val golferState: String,
    
    @SerializedName("golferImageUrl")
    val golferImageUrl: String? = null,
    
    @SerializedName("compScoreTotal")
    val compScoreTotal: Int,
    
    @SerializedName("roundScores")
    val roundScores: List<Int> = emptyList(),
    
    @SerializedName("roundIds")
    val roundIds: List<String> = emptyList(),
    
    @SerializedName("leaderboardIdentifier")
    val leaderboardIdentifier: String,
    
    @SerializedName("rank")
    val rank: Int,
    
    @SerializedName("roundScoresString")
    val roundScoresString: String
)

fun LeaderboardResponseDto.toDomain(): LeaderboardEntry {
    return LeaderboardEntry(
        entityId = entityId,
        golferName = golferName,
        golferState = golferState,
        golferImageUrl = golferImageUrl,
        compScoreTotal = compScoreTotal,
        roundScores = roundScores,
        roundIds = roundIds,
        leaderboardIdentifier = leaderboardIdentifier,
        rank = rank,
        roundScoresString = roundScoresString
    )
}