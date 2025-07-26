package com.sogo.golf.msl.domain.model.msl

data class ScoresContainer(
    val playerScores: List<ScoresPayload>
)

data class ScoresPayload(
    val golfLinkNumber: String,
    val signature: String,
    val holes: List<HolePayload>
)

data class HolePayload(
    val grossScore: Int,
    val ballPickedUp: Boolean,
    val notPlayed: Boolean
)

data class ScoresResponse(
    val errorMessage: String? = null
)
