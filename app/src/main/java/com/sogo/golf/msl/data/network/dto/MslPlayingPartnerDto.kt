package com.sogo.golf.msl.data.network.dto

data class MslPlayingPartnerDto(
    val firstName: String? = null,
    val lastName: String? = null,
    val dailyHandicap: Int,
    val golfLinkNumber: String? = null,
    val markedByGolfLinkNumber: String? = null
)