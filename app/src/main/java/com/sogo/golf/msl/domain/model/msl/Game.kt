package com.sogo.golf.msl.domain.model.msl

import org.threeten.bp.LocalDateTime


data class Game(
    val errorMessage: String? = null,
    val scorecardMessageOfTheDay: String? = null,

    val bookingTime: LocalDateTime? = null,

    val startingHoleNumber: Int,
    val mainCompetitionId: Int,
    val golflinkNumber: String? = null,
    val teeName: String? = null,
    val teeColourName: String? = null,
    val teeColour: String? = null,
    val dailyHandicap: Int? = null,
    val gaHandicap: Double? = null,
    val numberOfHoles: Int? = null,
    val playingPartners: List<PlayingPartner>,
    val competitions: List<GameCompetition>
)




