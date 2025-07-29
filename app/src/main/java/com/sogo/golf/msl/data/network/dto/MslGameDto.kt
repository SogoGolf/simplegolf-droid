package com.sogo.golf.msl.data.network.dto

import com.sogo.golf.msl.domain.model.msl.MslGame

data class MslGameDto(
    val errorMessage: String? = null,
    val scorecardMessageOfTheDay: String? = null,
    val bookingTime: String? = null,
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

fun MslGameDto.toDomainModel(): MslGame {
    return MslGame(
        errorMessage = errorMessage,
        scorecardMessageOfTheDay = scorecardMessageOfTheDay,
        bookingTime = bookingTime?.let { 
            try {
                org.threeten.bp.LocalDateTime.parse(it)
            } catch (e: Exception) {
                android.util.Log.w("MslGameDto", "Error parsing bookingTime: $it", e)
                null
            }
        },
        startingHoleNumber = startingHoleNumber,
        mainCompetitionId = mainCompetitionId,
        golflinkNumber = golflinkNumber,
        teeName = teeName,
        teeColourName = teeColourName,
        teeColour = teeColour,
        dailyHandicap = dailyHandicap,
        gaHandicap = gaHandicap,
        numberOfHoles = numberOfHoles,
        playingPartners = playingPartners.map { it.toDomainModel() },
        competitions = competitions.map { it.toDomainModel() }
    )
}
