package com.sogo.golf.msl.data.network.dto.mongodb

import com.google.gson.annotations.SerializedName
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.model.HoleScore
import com.sogo.golf.msl.domain.model.PlayingPartnerRound
import com.sogo.golf.msl.domain.model.MslMetaData
import com.sogo.golf.msl.domain.model.StateInfo
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

// Helper to convert LocalDateTime (in system's default timezone) to UTC ISO-8601 strings
// IMPORTANT: LocalDateTime is treated as being in the system's default timezone,
// then converted to UTC for storage in MongoDB
private fun formatUtc(dateTime: LocalDateTime?): String? = dateTime
    ?.atZone(org.threeten.bp.ZoneId.systemDefault())  // Treat as local time
    ?.withZoneSameInstant(org.threeten.bp.ZoneOffset.UTC)  // Convert to UTC
    ?.toInstant()
    ?.toString()

// For date-only semantics: always emit midnight at UTC (UTC-0) for the given date
private fun formatDateOnlyUtc(dateTime: LocalDateTime?): String? = dateTime
    ?.toLocalDate()
    ?.atStartOfDay(org.threeten.bp.ZoneOffset.UTC)
    ?.toInstant()
    ?.toString()

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
    
    @SerializedName("scorecardUrl")
    val scorecardUrl: String? = null,
    
    @SerializedName("roundRefCode")
    val roundRefCode: String? = null,
    
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
    
    @SerializedName("whsFrontScoreStableford")
    val whsFrontScoreStableford: Int? = null,
    
    @SerializedName("whsBackScoreStableford")
    val whsBackScoreStableford: Int? = null,
    
    @SerializedName("whsFrontScorePar")
    val whsFrontScorePar: Int? = null,
    
    @SerializedName("whsBackScorePar")
    val whsBackScorePar: Int? = null,
    
    @SerializedName("whsFrontScoreStroke")
    val whsFrontScoreStroke: Int? = null,
    
    @SerializedName("whsBackScoreStroke")
    val whsBackScoreStroke: Int? = null,
    
    @SerializedName("whsFrontScoreMaximumScore")
    val whsFrontScoreMaximumScore: Int? = null,
    
    @SerializedName("whsBackScoreMaximumScore")
    val whsBackScoreMaximumScore: Int? = null,
    
    @SerializedName("roundApprovedBy")
    val roundApprovedBy: String? = null,
    
    @SerializedName("comment")
    val comment: String? = null,
    
    @SerializedName("updateDate")
    val updateDate: String? = null,
    
    @SerializedName("updateUserId")
    val updateUserId: String? = null,
    
    @SerializedName("courseId")
    val courseId: String? = null,
    
    @SerializedName("courseUuid")
    val courseUuid: String? = null,
    
    @SerializedName("isClubSubmitted")
    val isClubSubmitted: Boolean? = null,
    
    @SerializedName("isValidated")
    val isValidated: Boolean? = null,
    
    @SerializedName("isMarkedForReview")
    val isMarkedForReview: Boolean? = null,
    
    @SerializedName("isDeleted")
    val isDeleted: Boolean? = null,
    
    @SerializedName("isAbandoned")
    val isAbandoned: Boolean? = null,
    
    @SerializedName("clubId")
    val clubId: String? = null,
    
    @SerializedName("clubUuid")
    val clubUuid: String? = null,
    
    @SerializedName("golferId")
    val golferId: String? = null,
    
    @SerializedName("golferGender")
    val golferGender: String? = null,
    
    @SerializedName("golferEmail")
    val golferEmail: String? = null,
    
    @SerializedName("golferImageUrl")
    val golferImageUrl: String? = null,
    
    @SerializedName("clubState")
    val clubState: StateInfoDto? = null,
    
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
    
    @SerializedName("markerEmail")
    val markerEmail: String? = null,
    
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
    
    @SerializedName("sogoAppVersion")
    val sogoAppVersion: String? = null,
    
    @SerializedName("transactionId")
    val transactionId: String? = null,
    
    @SerializedName("playingPartnerRound")
    val playingPartnerRound: PlayingPartnerRoundDto? = null,
    
    @SerializedName("roundApprovalSignatureUrl")
    val roundApprovalSignatureUrl: String? = null,
    
    @SerializedName("thirdPartyScorecardId")
    val thirdPartyScorecardId: String? = null,
    
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

data class StateInfoDto(
    @SerializedName("alpha2")
    val alpha2: String = "",
    
    @SerializedName("name")
    val name: String = "",
    
    @SerializedName("shortName")
    val shortName: String = ""
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
        scorecardUrl = scorecardUrl,
        roundRefCode = roundRefCode,
        roundDate = formatUtc(roundDate),
        roundType = roundType,
        startTime = formatUtc(startTime),
        finishTime = formatUtc(finishTime),
        scratchRating = scratchRating,
        slopeRating = slopeRating,
        submittedTime = formatUtc(submittedTime),
        compScoreTotal = compScoreTotal,
        whsFrontScoreStableford = whsFrontScoreStableford,
        whsBackScoreStableford = whsBackScoreStableford,
        whsFrontScorePar = whsFrontScorePar,
        whsBackScorePar = whsBackScorePar,
        whsFrontScoreStroke = whsFrontScoreStroke,
        whsBackScoreStroke = whsBackScoreStroke,
        whsFrontScoreMaximumScore = whsFrontScoreMaximumScore,
        whsBackScoreMaximumScore = whsBackScoreMaximumScore,
        roundApprovedBy = roundApprovedBy,
        comment = comment,
        updateDate = updateDate,
        updateUserId = updateUserId,
        courseId = courseId,
        courseUuid = courseUuid,
        isClubSubmitted = isClubSubmitted,
        isValidated = isValidated,
        isMarkedForReview = isMarkedForReview,
        isDeleted = isDeleted,
        isAbandoned = isAbandoned,
        clubId = clubId,
        clubUuid = clubUuid,
        golferId = golferId,
        golferGender = golferGender,
        golferEmail = golferEmail,
        golferImageUrl = golferImageUrl,
        clubState = clubState?.toDto(),
        clubName = clubName,
        golferFirstName = golferFirstName,
        golferLastName = golferLastName,
        golferGLNumber = golferGLNumber,
        markerFirstName = markerFirstName,
        markerLastName = markerLastName,
        markerEmail = markerEmail,
        markerGLNumber = markerGLNumber,
        compType = compType,
        teeColor = teeColor,
        isClubComp = isClubComp,
        isSubmitted = isSubmitted,
        isApproved = isApproved,
        holeScores = holeScores.map { it.toDto() },
        sogoAppVersion = sogoAppVersion,
        transactionId = transactionId,
        playingPartnerRound = playingPartnerRound?.toDto(),
        roundApprovalSignatureUrl = roundApprovalSignatureUrl,
        thirdPartyScorecardId = thirdPartyScorecardId,
        mslMetaData = mslMetaData?.toDto(),
        createdDate = formatUtc(createdDate)
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
roundDate = formatUtc(roundDate),
        roundType = roundType,
        startTime = formatUtc(startTime),
        finishTime = formatUtc(finishTime),
        scratchRating = scratchRating,
        slopeRating = slopeRating,
        compScoreTotal = compScoreTotal,
        teeColor = teeColor,
        compType = compType,
        isSubmitted = isSubmitted,
        golferGender = golferGender,
        holeScores = holeScores.map { it.toDto() },
        createdDate = formatUtc(createdDate)
    )
}

fun StateInfo.toDto(): StateInfoDto {
    return StateInfoDto(
        alpha2 = alpha2,
        name = name,
        shortName = shortName
    )
}

fun MslMetaData.toDto(): MslMetaDataDto {
    return MslMetaDataDto(
        isIncludeRoundOnSogo = isIncludeRoundOnSogo
    )
}
