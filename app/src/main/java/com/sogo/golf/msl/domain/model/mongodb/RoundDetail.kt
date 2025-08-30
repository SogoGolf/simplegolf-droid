package com.sogo.golf.msl.domain.model.mongodb

import org.threeten.bp.LocalDateTime

data class RoundDetail(
    val roundDate: LocalDateTime? = null,
    val holeScores: List<RoundDetailHoleScore> = emptyList(),
    val compType: String? = null,
    val clubName: String? = null,
    val scratchRating: Float? = null,
    val slopeRating: Float? = null,
    val teeColor: String? = null,
    val startTime: LocalDateTime? = null
)

data class RoundDetailHoleScore(
    val par: Int = 0,
    val playedPar: Int? = null,
    val parCourse: Boolean? = null,
    val holeNumber: Int = 0,
    val strokes: Int = 0,
    val score: Int = 0,
    val whsStableford: Int? = null,
    val whsPar: Int? = null,
    val whsStroke: Int? = null,
    val whsMaximumScore: Int? = null,
    val playedHcpIndex: Int? = null,
    val index1: Int = 0,
    val index2: Int = 0,
    val index3: Int = 0,
    val meters: Int = 0,
    val isBallPickedUp: Boolean = false,
    val isHoleNotPlayed: Boolean = false,
    val startTime: String? = null,
    val startLocation: String? = null,
    val finishTime: String? = null,
    val finishLocation: String? = null
)
