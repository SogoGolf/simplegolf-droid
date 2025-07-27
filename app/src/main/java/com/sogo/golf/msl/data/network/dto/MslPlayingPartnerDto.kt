package com.sogo.golf.msl.data.network.dto

import com.sogo.golf.msl.domain.model.msl.MslPlayingPartner

data class MslPlayingPartnerDto(
    val firstName: String? = null,
    val lastName: String? = null,
    val dailyHandicap: Int,
    val golfLinkNumber: String? = null,
    val markedByGolfLinkNumber: String? = null
)

fun MslPlayingPartnerDto.toDomainModel(): MslPlayingPartner {
    return MslPlayingPartner(
        firstName = firstName,
        lastName = lastName,
        dailyHandicap = dailyHandicap,
        golfLinkNumber = golfLinkNumber,
        markedByGolfLinkNumber = markedByGolfLinkNumber
    )
}