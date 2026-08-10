package com.sogo.golf.msl.features.review_scores.presentation

import com.sogo.golf.msl.domain.model.HoleScore
import com.sogo.golf.msl.domain.model.HoleStats
import com.sogo.golf.msl.domain.model.Round
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewRoundMergeTest {

    @Test
    fun pendingStatsMergeIntoLatestRoomScoresWithoutRestoringAStaleRound() {
        val oldStats = HoleStats(putts = 1)
        val pendingStats = HoleStats(putts = 2, greenHit = true)
        val authoritative = Round(
            id = "round-1",
            isSynced = true,
            holeScores = listOf(
                HoleScore(holeNumber = 1, strokes = 5, stats = oldStats),
                HoleScore(holeNumber = 2, strokes = 4)
            )
        )

        val merged = authoritative.withPendingReviewStats(
            mapOf(1 to pendingStats, 99 to HoleStats(putts = 9))
        )

        assertEquals(5, merged.holeScores.first { it.holeNumber == 1 }.strokes)
        assertEquals(pendingStats, merged.holeScores.first { it.holeNumber == 1 }.stats)
        assertEquals(4, merged.holeScores.first { it.holeNumber == 2 }.strokes)
        assertNull(merged.holeScores.first { it.holeNumber == 2 }.stats)
        assertFalse(merged.isSynced)
        assertEquals(oldStats, authoritative.holeScores.first().stats)
    }

    @Test
    fun onlyLatestLoadForActiveRoundCanPublish() {
        assertTrue(canPublishReviewRoundLoad(2L, 2L, "round-1", "round-1"))
        assertFalse(canPublishReviewRoundLoad(1L, 2L, "round-1", "round-1"))
        assertFalse(canPublishReviewRoundLoad(2L, 2L, "round-1", "round-2"))
    }
}
