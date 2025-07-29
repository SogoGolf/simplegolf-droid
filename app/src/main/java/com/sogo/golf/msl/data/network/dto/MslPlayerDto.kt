package com.sogo.golf.msl.data.network.dto

import com.sogo.golf.msl.domain.model.msl.MslPlayer

data class MslPlayerDto(
    val firstName: String?,
    val lastName: String?,
    val dailyHandicap: Int,
    val golfLinkNumber: String?,
    val competitionName: String?,
    val competitionType: String?,
    val teeName: String?,
    val teeColour: String?,
    val teeColourName: String?,
    val scoreType: String?,
    val slopeRating: Int,
    val scratchRating: Double,
    val gender: String?,
    val holes: List<MslHoleDto>
)

fun MslPlayerDto.toDomainModel(): MslPlayer {
    return MslPlayer(
        firstName = firstName,
        lastName = lastName,
        dailyHandicap = dailyHandicap,
        golfLinkNumber = golfLinkNumber,
        competitionName = competitionName,
        competitionType = competitionType,
        teeName = teeName,
        teeColour = teeColour,
        teeColourName = teeColourName,
        scoreType = scoreType,
        slopeRating = slopeRating,
        scratchRating = scratchRating,
        gender = gender,
        holes = holes.map { it.toDomainModel() }
    )
}
