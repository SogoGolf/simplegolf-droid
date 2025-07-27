package com.sogo.golf.msl.data.network.dto

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
    val holes: List<MslHoleDto>
)
