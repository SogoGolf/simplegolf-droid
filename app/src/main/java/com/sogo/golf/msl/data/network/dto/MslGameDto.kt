package com.sogo.golf.msl.data.network.dto

import com.sogo.golf.msl.domain.model.msl.MslGame
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

data class MslGameDto(
    val errorMessage: String? = null,
    val scorecardMessageOfTheDay: String? = null,
    val bookingTime: String? = null, // API sends as string like "1900-01-01T23:55:00"
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
        bookingTime = parseBookingTime(bookingTime),
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

/**
 * Parses booking time string from API to LocalDateTime
 * Example: "1900-01-01T23:55:00" -> LocalDateTime
 */
private fun parseBookingTime(bookingTimeString: String?): LocalDateTime? {
    return try {
        if (bookingTimeString.isNullOrBlank()) {
            null
        } else {
            // Parse the API format: "1900-01-01T23:55:00"
            LocalDateTime.parse(bookingTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
    } catch (e: Exception) {
        android.util.Log.w("MslGameDto", "Failed to parse booking time: $bookingTimeString", e)
        null
    }
}
