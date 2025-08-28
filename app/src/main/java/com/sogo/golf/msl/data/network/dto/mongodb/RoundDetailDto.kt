package com.sogo.golf.msl.data.network.dto.mongodb

import com.google.gson.annotations.SerializedName
import com.sogo.golf.msl.domain.model.mongodb.RoundDetail
import com.sogo.golf.msl.domain.model.mongodb.RoundDetailHoleScore
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

data class RoundDetailDto(
    @SerializedName("roundDate")
    val roundDate: String? = null,
    
    @SerializedName("holeScores")
    val holeScores: List<RoundDetailHoleScoreDto> = emptyList(),
    
    @SerializedName("compType")
    val compType: String? = null,
    
    @SerializedName("clubName")
    val clubName: String? = null,
    
    @SerializedName("scratchRating")
    val scratchRating: Float? = null,
    
    @SerializedName("slopeRating")
    val slopeRating: Float? = null,
    
    @SerializedName("teeColor")
    val teeColor: String? = null,
    
    @SerializedName("startTime")
    val startTime: String? = null
)

data class RoundDetailHoleScoreDto(
    @SerializedName("par")
    val par: Int = 0,
    
    @SerializedName("playedPar")
    val playedPar: Int? = null,
    
    @SerializedName("parCourse")
    val parCourse: Boolean? = null,
    
    @SerializedName("holeNumber")
    val holeNumber: Int = 0,
    
    @SerializedName("strokes")
    val strokes: Int = 0,
    
    @SerializedName("score")
    val score: Int = 0,
    
    @SerializedName("whsStableford")
    val whsStableford: Int? = null,
    
    @SerializedName("whsPar")
    val whsPar: Int? = null,
    
    @SerializedName("whsStroke")
    val whsStroke: Int? = null,
    
    @SerializedName("whsMaximumScore")
    val whsMaximumScore: Int? = null,
    
    @SerializedName("playedHcpIndex")
    val playedHcpIndex: Int? = null,
    
    @SerializedName("index1")
    val index1: Int = 0,
    
    @SerializedName("index2")
    val index2: Int = 0,
    
    @SerializedName("index3")
    val index3: Int = 0,
    
    @SerializedName("meters")
    val meters: Int = 0,
    
    @SerializedName("isBallPickedUp")
    val isBallPickedUp: Boolean = false,
    
    @SerializedName("isHoleNotPlayed")
    val isHoleNotPlayed: Boolean = false,
    
    @SerializedName("startTime")
    val startTime: String? = null,
    
    @SerializedName("startLocation")
    val startLocation: String? = null,
    
    @SerializedName("finishTime")
    val finishTime: String? = null,
    
    @SerializedName("finishLocation")
    val finishLocation: String? = null
)

fun RoundDetailDto.toDomain(): RoundDetail {
    return RoundDetail(
        roundDate = roundDate?.let { dateString ->
            try {
                when {
                    dateString.contains("Z") -> {
                        // UTC time - convert to LocalDateTime
                        val instant = org.threeten.bp.Instant.parse(dateString)
                        LocalDateTime.ofInstant(instant, org.threeten.bp.ZoneId.systemDefault())
                    }
                    dateString.contains("T") -> {
                        // Local date-time format
                        LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    }
                    else -> {
                        // Fallback
                        LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("RoundDetailDto", "Failed to parse roundDate: $dateString", e)
                null
            }
        },
        holeScores = holeScores.map { it.toDomain() },
        compType = compType,
        clubName = clubName,
        scratchRating = scratchRating,
        slopeRating = slopeRating,
        teeColor = teeColor,
        startTime = startTime?.let { timeString ->
            try {
                when {
                    timeString.contains("Z") -> {
                        // UTC time - convert to LocalDateTime
                        val instant = org.threeten.bp.Instant.parse(timeString)
                        LocalDateTime.ofInstant(instant, org.threeten.bp.ZoneId.systemDefault())
                    }
                    timeString.contains("T") -> {
                        // Local date-time format
                        LocalDateTime.parse(timeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    }
                    else -> {
                        // Fallback
                        LocalDateTime.parse(timeString, DateTimeFormatter.ISO_DATE_TIME)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("RoundDetailDto", "Failed to parse startTime: $timeString", e)
                null
            }
        }
    )
}

fun RoundDetailHoleScoreDto.toDomain(): RoundDetailHoleScore {
    return RoundDetailHoleScore(
        par = par,
        playedPar = playedPar,
        parCourse = parCourse,
        holeNumber = holeNumber,
        strokes = strokes,
        score = score,
        whsStableford = whsStableford,
        whsPar = whsPar,
        whsStroke = whsStroke,
        whsMaximumScore = whsMaximumScore,
        playedHcpIndex = playedHcpIndex,
        index1 = index1,
        index2 = index2,
        index3 = index3,
        meters = meters,
        isBallPickedUp = isBallPickedUp,
        isHoleNotPlayed = isHoleNotPlayed,
        startTime = startTime,
        startLocation = startLocation,
        finishTime = finishTime,
        finishLocation = finishLocation
    )
}
