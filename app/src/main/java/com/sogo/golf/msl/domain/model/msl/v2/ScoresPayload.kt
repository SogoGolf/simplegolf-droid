package com.sogo.golf.msl.domain.model.msl.v2

data class ScoresPayload(
    val golfLinkNumber: String,
    val signature: String,
    val holes: List<HolePayload>
)
