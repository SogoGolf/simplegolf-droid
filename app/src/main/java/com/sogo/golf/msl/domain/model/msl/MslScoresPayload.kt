package com.sogo.golf.msl.domain.model.msl

data class MslScoresContainer(
    val playerScores: List<MslScoresPayload>
)

data class MslScoresPayload(
    val golfLinkNumber: String,
    val signature: String,
    val holes: List<MslHolePayload>
)

data class MslHolePayload(
    val grossScore: Int,
    val ballPickedUp: Boolean,
    val notPlayed: Boolean
)

data class MslScoresResponse(
    val errorMessage: String? = null
)
