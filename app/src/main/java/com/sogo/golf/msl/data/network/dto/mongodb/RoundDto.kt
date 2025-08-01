package com.sogo.golf.msl.data.network.dto.mongodb

import com.google.gson.annotations.SerializedName
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.model.HoleScore
import com.sogo.golf.msl.domain.model.PlayingPartnerRound
import com.sogo.golf.msl.domain.model.MslMetaData
import org.threeten.bp.format.DateTimeFormatter

data class RoundDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("uuid")
    val uuid: String? = null,
    
    @SerializedName("entityId")
    val entityId: String? = null,
    
    @SerializedName("roundPlayedOff")
    val roundPlayedOff: Double? = null,
    
    @SerializedName("dailyHandicap")
    val dailyHandicap: Double? = null,
    
    @SerializedName("golfLinkHandicap")
    val golfLinkHandicap: Double? = null,
    
    @SerializedName("golflinkNo")
    val golflinkNo: String? = null,
    
    @SerializedName("roundDate")
    val roundDate: String? = null,
    
    @SerializedName("roundType")
    val roundType: String = "",
    
    @SerializedName("startTime")
    val startTime: String? = null,
    
    @SerializedName("finishTime")
    val finishTime: String? = null,
    
    @SerializedName("scratchRating")
    val scratchRating: Float? = null,
    
    @SerializedName("slopeRating")
    val slopeRating: Float? = null,
    
    @SerializedName("submittedTime")
    val submittedTime: String? = null,
    
    @SerializedName("compScoreTotal")
    val compScoreTotal: Int? = null,
    
    @SerializedName("clubName")
    val clubName: String? = null,
    
    @SerializedName("golferFirstName")
    val golferFirstName: String? = null,
    
    @SerializedName("golferLastName")
    val golferLastName: String? = null,
    
    @SerializedName("golferGLNumber")
    val golferGLNumber: String? = null,
    
    @SerializedName("markerFirstName")
    val markerFirstName: String? = null,
    
    @SerializedName("markerLastName")
    val markerLastName: String? = null,
    
    @SerializedName("markerGLNumber")
    val markerGLNumber: String? = null,
    
    @SerializedName("compType")
    val compType: String? = null,
    
    @SerializedName("teeColor")
    val teeColor: String? = null,
    
    @SerializedName("isClubComp")
    val isClubComp: Boolean? = null,
    
    @SerializedName("isSubmitted")
    val isSubmitted: Boolean? = null,
    
    @SerializedName("isApproved")
    val isApproved: Boolean? = null,
    
    @SerializedName("holeScores")
    val holeScores: List<HoleScoreDto> = emptyList(),
    
    @SerializedName("playingPartnerRound")
    val playingPartnerRound: PlayingPartnerRoundDto? = null,
    
    @SerializedName("mslMetaData")
    val mslMetaData: MslMetaDataDto? = null,
    
    @SerializedName("createdDate")
    val createdDate: String? = null
)

data class HoleScoreDto(
    @SerializedName("holeNumber")
    val holeNumber: Int = 0,
    
    @SerializedName("par")
    val par: Int = 0,
    
    @SerializedName("strokes")
    val strokes: Int = 0,
    
    @SerializedName("score")
    val score: Float = 0f,
    
    @SerializedName("index1")
    val index1: Int = 0,
    
    @SerializedName("index2")
    val index2: Int = 0,
    
    @SerializedName("index3")
    val index3: Int? = null,
    
    @SerializedName("meters")
    val meters: Int = 0,
    
    @SerializedName("isBallPickedUp")
    val isBallPickedUp: Boolean? = false
)

data class PlayingPartnerRoundDto(
    @SerializedName("uuid")
    val uuid: String? = null,
    
    @SerializedName("entityId")
    val entityId: String? = null,
    
    @SerializedName("dailyHandicap")
    val dailyHandicap: Float? = null,
    
    @SerializedName("golfLinkHandicap")
    val golfLinkHandicap: Float? = null,
    
    @SerializedName("golferFirstName")
    val golferFirstName: String? = null,
    
    @SerializedName("golferLastName")
    val golferLastName: String? = null,
    
    @SerializedName("golferGLNumber")
    val golferGLNumber: String? = null,
    
    @SerializedName("golflinkNo")
    val golflinkNo: String? = null,
    
    @SerializedName("roundDate")
    val roundDate: String? = null,
    
    @SerializedName("roundType")
    val roundType: String? = null,
    
    @SerializedName("startTime")
    val startTime: String? = null,
    
    @SerializedName("finishTime")
    val finishTime: String? = null,
    
    @SerializedName("scratchRating")
    val scratchRating: Float? = null,
    
    @SerializedName("slopeRating")
    val slopeRating: Float? = null,
    
    @SerializedName("compScoreTotal")
    val compScoreTotal: Int? = null,
    
    @SerializedName("teeColor")
    val teeColor: String? = null,
    
    @SerializedName("compType")
    val compType: String? = null,
    
    @SerializedName("isSubmitted")
    val isSubmitted: Boolean? = null,
    
    @SerializedName("golferGender")
    val golferGender: String? = null,
    
    @SerializedName("holeScores")
    val holeScores: List<HoleScoreDto> = emptyList(),
    
    @SerializedName("createdDate")
    val createdDate: String? = null
)

data class MslMetaDataDto(
    @SerializedName("isIncludeRoundOnSogo")
    val isIncludeRoundOnSogo: Boolean? = null
)

fun Round.toDto(): RoundDto {
    return RoundDto(
        id = id,
        uuid = uuid,
        entityId = entityId,
        roundPlayedOff = roundPlayedOff,
        dailyHandicap = dailyHandicap,
        golfLinkHandicap = golfLinkHandicap,
        golflinkNo = golflinkNo,
        roundDate = roundDate?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        roundType = roundType,
        startTime = startTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        finishTime = finishTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        scratchRating = scratchRating,
        slopeRating = slopeRating,
        submittedTime = submittedTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        compScoreTotal = compScoreTotal,
        clubName = clubName,
        golferFirstName = golferFirstName,
        golferLastName = golferLastName,
        golferGLNumber = golferGLNumber,
        markerFirstName = markerFirstName,
        markerLastName = markerLastName,
        markerGLNumber = markerGLNumber,
        compType = compType,
        teeColor = teeColor,
        isClubComp = isClubComp,
        isSubmitted = isSubmitted,
        isApproved = isApproved,
        holeScores = holeScores.map { it.toDto() },
        playingPartnerRound = playingPartnerRound?.toDto(),
        mslMetaData = mslMetaData?.toDto(),
        createdDate = createdDate?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    )
}

fun HoleScore.toDto(): HoleScoreDto {
    return HoleScoreDto(
        holeNumber = holeNumber,
        par = par,
        strokes = strokes,
        score = score,
        index1 = index1,
        index2 = index2,
        index3 = index3,
        meters = meters,
        isBallPickedUp = isBallPickedUp
    )
}

fun PlayingPartnerRound.toDto(): PlayingPartnerRoundDto {
    return PlayingPartnerRoundDto(
        uuid = uuid,
        entityId = entityId,
        dailyHandicap = dailyHandicap,
        golfLinkHandicap = golfLinkHandicap,
        golferFirstName = golferFirstName,
        golferLastName = golferLastName,
        golferGLNumber = golferGLNumber,
        golflinkNo = golflinkNo,
        roundDate = roundDate?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        roundType = roundType,
        startTime = startTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        finishTime = finishTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        scratchRating = scratchRating,
        slopeRating = slopeRating,
        compScoreTotal = compScoreTotal,
        teeColor = teeColor,
        compType = compType,
        isSubmitted = isSubmitted,
        golferGender = golferGender,
        holeScores = holeScores.map { it.toDto() },
        createdDate = createdDate?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    )
}

fun MslMetaData.toDto(): MslMetaDataDto {
    return MslMetaDataDto(
        isIncludeRoundOnSogo = isIncludeRoundOnSogo
    )
}
