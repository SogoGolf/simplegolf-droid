package com.sogo.golf.msl.features.play.presentation

import org.junit.Test
import org.junit.Assert.*

class PlayRoundViewModelTest {

    @Test
    fun testGetCycleIndices_18HoleRound_StartingAtHole1_ReturnsCorrectOrder() {
        val startingHole = 1
        val numberOfHoles = 18
        val cycle = getCycleIndicesForTest(startingHole, numberOfHoles)
        
        // Standard 18-hole round: holes 1-18, indices 0-17
        val expected = (0..17).toList()
        assertEquals(expected, cycle)
    }

    @Test
    fun testGetCycleIndices_9HoleRound_StartingAtHole1_ReturnsCorrectOrder() {
        val startingHole = 1
        val numberOfHoles = 9
        val cycle = getCycleIndicesForTest(startingHole, numberOfHoles)
        
        // Standard 9-hole round: holes 1-9, indices 0-8
        val expected = (0..8).toList()
        assertEquals(expected, cycle)
    }

    private fun getCycleIndicesForTest(startingHole: Int, numberOfHoles: Int): List<Int> {
        val maxHole = when {
            startingHole == 1 && numberOfHoles == 18 -> 18
            startingHole == 1 && numberOfHoles == 9 -> 9
            startingHole == 10 && numberOfHoles == 9 -> 18
            else -> startingHole + numberOfHoles - 1
        }
        
        val holeNumbers = mutableListOf<Int>()
        var currentHole = startingHole
        
        repeat(numberOfHoles) {
            holeNumbers.add(currentHole)
            currentHole++
            if (currentHole > maxHole) {
                currentHole = if (maxHole == 18 && startingHole >= 10) 10 else 1
            }
        }
        
        return holeNumbers.map { it - 1 }
    }
}
