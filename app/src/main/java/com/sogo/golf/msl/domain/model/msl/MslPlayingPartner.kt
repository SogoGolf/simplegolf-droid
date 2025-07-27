package com.sogo.golf.msl.domain.model.msl

data class MslPlayingPartner(
    val firstName: String? = null,
    val lastName: String? = null,
    val dailyHandicap: Int,
    val golfLinkNumber: String? = null,
    val markedByGolfLinkNumber: String? = null
)