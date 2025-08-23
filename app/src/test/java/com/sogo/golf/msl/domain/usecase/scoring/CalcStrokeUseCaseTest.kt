package com.sogo.golf.msl.domain.usecase.scoring

import com.sogo.golf.msl.domain.model.HoleScoreForCalcs
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class CalcStrokeUseCaseTest {

    private lateinit var calcHoleNetParUseCase: CalcHoleNetParUseCase
    private lateinit var calcStrokeUseCase: CalcStrokeUseCase

    @Before
    fun setUp() {
        calcHoleNetParUseCase = CalcHoleNetParUseCase()
        calcStrokeUseCase = CalcStrokeUseCase(calcHoleNetParUseCase)
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
    fun testStroke_HoleInOne_Par3_ScratchGolfer_ReturnsMinus2() {
        val hole = createPar3Hole()
        val handicap = 0.0
        val strokes = 1
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(-2.0F, result, 0.01F)
    }

    @Test
    fun testStroke_HoleInOne_Par4_ScratchGolfer_ReturnsMinus3() {
        val hole = createPar4Hole()
        val handicap = 0.0
        val strokes = 1
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(-3.0F, result, 0.01F)
    }

    @Test
    fun testStroke_HoleInOne_Par5_ScratchGolfer_ReturnsMinus4() {
        val hole = createPar5Hole()
        val handicap = 0.0
        val strokes = 1
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(-4.0F, result, 0.01F)
    }

    @Test
    fun testStroke_Eagle_Par4_ScratchGolfer_ReturnsMinus2() {
        val hole = createPar4Hole()
        val handicap = 0.0
        val strokes = 2
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(-2.0F, result, 0.01F)
    }

    @Test
    fun testStroke_Eagle_Par5_ScratchGolfer_ReturnsMinus2() {
        val hole = createPar5Hole()
        val handicap = 0.0
        val strokes = 3
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(-2.0F, result, 0.01F)
    }

    @Test
    fun testStroke_Birdie_Par3_ScratchGolfer_ReturnsMinus1() {
        val hole = createPar3Hole()
        val handicap = 0.0
        val strokes = 2
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(-1.0F, result, 0.01F)
    }

    @Test
    fun testStroke_Birdie_Par4_ScratchGolfer_ReturnsMinus1() {
        val hole = createPar4Hole()
        val handicap = 0.0
        val strokes = 3
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(-1.0F, result, 0.01F)
    }

    @Test
    fun testStroke_Birdie_Par5_ScratchGolfer_ReturnsMinus1() {
        val hole = createPar5Hole()
        val handicap = 0.0
        val strokes = 4
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(-1.0F, result, 0.01F)
    }

    @Test
    fun testStroke_Par_Par3_ScratchGolfer_Returns0() {
        val hole = createPar3Hole()
        val handicap = 0.0
        val strokes = 3
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(0.0F, result, 0.01F)
    }

    @Test
    fun testStroke_Par_Par4_ScratchGolfer_Returns0() {
        val hole = createPar4Hole()
        val handicap = 0.0
        val strokes = 4
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(0.0F, result, 0.01F)
    }

    @Test
    fun testStroke_Par_Par5_ScratchGolfer_Returns0() {
        val hole = createPar5Hole()
        val handicap = 0.0
        val strokes = 5
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(0.0F, result, 0.01F)
    }

    @Test
    fun testStroke_Bogey_Par3_ScratchGolfer_ReturnsPlus1() {
        val hole = createPar3Hole()
        val handicap = 0.0
        val strokes = 4
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(1.0F, result, 0.01F)
    }

    @Test
    fun testStroke_Bogey_Par4_ScratchGolfer_ReturnsPlus1() {
        val hole = createPar4Hole()
        val handicap = 0.0
        val strokes = 5
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(1.0F, result, 0.01F)
    }

    @Test
    fun testStroke_DoubleBogey_Par4_ScratchGolfer_ReturnsPlus2() {
        val hole = createPar4Hole()
        val handicap = 0.0
        val strokes = 6
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(2.0F, result, 0.01F)
    }

    @Test
    fun testStroke_TripleBogey_Par4_ScratchGolfer_ReturnsPlus3() {
        val hole = createPar4Hole()
        val handicap = 0.0
        val strokes = 7
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(3.0F, result, 0.01F)
    }

    @Test
    fun testStroke_HighStrokes_Par4_ScratchGolfer_ReturnsPlus6() {
        val hole = createPar4Hole()
        val handicap = 0.0
        val strokes = 10
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(6.0F, result, 0.01F)
    }

    @Test
    fun testStroke_Par_Par4_LowHandicap_MaleGolfer_Returns0() {
        val hole = createPar4Hole(index = 15)
        val handicap = 5.0
        val strokes = 4
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(0.0F, result, 0.01F)
    }

    @Test
    fun testStroke_Par_Par4_LowHandicap_MaleGolfer_WithStroke_ReturnsMinus1() {
        val hole = createPar4Hole(index = 3)
        val handicap = 5.0
        val strokes = 4
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(-1.0F, result, 0.01F)
    }

    @Test
    fun testStroke_Bogey_Par4_LowHandicap_MaleGolfer_WithStroke_Returns0() {
        val hole = createPar4Hole(index = 3)
        val handicap = 5.0
        val strokes = 5
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(0.0F, result, 0.01F)
    }

    @Test
    fun testStroke_Par_Par4_MediumHandicap_MaleGolfer_Returns0() {
        val hole = createPar4Hole(index = 15)
        val handicap = 12.0
        val strokes = 4
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(0.0F, result, 0.01F)
    }

    @Test
    fun testStroke_Par_Par4_MediumHandicap_MaleGolfer_WithStroke_ReturnsMinus1() {
        val hole = createPar4Hole(index = 8)
        val handicap = 12.0
        val strokes = 4
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(-1.0F, result, 0.01F)
    }

    @Test
    fun testStroke_Par_Par4_HighHandicap_MaleGolfer_ReturnsMinus1() {
        val hole = createPar4Hole(index = 15)
        val handicap = 18.0
        val strokes = 4
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(-1.0F, result, 0.01F)
    }

    @Test
    fun testStroke_Par_Par4_HighHandicap_MaleGolfer_WithStroke_ReturnsMinus1() {
        val hole = createPar4Hole(index = 1)
        val handicap = 18.0
        val strokes = 4
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(-1.0F, result, 0.01F)
    }

    @Test
    fun testStroke_Par_Par4_VeryHighHandicap_FemaleGolfer_ReturnsMinus1() {
        val hole = createPar4Hole(index = 15)
        val handicap = 25.0
        val strokes = 4
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(-1.0F, result, 0.01F)
    }

    @Test
    fun testStroke_Par_Par4_VeryHighHandicap_FemaleGolfer_WithTwoStrokes_ReturnsMinus2() {
        val hole = createPar4Hole(index = 5)
        val handicap = 25.0
        val strokes = 4
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(-2.0F, result, 0.01F)
    }

    @Test
    fun testStroke_Bogey_Par4_VeryHighHandicap_FemaleGolfer_WithTwoStrokes_ReturnsMinus1() {
        val hole = createPar4Hole(index = 5)
        val handicap = 25.0
        val strokes = 5
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(-1.0F, result, 0.01F)
    }

    @Test
    fun testStroke_DoubleBogey_Par4_VeryHighHandicap_FemaleGolfer_WithTwoStrokes_Returns0() {
        val hole = createPar4Hole(index = 5)
        val handicap = 25.0
        val strokes = 6
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(0.0F, result, 0.01F)
    }

    @Test
    fun testStroke_Par_Par4_ExtremelyHighHandicap_FemaleGolfer_ReturnsMinus2() {
        val hole = createPar4Hole(index = 15)
        val handicap = 40.0
        val strokes = 4
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(-2.0F, result, 0.01F)
    }

    @Test
    fun testStroke_Par_Par4_ExtremelyHighHandicap_FemaleGolfer_WithThreeStrokes_ReturnsMinus3() {
        val hole = createPar4Hole(index = 2)
        val handicap = 40.0
        val strokes = 4
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(-3.0F, result, 0.01F)
    }

    @Test
    fun testStroke_DoubleBogey_Par4_ExtremelyHighHandicap_FemaleGolfer_WithThreeStrokes_ReturnsMinus1() {
        val hole = createPar4Hole(index = 2)
        val handicap = 40.0
        val strokes = 6
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(-1.0F, result, 0.01F)
    }

    @Test
    fun testStroke_HighStrokes_Par4_ExtremelyHighHandicap_FemaleGolfer_WithThreeStrokes_ReturnsPlus3() {
        val hole = createPar4Hole(index = 2)
        val handicap = 40.0
        val strokes = 10
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(3.0F, result, 0.01F)
    }

    @Test
    fun testStroke_PlusHandicap_Par4_ReturnsMinus1() {
        val hole = createPar4Hole(index = 1)
        val handicap = -2.0
        val strokes = 3
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(-1.0F, result, 0.01F)
    }

    @Test
    fun testStroke_PlusHandicap_Par4_GivesStroke_Returns0() {
        val hole = createPar4Hole(index = 18)
        val handicap = -2.0
        val strokes = 3
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(0.0F, result, 0.01F)
    }

    @Test
    fun testStroke_PlusHandicap_Par4_GivesStroke_ReturnsPlus1() {
        val hole = createPar4Hole(index = 18)
        val handicap = -2.0
        val strokes = 4
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(1.0F, result, 0.01F)
    }

    @Test
    fun testStroke_HoleInOne_Par4_ReturnsMinus3() {
        val hole = createPar4Hole()
        val handicap = 0.0
        val strokes = 1
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(-3.0F, result, 0.01F)
    }

    @Test
    fun testStroke_AlbatrossEquivalent_Par5_ReturnsMinus3() {
        val hole = createPar5Hole()
        val handicap = 0.0
        val strokes = 2
        val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
        assertEquals(-3.0F, result, 0.01F)
    }

    @Test
    fun testStroke_NegativeStrokeDifference_Range() {
        val hole = createPar4Hole()
        val handicap = 0.0
        
        for (strokeDiff in -5..-1) {
            val strokes = 4 + strokeDiff
            val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
            assertEquals(strokeDiff.toFloat(), result, 0.01F)
        }
    }

    @Test
    fun testStroke_PositiveStrokeDifference_Range() {
        val hole = createPar4Hole()
        val handicap = 0.0
        
        for (strokeDiff in 1..10) {
            val strokes = 4 + strokeDiff
            val result = calcStrokeUseCase.invoke(strokes, hole, handicap)
            assertEquals(strokeDiff.toFloat(), result, 0.01F)
        }
    }
}
