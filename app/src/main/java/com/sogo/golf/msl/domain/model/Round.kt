package com.sogo.golf.msl.domain.model

import org.threeten.bp.LocalDateTime
import java.util.UUID

data class Round(
    val id: String = UUID.randomUUID().toString(),
    val uuid: String? = null,
    val entityId: String? = null,
    val roundPlayedOff: Double? = null,
    val dailyHandicap: Double? = null,
    val golfLinkHandicap: Double? = null,
    val golflinkNo: String? = null,
    val scorecardUrl: String? = null,
    val roundRefCode: String? = null,
    val roundDate: LocalDateTime? = null,
    val roundType: String = "",
    val startTime: LocalDateTime? = null,
    val finishTime: LocalDateTime? = null,
    val scratchRating: Float? = null,
    val slopeRating: Float? = null,
    val submittedTime: LocalDateTime? = null,
    val compScoreTotal: Int? = null,
    val whsFrontScoreStableford: Int? = null,
    val whsBackScoreStableford: Int? = null,
    val whsFrontScorePar: Int? = null,
    val whsBackScorePar: Int? = null,
    val whsFrontScoreStroke: Int? = null,
    val whsBackScoreStroke: Int? = null,
    val whsFrontScoreMaximumScore: Int? = null,
    val whsBackScoreMaximumScore: Int? = null,
    val roundApprovedBy: String? = null,
    val comment: String? = null,
    val createdDate: LocalDateTime? = null,
    val updateDate: String? = null,
    val updateUserId: String? = null,
    val courseId: String? = null,
    val courseUuid: String? = null,
    val isClubSubmitted: Boolean? = null,
    val isSubmitted: Boolean? = null,
    val isMarkedForReview: Boolean? = null,
    val isApproved: Boolean? = null,
    val teeColor: String? = null,
    val isClubComp: Boolean? = null,
    val isDeleted: Boolean? = null,
    val isAbandoned: Boolean? = null,
    val clubId: String? = null,
    val clubUuid: String? = null,
    val golferId: String? = null,
    val golferGender: String? = null,
    val golferEmail: String? = null,
    val golferFirstName: String? = null,
    val golferLastName: String? = null,
    val golferGLNumber: String? = null,
    val golferImageUrl: String? = null,
    val clubState: StateInfo? = null,
    val clubName: String? = null,
    val markerFirstName: String? = null,
    val markerLastName: String? = null,
    val markerEmail: String? = null,
    val markerGLNumber: String? = null,
    val compType: String? = null,
    val holeScores: List<HoleScore> = emptyList(),
    val sogoAppVersion: String? = null,
    val transactionId: String? = null,
    val playingPartnerRound: PlayingPartnerRound? = null,
    val roundApprovalSignatureUrl: String? = null,
    val thirdPartyScorecardId: String? = null,
    val mslMetaData: MslMetaData? = null,
    val lastUpdated: Long = 0L,
    val isSynced: Boolean = false
)

data class StateInfo(
    val alpha2: String = "",
    val name: String = "",
    val shortName: String = ""
)

data class HoleScore(
    val par: Int = 0,
    val playedPar: Int? = null,
    val parCourse: Boolean? = null,
    val holeNumber: Int = 0,
    val strokes: Int = 0,
    val score: Float = 0f,
    val whsStableford: Float? = null,
    val whsPar: Int? = null,
    val whsStroke: Int? = null,
    val whsMaximumScore: Int? = null,
    val playedHcpIndex: Int? = null,
    val index1: Int = 0,
    val index2: Int = 0,
    val index3: Int? = null,
    val meters: Int = 0,
    val isBallPickedUp: Boolean? = false,
    val isHoleNotPlayed: Boolean? = false,
    val startTime: String? = null,
    val startLocation: Location? = null,
    val finishTime: String? = null,
    val finishLocation: Location? = null
)

data class Location(
    val type: String = "Point",
    val coordinates: List<Double> = emptyList()
)

data class PlayingPartnerRound(
    val uuid: String? = null,
    val entityId: String? = null,
    val dailyHandicap: Float? = null,
    val golfLinkHandicap: Float? = null,
    val thirdPartyRoundId: Int? = null,
    val roundDate: LocalDateTime? = null,
    val roundType: String? = null,
    val startTime: LocalDateTime? = null,
    val finishTime: LocalDateTime? = null,
    val submittedTime: LocalDateTime? = null,
    val scratchRating: Float? = null,
    val slopeRating: Float? = null,
    val compScoreTotal: Int? = null,
    val teeColor: String? = null,
    val compType: String? = null,
    val isSubmitted: Boolean? = null,
    val golferId: String? = null,
    val golferFirstName: String? = null,
    val golferLastName: String? = null,
    val golferGLNumber: String? = null,
    val golflinkNo: String? = null,
    val golferEmail: String? = null,
    val golferImageUrl: String? = null,
    val golferGender: String? = null,
    val holeScores: List<HoleScore> = emptyList(),
    val roundApprovalSignatureUrl: String? = null,
    val createdDate: LocalDateTime? = null,
    val updateDate: LocalDateTime? = null,
    val deleteDate: LocalDateTime? = null
)

data class MslMetaData(
    val isIncludeRoundOnSogo: Boolean? = null
)
