package com.sogo.golf.msl.features.play.presentation

import com.sogo.golf.msl.domain.model.HoleStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HoleStatsEditorStateTest {

    @Test
    fun untouchedNullableCountersRemainUntrackedWhenAnotherFieldChanges() {
        val result = HoleStatsEditorState.from(existing = null)
            .toggleGreenHit(true)
            .toHoleStats()

        assertTrue(result.greenHit == true)
        assertNull(result.putts)
        assertNull(result.bunkerShots)
        assertNull(result.penalties)
    }

    @Test
    fun explicitCounterZeroSurvivesRoundTrip() {
        val existing = HoleStats(putts = 0, bunkerShots = 0, penalties = 0)

        assertEquals(existing, HoleStatsEditorState.from(existing).toHoleStats())
        assertEquals(0, decrementTrackedCounter(1))
        assertNull(decrementTrackedCounter(null))
    }

    @Test
    fun fairwayMissIsSingleSelectAndOnlySerializedForMiss() {
        val missedLeft = HoleStatsEditorState()
            .toggleFairwayHit(false)
            .toggleFairwayMiss("left")
        val missedShort = missedLeft.toggleFairwayMiss("short")

        assertEquals("short", missedShort.toHoleStats().fairwayMiss)

        val hit = missedShort.toggleFairwayHit(true).toHoleStats()
        assertTrue(hit.fairwayHit == true)
        assertNull(hit.fairwayMiss)

        val unset = HoleStatsEditorState(fairwayHit = false, fairwayMiss = "right")
            .toggleFairwayHit(false)
            .toHoleStats()
        assertNull(unset.fairwayHit)
        assertNull(unset.fairwayMiss)

        val invalid = HoleStatsEditorState(fairwayHit = false)
            .toggleFairwayMiss("long")
        assertNull(invalid.fairwayMiss)
        assertEquals(
            HoleStatsEditorState(fairwayHit = true),
            HoleStatsEditorState(fairwayHit = true).toggleFairwayMiss("left")
        )
    }

    @Test
    fun parThreeFairwayStateRoundTripsWithEveryOtherField() {
        val existing = HoleStats(
            teeClub = "7I",
            fairwayHit = false,
            fairwayMiss = "left",
            approachClub = "PW",
            greenHit = false,
            approachMiss = "right,short",
            putts = 2,
            bunkerShots = 1,
            penalties = 0
        )

        val result = HoleStatsEditorState.from(existing).toHoleStats()

        assertEquals("7I", result.teeClub)
        assertFalse(result.fairwayHit!!)
        assertEquals("left", result.fairwayMiss)
        assertEquals("PW", result.approachClub)
        assertFalse(result.greenHit!!)
        assertEquals("right,short", result.approachMiss)
        assertEquals(2, result.putts)
        assertEquals(1, result.bunkerShots)
        assertEquals(0, result.penalties)
    }

    @Test
    fun approachMissUsesOneDirectionPerAxisAndClearsOnHit() {
        val state = HoleStatsEditorState()
            .toggleGreenHit(false)
            .toggleApproachMiss("left")
            .toggleApproachMiss("right")
            .toggleApproachMiss("short")

        assertEquals(setOf("right", "short"), state.approachMisses)
        assertEquals("right,short", state.toHoleStats().approachMiss)
        assertNull(state.toggleGreenHit(true).toHoleStats().approachMiss)
    }
}
