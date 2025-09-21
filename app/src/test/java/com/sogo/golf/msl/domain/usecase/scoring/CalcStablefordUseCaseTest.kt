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
    fun testStableford_Round_Table_Hcp11_All18Holes() {
        // handicap used for this round
        val handicap = 11.0

        // helper to build a hole from par + index1
        fun createHole(par: Int, index1: Int): HoleScoreForCalcs {
            return HoleScoreForCalcs(
                par = par,
                index1 = index1,
                index2 = index1 + 18,
                index3 = index1 + 36
            )
        }

        // Columns: par, index1, strokes, expectedScore
        val cases = listOf(
            Triple( createHole(5,  8),  6, 2.0F), // Hole 1:  Par5, SI8,  strokes 6 -> 2 pts
            Triple( createHole(4,  16),  5, 1.0F), // Hole 2:  Par4, SI5,  strokes 6 -> 1 pt
            Triple( createHole(3, 18),  6, 0.0F), // Hole 3:  Par3, SI18, strokes 6 -> 0 pts
            Triple( createHole(4, 13),  5, 1.0F), // Hole 4:  Par4, SI13, strokes 5 -> 1 pt
            Triple( createHole(4, 11),  6, 1.0F), // Hole 5:  Par4, SI11, strokes 6 -> 1 pt
            Triple( createHole(5,  7),  9, 0.0F), // Hole 6:  Par5, SI7,  strokes 9 -> 0 pts
            Triple( createHole(3, 17),  5, 0.0F), // Hole 7:  Par3, SI17, strokes 5 -> 0 pts
            Triple( createHole(4, 10),  5, 2.0F), // Hole 8:  Par4, SI10, strokes 5 -> 2 pts
            Triple( createHole(4,  1),  8, 0.0F), // Hole 9:  Par4, SI1,  strokes 8 -> 0 pts
            Triple( createHole(4,  3),  6, 1.0F), // Hole 10: Par4, SI3,  strokes 6 -> 1 pt
            Triple( createHole(3,  6),  6, 0.0F), // Hole 11: Par3, SI6,  strokes 6 -> 0 pts
            Triple( createHole(5,  9),  7, 1.0F), // Hole 12: Par5, SI9,  strokes 7 -> 1 pt
            Triple( createHole(4,  2),  5, 2.0F), // Hole 13: Par4, SI2,  strokes 5 -> 2 pts
            Triple( createHole(4, 16),  4, 2.0F), // Hole 14: Par4, SI16, strokes 4 -> 2 pts
            Triple( createHole(3, 15),  4, 1.0F), // Hole 15: Par3, SI15, strokes 4 -> 1 pt
            Triple( createHole(4,  4),  5, 2.0F), // Hole 16: Par4, SI4,  strokes 5 -> 2 pts
            Triple( createHole(5, 14),  7, 0.0F), // Hole 17: Par5, SI14, strokes 7 -> 0 pts
            Triple( createHole(4, 12),  5, 1.0F)  // Hole 18: Par4, SI12, strokes 5 -> 1 pt
        )

        cases.forEachIndexed { idx, (hole, strokes, expected) ->
            val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
            assertEquals("Hole ${idx + 1} failed (par=${hole.par}, SI=${hole.index1}, strokes=$strokes)", expected, result, 0.01F)
        }
    }

    @Test
    fun testStableford_Round_Table_HcpMinus4_All18Holes() {
        val handicap = -4.0

        // Build cases using the same 18 holes (par + index1 + strokes) from your earlier layout
        // Format: HoleScoreForCalcs, strokes, expectedStableford
        val cases = listOf(
            Triple(createPar5Hole(index = 8),  6, 1.0F), // H1  Par5 SI8  gross6 -> +1 over -> 1
            Triple(createPar4Hole(index = 5),  6, 0.0F), // H2  Par4 SI5  gross6 -> +2 over -> 0
            Triple(createPar3Hole(index = 18), 6, 0.0F), // H3  Par3 SI18 gross6 -> SI18 gets -1 => net 7 (+4) -> 0
            Triple(createPar4Hole(index = 13), 5, 1.0F), // H4  Par4 SI13 gross5 -> +1 -> 1
            Triple(createPar4Hole(index = 11), 6, 0.0F), // H5  Par4 SI11 gross6 -> +2 -> 0
            Triple(createPar5Hole(index = 7),  9, 0.0F), // H6  Par5 SI7  gross9 -> +4 -> 0
            Triple(createPar3Hole(index = 17), 5, 0.0F), // H7  Par3 SI17 gross5 -> SI17 gets -1 => net 6 (+3) -> 0
            Triple(createPar4Hole(index = 10), 5, 1.0F), // H8  Par4 SI10 gross5 -> +1 -> 1
            Triple(createPar4Hole(index = 1),  8, 0.0F), // H9  Par4 SI1  gross8 -> +4 -> 0
            Triple(createPar4Hole(index = 3),  6, 0.0F), // H10 Par4 SI3  gross6 -> +2 -> 0
            Triple(createPar3Hole(index = 6),  6, 0.0F), // H11 Par3 SI6  gross6 -> +3 -> 0
            Triple(createPar5Hole(index = 9),  7, 0.0F), // H12 Par5 SI9  gross7 -> +2 -> 0
            Triple(createPar4Hole(index = 2),  5, 1.0F), // H13 Par4 SI2  gross5 -> +1 -> 1
            Triple(createPar4Hole(index = 16), 4, 1.0F), // H14 Par4 SI16 gross4 -> SI16 gets -1 => net 5 (+1) -> 1
            Triple(createPar3Hole(index = 15), 4, 0.0F), // H15 Par3 SI15 gross4 -> SI15 gets -1 => net 5 (+2) -> 0
            Triple(createPar4Hole(index = 4),  5, 1.0F), // H16 Par4 SI4  gross5 -> +1 -> 1
            Triple(createPar5Hole(index = 14), 7, 0.0F), // H17 Par5 SI14 gross7 -> +2 -> 0
            Triple(createPar4Hole(index = 12), 5, 1.0F)  // H18 Par4 SI12 gross5 -> +1 -> 1
        )

        cases.forEachIndexed { idx, (hole, strokes, expected) ->
            val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
            assertEquals("Hole ${idx + 1} failed (par=${hole.par}, SI=${hole.index1}, strokes=$strokes)", expected, result, 0.01F)
        }
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
    fun testStableford_Par5_HCap11__Returns5Points() {
        val hole = createPar5Hole()
        val handicap = 11.0
        val strokes = 6
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(2.0F, result, 0.01F)
    }


    @Test
    fun testStableford_Par3_HCap13__Returns5Points() {
        val hole = createPar3Hole()
        val handicap = 13.0
        val strokes = 4
        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)
        assertEquals(1.0F, result, 0.01F)
        assertNotEquals(2.0f, result, 0.01F)
    }

    @Test
    fun testStableford_UsesSecondStrokeIndex_WhenProvided() {
        val hole = HoleScoreForCalcs(par = 3, index1 = 6, index2 = 33, index3 = 55)
        val handicap = 29.0
        val strokes = 3

        val result = calcStablefordUseCase.invoke(hole, handicap, strokes)

        assertEquals(3.0F, result, 0.01F)
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
