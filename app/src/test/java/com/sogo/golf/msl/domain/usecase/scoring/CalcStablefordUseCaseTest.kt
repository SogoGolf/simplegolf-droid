package com.sogo.golf.msl.domain.usecase.scoring

import com.sogo.golf.msl.domain.model.HoleScoreForCalcs
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class CalcStablefordUseCaseTest {

    private lateinit var calcHoleNetParUseCase: CalcHoleNetParUseCase
    private lateinit var calcStablefordUseCase: CalcStablefordUseCase

    @Before
    fun setUp() {
        calcHoleNetParUseCase = CalcHoleNetParUseCase()
        calcStablefordUseCase = CalcStablefordUseCase(calcHoleNetParUseCase)
    }

    private fun createPar3Hole(index: Int = 17): HoleScoreForCalcs {
        return HoleScoreForCalcs(par = 3, index1 = index, index2 = index + 18, index3 = index + 36)
    }

    private fun createPar4Hole(index: Int = 10): HoleScoreForCalcs {
        return HoleScoreForCalcs(par = 4, index1 = index, index2 = index + 18, index3 = index + 36)
    }

    private fun createPar5Hole(index: Int = 2): HoleScoreForCalcs {
        return HoleScoreForCalcs(par = 5, index1 = index, index2 = index + 18, index3 = index + 36)
    }

    @Test
    fun testStableford_HoleInOne_Par3_ScratchGolfer_Returns4Points() {
        val hole = createPar3Hole()
        val handicap = 0.0
        val strokes = 1
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(4.0F, result, 0.01F)
    }

    @Test
    fun testStableford_HoleInOne_Par4_ScratchGolfer_Returns5Points() {
        val hole = createPar4Hole()
        val handicap = 0.0
        val strokes = 1
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(5.0F, result, 0.01F)
    }

    @Test
    fun testStableford_HoleInOne_Par5_ScratchGolfer_Returns6Points() {
        val hole = createPar5Hole()
        val handicap = 0.0
        val strokes = 1
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(6.0F, result, 0.01F)
    }

    @Test
    fun testStableford_Eagle_Par4_ScratchGolfer_Returns4Points() {
        val hole = createPar4Hole()
        val handicap = 0.0
        val strokes = 2
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(4.0F, result, 0.01F)
    }

    @Test
    fun testStableford_Eagle_Par5_ScratchGolfer_Returns4Points() {
        val hole = createPar5Hole()
        val handicap = 0.0
        val strokes = 3
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(4.0F, result, 0.01F)
    }

    @Test
    fun testStableford_Birdie_Par3_ScratchGolfer_Returns3Points() {
        val hole = createPar3Hole()
        val handicap = 0.0
        val strokes = 2
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(3.0F, result, 0.01F)
    }

    @Test
    fun testStableford_Birdie_Par4_ScratchGolfer_Returns3Points() {
        val hole = createPar4Hole()
        val handicap = 0.0
        val strokes = 3
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(3.0F, result, 0.01F)
    }

    @Test
    fun testStableford_Birdie_Par5_ScratchGolfer_Returns3Points() {
        val hole = createPar5Hole()
        val handicap = 0.0
        val strokes = 4
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(3.0F, result, 0.01F)
    }

    @Test
    fun testStableford_Par_Par3_ScratchGolfer_Returns2Points() {
        val hole = createPar3Hole()
        val handicap = 0.0
        val strokes = 3
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(2.0F, result, 0.01F)
    }

    @Test
    fun testStableford_Par_Par4_ScratchGolfer_Returns2Points() {
        val hole = createPar4Hole()
        val handicap = 0.0
        val strokes = 4
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(2.0F, result, 0.01F)
    }

    @Test
    fun testStableford_Par_Par5_ScratchGolfer_Returns2Points() {
        val hole = createPar5Hole()
        val handicap = 0.0
        val strokes = 5
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(2.0F, result, 0.01F)
    }

    @Test
    fun testStableford_Bogey_Par3_ScratchGolfer_Returns1Point() {
        val hole = createPar3Hole()
        val handicap = 0.0
        val strokes = 4
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(1.0F, result, 0.01F)
    }

    @Test
    fun testStableford_Bogey_Par4_ScratchGolfer_Returns1Point() {
        val hole = createPar4Hole()
        val handicap = 0.0
        val strokes = 5
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(1.0F, result, 0.01F)
    }

    @Test
    fun testStableford_DoubleBogey_Par4_ScratchGolfer_Returns0Points() {
        val hole = createPar4Hole()
        val handicap = 0.0
        val strokes = 6
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(0.0F, result, 0.01F)
    }

    @Test
    fun testStableford_HighStrokes_Par4_ScratchGolfer_Returns0Points() {
        val hole = createPar4Hole()
        val handicap = 0.0
        val strokes = 10
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(0.0F, result, 0.01F)
    }

    @Test
    fun testStableford_Par_Par4_LowHandicap_MaleGolfer_Returns2Points() {
        val hole = createPar4Hole(index = 15)
        val handicap = 5.0
        val strokes = 4
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(2.0F, result, 0.01F)
    }

    @Test
    fun testStableford_Par_Par4_LowHandicap_MaleGolfer_WithStroke_Returns3Points() {
        val hole = createPar4Hole(index = 3)
        val handicap = 5.0
        val strokes = 4
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(3.0F, result, 0.01F)
    }

    @Test
    fun testStableford_Par_Par4_MediumHandicap_MaleGolfer_Returns2Points() {
        val hole = createPar4Hole(index = 15)
        val handicap = 12.0
        val strokes = 4
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(2.0F, result, 0.01F)
    }

    @Test
    fun testStableford_Par_Par4_MediumHandicap_MaleGolfer_WithStroke_Returns3Points() {
        val hole = createPar4Hole(index = 8)
        val handicap = 12.0
        val strokes = 4
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(3.0F, result, 0.01F)
    }

    @Test
    fun testStableford_Par_Par4_HighHandicap_MaleGolfer_Returns3Points() {
        val hole = createPar4Hole(index = 15)
        val handicap = 18.0
        val strokes = 4
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(3.0F, result, 0.01F)
    }

    @Test
    fun testStableford_Par_Par4_HighHandicap_MaleGolfer_WithStroke_Returns3Points() {
        val hole = createPar4Hole(index = 1)
        val handicap = 18.0
        val strokes = 4
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(3.0F, result, 0.01F)
    }

    @Test
    fun testStableford_Par_Par4_VeryHighHandicap_FemaleGolfer_Returns3Points() {
        val hole = createPar4Hole(index = 15)
        val handicap = 25.0
        val strokes = 4
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(3.0F, result, 0.01F)
    }

    @Test
    fun testStableford_Par_Par4_VeryHighHandicap_FemaleGolfer_WithTwoStrokes_Returns4Points() {
        val hole = createPar4Hole(index = 5)
        val handicap = 25.0
        val strokes = 4
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(4.0F, result, 0.01F)
    }

    @Test
    fun testStableford_Par_Par4_ExtremelyHighHandicap_FemaleGolfer_Returns4Points() {
        val hole = createPar4Hole(index = 15)
        val handicap = 40.0
        val strokes = 4
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(4.0F, result, 0.01F)
    }

    @Test
    fun testStableford_Par_Par4_ExtremelyHighHandicap_FemaleGolfer_WithThreeStrokes_Returns5Points() {
        val hole = createPar4Hole(index = 2)
        val handicap = 40.0
        val strokes = 4
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(5.0F, result, 0.01F)
    }

    @Test
    fun testStableford_Birdie_Par4_HighHandicap_WithStroke_Returns3Points() {
        val hole = createPar4Hole(index = 5)
        val handicap = 18.0
        val strokes = 4
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(3.0F, result, 0.01F)
    }

    @Test
    fun testStableford_Eagle_Par5_HighHandicap_WithStroke_Returns4Points() {
        val hole = createPar5Hole(index = 1)
        val handicap = 18.0
        val strokes = 4
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(4.0F, result, 0.01F)
    }

    @Test
    fun testStableford_PlusHandicap_Par4_Returns3Points() {
        val hole = createPar4Hole(index = 1)
        val handicap = -2.0
        val strokes = 3
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(3.0F, result, 0.01F)
    }

    @Test
    fun testStableford_PlusHandicap_Par4_GivesStroke_Returns2Points() {
        val hole = createPar4Hole(index = 18)
        val handicap = -2.0
        val strokes = 3
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(2.0F, result, 0.01F)
    }

    @Test
    fun testStableford_HoleInOne_Par4_Returns5Points() {
        val hole = createPar4Hole()
        val handicap = 0.0
        val strokes = 1
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(5.0F, result, 0.01F)
    }

    @Test
    fun testStableford_AlbatrossEquivalent_Par5_Returns5Points() {
        val hole = createPar5Hole()
        val handicap = 0.0
        val strokes = 2
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(5.0F, result, 0.01F)
    }

    @Test
    fun testStableford_HoleInOne_Par5_Returns6Points() {
        val hole = createPar5Hole()
        val handicap = 0.0
        val strokes = 1
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(6.0F, result, 0.01F)
    }
}
