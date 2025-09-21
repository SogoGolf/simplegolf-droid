package com.sogo.golf.msl.domain.usecase.scoring

import com.sogo.golf.msl.domain.model.HoleScoreForCalcs
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class CalcHoleNetParUseCaseTest {

    private lateinit var calcHoleNetParUseCase: CalcHoleNetParUseCase

    @Before
    fun setUp() {
        calcHoleNetParUseCase = CalcHoleNetParUseCase()
    }

    private fun createPar4Hole(index: Int = 10): HoleScoreForCalcs {
        return HoleScoreForCalcs(par = 4, index1 = index, index2 = index + 18, index3 = index + 36)
    }

    @Test
    fun testNetPar_ScratchGolfer_ReturnsOriginalPar() {
        val hole = createPar4Hole()
        val handicap = 0.0
        val result = calcHoleNetParUseCase.invoke(hole, handicap)
        assertEquals(4.0, result, 0.01)
    }

    @Test
    fun testNetPar_LowHandicap_NoStroke_ReturnsOriginalPar() {
        val hole = createPar4Hole(index = 15)
        val handicap = 5.0
        val result = calcHoleNetParUseCase.invoke(hole, handicap)
        assertEquals(4.0, result, 0.01)
    }

    @Test
    fun testNetPar_LowHandicap_WithStroke_ReturnsParPlus1() {
        val hole = createPar4Hole(index = 3)
        val handicap = 5.0
        val result = calcHoleNetParUseCase.invoke(hole, handicap)
        assertEquals(5.0, result, 0.01)
    }

    @Test
    fun testNetPar_MediumHandicap_NoStroke_ReturnsOriginalPar() {
        val hole = createPar4Hole(index = 15)
        val handicap = 12.0
        val result = calcHoleNetParUseCase.invoke(hole, handicap)
        assertEquals(4.0, result, 0.01)
    }

    @Test
    fun testNetPar_MediumHandicap_WithStroke_ReturnsParPlus1() {
        val hole = createPar4Hole(index = 8)
        val handicap = 12.0
        val result = calcHoleNetParUseCase.invoke(hole, handicap)
        assertEquals(5.0, result, 0.01)
    }

    @Test
    fun testNetPar_HighHandicap_NoStroke_ReturnsOriginalPar() {
        val hole = createPar4Hole(index = 15)
        val handicap = 18.0
        val result = calcHoleNetParUseCase.invoke(hole, handicap)
        assertEquals(5.0, result, 0.01)
    }

    @Test
    fun testNetPar_HighHandicap_WithStroke_ReturnsParPlus1() {
        val hole = createPar4Hole(index = 1)
        val handicap = 18.0
        val result = calcHoleNetParUseCase.invoke(hole, handicap)
        assertEquals(5.0, result, 0.01)
    }

    @Test
    fun testNetPar_VeryHighHandicap_NoStroke_ReturnsParPlus1() {
        val hole = createPar4Hole(index = 15)
        val handicap = 25.0
        val result = calcHoleNetParUseCase.invoke(hole, handicap)
        assertEquals(5.0, result, 0.01)
    }

    @Test
    fun testNetPar_VeryHighHandicap_WithTwoStrokes_ReturnsParPlus2() {
        val hole = createPar4Hole(index = 5)
        val handicap = 25.0
        val result = calcHoleNetParUseCase.invoke(hole, handicap)
        assertEquals(6.0, result, 0.01)
    }

    @Test
    fun testNetPar_ExtremelyHighHandicap_NoStroke_ReturnsParPlus2() {
        val hole = createPar4Hole(index = 15)
        val handicap = 40.0
        val result = calcHoleNetParUseCase.invoke(hole, handicap)
        assertEquals(6.0, result, 0.01)
    }

    @Test
    fun testNetPar_ExtremelyHighHandicap_WithThreeStrokes_ReturnsParPlus3() {
        val hole = createPar4Hole(index = 2)
        val handicap = 40.0
        val result = calcHoleNetParUseCase.invoke(hole, handicap)
        assertEquals(7.0, result, 0.01)
    }

    @Test
    fun testNetPar_PlusHandicap_NoStroke_ReturnsOriginalPar() {
        val hole = createPar4Hole(index = 1)
        val handicap = -2.0
        val result = calcHoleNetParUseCase.invoke(hole, handicap)
        assertEquals(4.0, result, 0.01)
    }

    @Test
    fun testNetPar_PlusHandicap_GivesStroke_ReturnsParMinus1() {
        val hole = createPar4Hole(index = 18)
        val handicap = -2.0
        val result = calcHoleNetParUseCase.invoke(hole, handicap)
        assertEquals(3.0, result, 0.01)
    }

    @Test
    fun testNetPar_HandicapRange1to18_BoundaryTests() {
        val hole = createPar4Hole(index = 10)
        
        val result1 = calcHoleNetParUseCase.invoke(hole, 1.0)
        assertEquals(4.0, result1, 0.01)
        
        val result10 = calcHoleNetParUseCase.invoke(hole, 10.0)
        assertEquals(5.0, result10, 0.01)
        
        val result18 = calcHoleNetParUseCase.invoke(hole, 18.0)
        assertEquals(5.0, result18, 0.01)
    }

    @Test
    fun testNetPar_HandicapRange19to36_BoundaryTests() {
        val hole = createPar4Hole(index = 10)
        
        val result19 = calcHoleNetParUseCase.invoke(hole, 19.0)
        assertEquals(5.0, result19, 0.01)
        
        val result28 = calcHoleNetParUseCase.invoke(hole, 28.0)
        assertEquals(6.0, result28, 0.01)
        
        val result36 = calcHoleNetParUseCase.invoke(hole, 36.0)
        assertEquals(6.0, result36, 0.01)
    }

    @Test
    fun testNetPar_UsesSecondStrokeIndex_WhenProvided() {
        val hole = HoleScoreForCalcs(par = 3, index1 = 6, index2 = 33, index3 = 55)
        val handicap = 29.0

        val result = calcHoleNetParUseCase.invoke(hole, handicap)

        assertEquals(4.0, result, 0.01)
    }

    @Test
    fun testNetPar_MissingThirdIndex_DoesNotGrantExtraStroke() {
        val hole = HoleScoreForCalcs(par = 4, index1 = 8, index2 = 30, index3 = 0)
        val handicap = 40.0

        val result = calcHoleNetParUseCase.invoke(hole, handicap)

        assertEquals(6.0, result, 0.01)
    }

    @Test
    fun testNetPar_HandicapAbove36_BoundaryTests() {
        val hole = createPar4Hole(index = 10)
        
        val result37 = calcHoleNetParUseCase.invoke(hole, 37.0)
        assertEquals(6.0, result37, 0.01)
        
        val result46 = calcHoleNetParUseCase.invoke(hole, 46.0)
        assertEquals(7.0, result46, 0.01)
        
        val result54 = calcHoleNetParUseCase.invoke(hole, 54.0)
        assertEquals(7.0, result54, 0.01)
    }

    @Test
    fun testNetPar_AllHoleIndexes_LowHandicap() {
        val handicap = 10.0
        
        for (index in 1..18) {
            val hole = createPar4Hole(index = index)
            val result = calcHoleNetParUseCase.invoke(hole, handicap)
            
            if (handicap >= index) {
                assertEquals("Index $index should get stroke", 5.0, result, 0.01)
            } else {
                assertEquals("Index $index should not get stroke", 4.0, result, 0.01)
            }
        }
    }

    @Test
    fun testNetPar_AllHoleIndexes_HighHandicap() {
        val handicap = 25.0
        
        for (index in 1..18) {
            val hole = createPar4Hole(index = index)
            val result = calcHoleNetParUseCase.invoke(hole, handicap)
            
            if (handicap >= index + 18) {
                assertEquals("Index $index should get 2 strokes", 6.0, result, 0.01)
            } else {
                assertEquals("Index $index should get 1 stroke", 5.0, result, 0.01)
            }
        }
    }

    @Test
    fun testNetPar_WomensTypicalHandicaps() {
        val hole = createPar4Hole(index = 10)
        
        val womensHandicaps = listOf(20.0, 25.0, 30.0, 35.0, 40.0)
        val expectedResults = listOf(5.0, 5.0, 6.0, 6.0, 6.0)
        
        womensHandicaps.zip(expectedResults).forEach { (handicap, expected) ->
            val result = calcHoleNetParUseCase.invoke(hole, handicap)
            assertEquals("Handicap $handicap should return $expected", expected, result, 0.01)
        }
    }

    @Test
    fun testNetPar_MensTypicalHandicaps() {
        val hole = createPar4Hole(index = 10)
        
        val mensHandicaps = listOf(0.0, 5.0, 10.0, 15.0, 18.0, 20.0, 25.0, 28.0)
        val expectedResults = listOf(4.0, 4.0, 5.0, 5.0, 5.0, 5.0, 5.0, 6.0)
        
        mensHandicaps.zip(expectedResults).forEach { (handicap, expected) ->
            val result = calcHoleNetParUseCase.invoke(hole, handicap)
            assertEquals("Handicap $handicap should return $expected", expected, result, 0.01)
        }
    }
}
