package com.sogo.golf.msl.features.play.presentation

import org.junit.Test
import org.junit.Assert.*

class PlayRoundViewModelTest {

    @Test
    fun testGetCycleIndices_18HoleRound_StartingAtHole1_ReturnsCorrectOrder() {
        val startingHole = 1
        val numberOfHoles = 18
        val cycle = getCycleIndicesForTest(startingHole, numberOfHoles)
        
        val expected = (0..17).toList()
        assertEquals(expected, cycle)
    }

    @Test
    fun testGetCycleIndices_18HoleRound_StartingAtHole10_ReturnsCorrectOrder() {
        val startingHole = 10
        val numberOfHoles = 18
        val cycle = getCycleIndicesForTest(startingHole, numberOfHoles)
        
        val expected = listOf(9, 10, 11, 12, 13, 14, 15, 16, 17, 0, 1, 2, 3, 4, 5, 6, 7, 8)
        assertEquals(expected, cycle)
    }

    @Test
    fun testGetCycleIndices_9HoleRound_StartingAtHole5_ReturnsCorrectOrder() {
        val startingHole = 5
        val numberOfHoles = 9
        val cycle = getCycleIndicesForTest(startingHole, numberOfHoles)
        
        val expected = listOf(4, 5, 6, 7, 8, 0, 1, 2, 3)
        assertEquals(expected, cycle)
    }

    @Test
    fun testGetCycleIndices_9HoleRound_StartingAtHole1_ReturnsCorrectOrder() {
        val startingHole = 1
        val numberOfHoles = 9
        val cycle = getCycleIndicesForTest(startingHole, numberOfHoles)
        
        val expected = (0..8).toList()
        assertEquals(expected, cycle)
    }

    @Test
    fun testGetCycleIndices_9HoleRound_StartingAtHole9_ReturnsCorrectOrder() {
        val startingHole = 9
        val numberOfHoles = 9
        val cycle = getCycleIndicesForTest(startingHole, numberOfHoles)
        
        val expected = listOf(8, 0, 1, 2, 3, 4, 5, 6, 7)
        assertEquals(expected, cycle)
    }

    @Test
    fun testGetHoleIndex_18HoleRound_StartingAtHole10_ReturnsCorrectIndices() {
        val startingHole = 10
        val numberOfHoles = 18
        
        assertEquals(0, getHoleIndexForTest(10, startingHole, numberOfHoles))
        assertEquals(8, getHoleIndexForTest(18, startingHole, numberOfHoles))
        assertEquals(9, getHoleIndexForTest(1, startingHole, numberOfHoles))
        assertEquals(17, getHoleIndexForTest(9, startingHole, numberOfHoles))
    }

    @Test
    fun testGetHoleIndex_9HoleRound_StartingAtHole5_ReturnsCorrectIndices() {
        val startingHole = 5
        val numberOfHoles = 9
        
        assertEquals(0, getHoleIndexForTest(5, startingHole, numberOfHoles))
        assertEquals(4, getHoleIndexForTest(9, startingHole, numberOfHoles))
        assertEquals(5, getHoleIndexForTest(1, startingHole, numberOfHoles))
        assertEquals(8, getHoleIndexForTest(4, startingHole, numberOfHoles))
    }

    @Test
    fun testGetHoleIndex_18HoleRound_StartingAtHole1_ReturnsCorrectIndices() {
        val startingHole = 1
        val numberOfHoles = 18
        
        assertEquals(0, getHoleIndexForTest(1, startingHole, numberOfHoles))
        assertEquals(9, getHoleIndexForTest(10, startingHole, numberOfHoles))
        assertEquals(17, getHoleIndexForTest(18, startingHole, numberOfHoles))
    }

    @Test
    fun testGetHoleIndex_18HoleRound_StartingAtHole18_ReturnsCorrectIndices() {
        val startingHole = 18
        val numberOfHoles = 18
        
        assertEquals(0, getHoleIndexForTest(18, startingHole, numberOfHoles))
        assertEquals(1, getHoleIndexForTest(1, startingHole, numberOfHoles))
        assertEquals(17, getHoleIndexForTest(17, startingHole, numberOfHoles))
    }

    @Test
    fun testGetCycleIndices_10to18HoleRound_StartingAtHole12_ReturnsCorrectOrder() {
        val startingHole = 12
        val numberOfHoles = 9
        val cycle = getCycleIndicesForTest(startingHole, numberOfHoles)
        
        // For 10-18 hole round starting at 12: holes 12,13,14,15,16,17,18,10,11
        // Converted to 0-based indices: 11,12,13,14,15,16,17,9,10
        val expected = listOf(11, 12, 13, 14, 15, 16, 17, 9, 10)
        assertEquals(expected, cycle)
    }

    @Test
    fun testGetCycleIndices_10to18HoleRound_StartingAtHole10_ReturnsCorrectOrder() {
        val startingHole = 10
        val numberOfHoles = 9
        val cycle = getCycleIndicesForTest(startingHole, numberOfHoles)
        
        val expected = listOf(9, 10, 11, 12, 13, 14, 15, 16, 17)
        assertEquals(expected, cycle)
    }

    @Test
    fun testGetCycleIndices_10to18HoleRound_StartingAtHole18_ReturnsCorrectOrder() {
        val startingHole = 18
        val numberOfHoles = 9
        val cycle = getCycleIndicesForTest(startingHole, numberOfHoles)
        
        val expected = listOf(17, 9, 10, 11, 12, 13, 14, 15, 16)
        assertEquals(expected, cycle)
    }

    @Test
    fun testGetHoleIndex_EdgeCase_InvalidHoleNumber_ReturnsNegativeOne() {
        val startingHole = 1
        val numberOfHoles = 18
        
        assertEquals(-1, getHoleIndexForTest(19, startingHole, numberOfHoles))
        assertEquals(-1, getHoleIndexForTest(0, startingHole, numberOfHoles))
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

    private fun getHoleIndexForTest(holeNumber: Int, startingHole: Int, numberOfHoles: Int): Int {
        val cycle = getCycleIndicesForTest(startingHole, numberOfHoles)
        return cycle.indexOf(holeNumber - 1)
    }
}
