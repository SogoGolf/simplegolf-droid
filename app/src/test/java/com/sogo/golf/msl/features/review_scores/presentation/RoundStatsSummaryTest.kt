package com.sogo.golf.msl.features.review_scores.presentation

import com.sogo.golf.msl.domain.model.HoleScore
import com.sogo.golf.msl.domain.model.HoleStats
import com.sogo.golf.msl.domain.model.Round
import org.junit.Assert.assertEquals
import org.junit.Test

class RoundStatsSummaryTest {

    @Test
    fun summaryCountsExplicitZeroAndIncludesParThreeFairways() {
        val round = Round(
            holeScores = listOf(
                hole(1, par = 4, stats = HoleStats(putts = 0, greenHit = true, fairwayHit = true)),
                hole(2, par = 3, stats = HoleStats(putts = 2, greenHit = false, fairwayHit = true)),
                hole(3, par = 5, stats = HoleStats(putts = null, greenHit = null, fairwayHit = false)),
                hole(4, par = 4, notPlayed = true, stats = HoleStats(putts = 9, greenHit = true, fairwayHit = true))
            )
        )

        assertEquals(
            RoundStatsSummary(
                playedHoles = 3,
                totalPutts = 2,
                puttsTracked = 2,
                greensHit = 1,
                greensTracked = 2,
                fairwaysHit = 2,
                fairwaysTracked = 3,
                fairwayEligibleHoles = 3
            ),
            round.roundStatsSummary()
        )
    }

    @Test
    fun playedHolesSupportBackNineAndPickedUpWhileSkippingUnentered() {
        val round = Round(
            holeScores = (10..18).map { number ->
                when (number) {
                    12 -> hole(number, strokes = 0, pickedUp = true)
                    15 -> hole(number, strokes = 0)
                    17 -> hole(number, notPlayed = true)
                    else -> hole(number)
                }
            }.reversed()
        )

        assertEquals(
            listOf(10, 11, 12, 13, 14, 16, 18),
            round.playedHoleScoresForStats().map { it.holeNumber }
        )
    }

    @Test
    fun frontNineUsesOnlyTheNineActuallyPlayedHoles() {
        val round = Round(
            holeScores = (1..18).map { number ->
                if (number <= 9) hole(number) else hole(number, strokes = 0)
            }
        )

        assertEquals(9, round.roundStatsSummary().playedHoles)
        assertEquals((1..9).toList(), round.playedHoleScoresForStats().map { it.holeNumber })
    }

    private fun hole(
        number: Int,
        par: Int = 4,
        strokes: Int = 4,
        notPlayed: Boolean = false,
        pickedUp: Boolean = false,
        stats: HoleStats? = null
    ) = HoleScore(
        holeNumber = number,
        par = par,
        strokes = strokes,
        isHoleNotPlayed = notPlayed,
        isBallPickedUp = pickedUp,
        stats = stats
    )
}
