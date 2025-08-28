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
    fun testGetHoleIndex_EdgeCase_InvalidHoleNumber_ReturnsNegativeOne() {
        val startingHole = 1
        val numberOfHoles = 18
        
        assertEquals(-1, getHoleIndexForTest(19, startingHole, numberOfHoles))
        assertEquals(-1, getHoleIndexForTest(0, startingHole, numberOfHoles))
    }

    private fun getCycleIndicesForTest(startingHole: Int, numberOfHoles: Int): List<Int> {
        val size = numberOfHoles
        val startIndex = startingHole - 1
        return (startIndex until size).toList() + (0 until startIndex).toList()
    }

    private fun getHoleIndexForTest(holeNumber: Int, startingHole: Int, numberOfHoles: Int): Int {
        val cycle = getCycleIndicesForTest(startingHole, numberOfHoles)
        return cycle.indexOf(holeNumber - 1)
    }
}
