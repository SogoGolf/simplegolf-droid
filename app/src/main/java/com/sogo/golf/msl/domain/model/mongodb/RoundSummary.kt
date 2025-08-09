package com.sogo.golf.msl.domain.model.mongodb

import com.sogo.golf.msl.domain.model.StateInfo
import org.threeten.bp.LocalDateTime

data class RoundSummary(
    val roundDate: LocalDateTime? = null,
    val golferEmail: String? = null,
    val golferId: String? = null,
    val golferFirstName: String? = null,
    val golferLastName: String? = null,
    val score: Int? = null,
    val countOfHoleScores: Int? = null,
    val golflinkNo: String? = null,
    val clubState: StateInfo? = null,
    val clubName: String? = null,
    val playingPartnerGolferFirstName: String? = null,
    val playingPartnerGolferLastName: String? = null,
    val golfLinkHandicap: Float? = null,
    val playingPartnerGolfLinkHandicap: Float? = null,
    val scratchRating: Float? = null,
    val slopeRating: Float? = null,
    val compType: String? = null,
    val isSubmitted: Boolean? = null,
    val teeColor: String? = null
)