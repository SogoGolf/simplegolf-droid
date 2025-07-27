package com.sogo.golf.msl.data.network.dto

data class MslGameDto(
    val errorMessage: String? = null,
    val scorecardMessageOfTheDay: String? = null,
    val startingHoleNumber: Int,
    val mainCompetitionId: Int,
    val golflinkNumber: String? = null,
    val teeName: String? = null,
    val teeColourName: String? = null,
    val teeColour: String? = null,
    val dailyHandicap: Int? = null,
    val gaHandicap: Double? = null,
    val numberOfHoles: Int? = null,
    val playingPartners: List<MslPlayingPartnerDto>,
    val competitions: List<MslGameCompetitionDto>
)