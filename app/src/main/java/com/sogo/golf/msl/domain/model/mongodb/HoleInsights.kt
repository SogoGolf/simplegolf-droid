package com.sogo.golf.msl.domain.model.mongodb

// Analytics for a single hole at a venue, from GET /holeInsights.
data class HoleInsights(
    val holeNumber: Int,
    val par: Int,
    val yourRoundsCount: Int,
    val yourAvg: Double?,
    val yourBest: Int?,
    val history: List<HoleInsightsItem>,
    val distribution: HoleInsightsDistribution?,
    val fieldGross: HoleInsightsFieldGross?,
    val fieldNet: HoleInsightsFieldNet?
)

data class HoleInsightsItem(
    val date: String?,
    val strokes: Int,
    val par: Int
)

data class HoleInsightsDistribution(
    val eagle: Int,
    val birdie: Int,
    val par: Int,
    val bogey: Int,
    val doublePlus: Int
) {
    val total: Int get() = eagle + birdie + par + bogey + doublePlus
}

// Gross field average for golfers within a daily-handicap band of you (today).
data class HoleInsightsFieldGross(
    val avg: Double,
    val count: Int,
    val hcpLow: Double,
    val hcpHigh: Double,
    val widened: Boolean
)

// Net field average across ALL golfers today (normalised by strokes received).
data class HoleInsightsFieldNet(
    val avg: Double,
    val count: Int
)
