package com.sogo.golf.msl.domain.model.msl.v2

data class HolePayload(
    val grossScore: Int,
    val ballPickedUp: Boolean,
    val notPlayed: Boolean
)
