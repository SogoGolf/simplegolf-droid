package com.sogo.golf.msl.data.network.dto.mongodb

import com.google.gson.annotations.SerializedName
import com.sogo.golf.msl.domain.model.StateInfo
import com.sogo.golf.msl.domain.model.mongodb.RoundSummary
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

data class RoundSummaryDto(
    @SerializedName("roundDate")
    val roundDate: String? = null,
    
    @SerializedName("golferEmail")
    val golferEmail: String? = null,
    
    @SerializedName("golferId")
    val golferId: String? = null,
    
    @SerializedName("golferFirstName")
    val golferFirstName: String? = null,
    
    @SerializedName("golferLastName")
    val golferLastName: String? = null,
    
    @SerializedName("score")
    val score: Int? = null,
    
    @SerializedName("countOfHoleScores")
    val countOfHoleScores: Int? = null,
    
    @SerializedName("golflinkNo")
    val golflinkNo: String? = null,
    
    @SerializedName("clubState")
    val clubState: String? = null,
    
    @SerializedName("clubName")
    val clubName: String? = null,
    
    @SerializedName("playingPartnerGolferFirstName")
    val playingPartnerGolferFirstName: String? = null,
    
    @SerializedName("playingPartnerGolferLastName")
    val playingPartnerGolferLastName: String? = null,
    
    @SerializedName("golfLinkHandicap")
    val golfLinkHandicap: Float? = null,
    
    @SerializedName("playingPartnerGolfLinkHandicap")
    val playingPartnerGolfLinkHandicap: Float? = null,
    
    @SerializedName("scratchRating")
    val scratchRating: Float? = null,
    
    @SerializedName("slopeRating")
    val slopeRating: Float? = null,
    
    @SerializedName("compType")
    val compType: String? = null,
    
    @SerializedName("isSubmitted")
    val isSubmitted: Boolean? = null,
    
    @SerializedName("teeColor")
    val teeColor: String? = null
)

data class ClubStateDto(
    @SerializedName("alpha2")
    val alpha2: String? = null,
    
    @SerializedName("name")
    val name: String? = null,
    
    @SerializedName("shortName")
    val shortName: String? = null
)

fun RoundSummaryDto.toDomain(): RoundSummary {
    return RoundSummary(
        roundDate = roundDate?.let { dateString ->
            try {
                // Parse as local date-time since API returns local time for the golfer
                when {
                    dateString.contains("Z") -> {
                        // If it has Z, it's UTC - convert to local by removing Z
                        // Note: This treats it as local time, not converting from UTC
                        val localString = dateString.substring(0, dateString.indexOf('Z'))
                        LocalDateTime.parse(localString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    }
                    dateString.contains("T") -> {
                        // Standard ISO local date-time format
                        LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    }
                    else -> {
                        // Fallback to ISO date
                        LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
                    }
                }
            } catch (e: Exception) {
                // Log the error for debugging
                android.util.Log.e("RoundSummaryDto", "Failed to parse date: $dateString", e)
                null
            }
        },
        golferEmail = golferEmail,
        golferId = golferId,
        golferFirstName = golferFirstName,
        golferLastName = golferLastName,
        score = score,
        countOfHoleScores = countOfHoleScores,
        golflinkNo = golflinkNo,
        clubState = clubState?.let { stateString ->
            StateInfo(
                alpha2 = "",
                name = stateString,
                shortName = stateString
            )
        },
        clubName = clubName,
        playingPartnerGolferFirstName = playingPartnerGolferFirstName,
        playingPartnerGolferLastName = playingPartnerGolferLastName,
        golfLinkHandicap = golfLinkHandicap,
        playingPartnerGolfLinkHandicap = playingPartnerGolfLinkHandicap,
        scratchRating = scratchRating,
        slopeRating = slopeRating,
        compType = compType,
        isSubmitted = isSubmitted,
        teeColor = teeColor
    )
}