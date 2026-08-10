package com.sogo.golf.msl.features.review_scores.presentation

import com.sogo.golf.msl.domain.model.HoleScore
import com.sogo.golf.msl.domain.model.Round

data class RoundStatsSummary(
    val playedHoles: Int,
    val totalPutts: Int,
    val puttsTracked: Int,
    val greensHit: Int,
    val greensTracked: Int,
    val fairwaysHit: Int,
    val fairwaysTracked: Int,
    val fairwayEligibleHoles: Int
)

/** Main golfer only; not-played and never-entered holes do not affect values or coverage. */
internal fun Round.playedHoleScoresForStats(): List<HoleScore> = holeScores
    .filter { hole ->
        hole.isHoleNotPlayed != true && (hole.strokes > 0 || hole.isBallPickedUp == true)
    }
    .sortedBy { it.holeNumber }

internal fun Round.roundStatsSummary(): RoundStatsSummary {
    val played = playedHoleScoresForStats()
    // A golfer may intentionally play to the fairway on any hole, including a long par 3.
    return RoundStatsSummary(
        playedHoles = played.size,
        totalPutts = played.mapNotNull { it.stats?.putts }.sum(),
        puttsTracked = played.count { it.stats?.putts != null },
        greensHit = played.count { it.stats?.greenHit == true },
        greensTracked = played.count { it.stats?.greenHit != null },
        fairwaysHit = played.count { it.stats?.fairwayHit == true },
        fairwaysTracked = played.count { it.stats?.fairwayHit != null },
        fairwayEligibleHoles = played.size
    )
}
