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

    @Test
    fun testGetCycleIndices_9HoleRound_StartingAtHole5_ReturnsCorrectOrder() {
        val startingHole = 5
        val numberOfHoles = 9
        val cycle = getCycleIndicesForTest(startingHole, numberOfHoles)
        
        // 9-hole round starting at hole 5: holes 5,6,7,8,9,1,2,3,4
        // This should wrap from hole 9 back to hole 1 (1-9 range)
        val expected = listOf(4, 5, 6, 7, 8, 0, 1, 2, 3)
        assertEquals(expected, cycle)
    }

    @Test
    fun testGetCycleIndices_10to18HoleRound_StartingAtHole12_ReturnsCorrectOrder() {
        val startingHole = 12
        val numberOfHoles = 9
        val cycle = getCycleIndicesForTest(startingHole, numberOfHoles)
        
        // Should generate indices for holes: 12,13,14,15,16,17,18,10,11
        val expected = listOf(11, 12, 13, 14, 15, 16, 17, 9, 10)
        assertEquals(expected, cycle)
    }

    @Test
    fun testGetCycleIndices_10to18HoleRound_StartingAtHole10_ReturnsCorrectOrder() {
        val startingHole = 10
        val numberOfHoles = 9
        val cycle = getCycleIndicesForTest(startingHole, numberOfHoles)
        
        // Should generate indices for holes: 10,11,12,13,14,15,16,17,18
        val expected = listOf(9, 10, 11, 12, 13, 14, 15, 16, 17)
        assertEquals(expected, cycle)
    }

    @Test
    fun testGetCycleIndices_18HoleRound_StartingAtHole10_ReturnsCorrectOrder() {
        val startingHole = 10
        val numberOfHoles = 18
        val cycle = getCycleIndicesForTest(startingHole, numberOfHoles)
        
        println("DEBUG: startingHole=$startingHole, numberOfHoles=$numberOfHoles")
        println("DEBUG: actual cycle=$cycle")
        
        // Should generate indices for holes: 10,11,12,13,14,15,16,17,18,1,2,3,4,5,6,7,8,9
        val expected = listOf(9, 10, 11, 12, 13, 14, 15, 16, 17, 0, 1, 2, 3, 4, 5, 6, 7, 8)
        println("DEBUG: expected cycle=$expected")
        assertEquals(expected, cycle)
    }

    private fun getCycleIndicesForTest(startingHole: Int, numberOfHoles: Int): List<Int> {
        val maxHole = when {
            numberOfHoles == 18 -> 18  // Any 18-hole round uses holes 1-18
            startingHole >= 10 && numberOfHoles == 9 -> 18  // 10-18 hole range
            startingHole >= 1 && startingHole <= 9 && numberOfHoles == 9 -> 9  // 1-9 hole range
            else -> startingHole + numberOfHoles - 1
        }
        
        val holeNumbers = mutableListOf<Int>()
        var currentHole = startingHole
        
        repeat(numberOfHoles) {
            holeNumbers.add(currentHole)
            currentHole++
            if (currentHole > maxHole) {
                currentHole = when {
                    // 10-18 hole rounds: wrap from 18 back to 10
                    startingHole >= 10 && numberOfHoles == 9 -> 10
                    // All other rounds: wrap back to 1
                    else -> 1
                }
            }
        }
        
        return holeNumbers.map { it - 1 }
    }
}
