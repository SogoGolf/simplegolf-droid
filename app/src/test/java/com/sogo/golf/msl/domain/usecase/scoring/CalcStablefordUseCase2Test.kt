package com.sogo.golf.msl.domain.usecase.scoring

import com.sogo.golf.msl.domain.model.HoleScoreForCalcs
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalcStablefordUseCase2Test {

    private lateinit var calcHoleNetParUseCase: CalcHoleNetParUseCase
    private lateinit var calcStablefordUseCase: CalcStablefordUseCase

    private data class StablefordCase(
        val description: String,
        val par: Int,
        val index1: Int,
        val index2: Int,
        val index3: Int,
        val handicap: Double,
        val strokes: Int,
        val expectedPoints: Float
    )

    @Before
    fun setUp() {
        calcHoleNetParUseCase = CalcHoleNetParUseCase()
        calcStablefordUseCase = CalcStablefordUseCase(calcHoleNetParUseCase)
    }

    @Test
    fun stablefordCoversHandicapsOneToThirtySix() {
        val cases = listOf(
            StablefordCase(
                description = "Low handicap, no stroke awarded",
                par = 4,
                index1 = 10,
                index2 = 28,
                index3 = 46,
                handicap = 4.0,
                strokes = 4,
                expectedPoints = 2.0F
            ),
            StablefordCase(
                description = "Low handicap, receives first stroke",
                par = 4,
                index1 = 4,
                index2 = 22,
                index3 = 40,
                handicap = 7.0,
                strokes = 4,
                expectedPoints = 3.0F
            ),
            StablefordCase(
                description = "Mid handicap, single stroke hole",
                par = 5,
                index1 = 8,
                index2 = 26,
                index3 = 44,
                handicap = 24.0,
                strokes = 5,
                expectedPoints = 3.0F
            ),
            StablefordCase(
                description = "Mid handicap, double stroke hole",
                par = 5,
                index1 = 5,
                index2 = 23,
                index3 = 41,
                handicap = 34.0,
                strokes = 6,
                expectedPoints = 3.0F
            ),
            StablefordCase(
                description = "Handicap 36 boundary still uses second index",
                par = 4,
                index1 = 6,
                index2 = 24,
                index3 = 48,
                handicap = 36.0,
                strokes = 5,
                expectedPoints = 3.0F
            ),
            StablefordCase(
                description = "Heavy bogey drops Stableford to zero",
                par = 4,
                index1 = 7,
                index2 = 25,
                index3 = 43,
                handicap = 10.0,
                strokes = 8,
                expectedPoints = 0.0F
            ),
            StablefordCase(
                description = "Ace on a double-stroke hole pays out 8 points",
                par = 5,
                index1 = 2,
                index2 = 20,
                index3 = 38,
                handicap = 30.0,
                strokes = 1,
                expectedPoints = 8.0F
            )
        )

        runCases(cases)
    }

    @Test
    fun stablefordCoversHighHandicapsIncludingWomen() {
        val cases = listOf(
            StablefordCase(
                description = "High handicap only receives two strokes",
                par = 3,
                index1 = 9,
                index2 = 27,
                index3 = 51,
                handicap = 45.0,
                strokes = 5,
                expectedPoints = 2.0F
            ),
            StablefordCase(
                description = "High handicap gains third stroke when index3 hit",
                par = 4,
                index1 = 2,
                index2 = 20,
                index3 = 42,
                handicap = 44.0,
                strokes = 5,
                expectedPoints = 4.0F
            ),
            StablefordCase(
                description = "Women handicap 50 without third stroke allowance",
                par = 5,
                index1 = 12,
                index2 = 30,
                index3 = 52,
                handicap = 50.0,
                strokes = 7,
                expectedPoints = 2.0F
            ),
            StablefordCase(
                description = "Women handicap 50 earns third stroke when allowed",
                par = 5,
                index1 = 12,
                index2 = 30,
                index3 = 48,
                handicap = 50.0,
                strokes = 6,
                expectedPoints = 4.0F
            )
        )

        runCases(cases)
    }

    @Test
    fun stablefordHandlesPlusHandicaps() {
        val cases = listOf(
            StablefordCase(
                description = "Plus handicap does not give a stroke back on higher index hole",
                par = 4,
                index1 = 15,
                index2 = 33,
                index3 = 51,
                handicap = -3.0,
                strokes = 3,
                expectedPoints = 3.0F
            ),
            StablefordCase(
                description = "Plus handicap gives a stroke back on lowest index hole",
                par = 4,
                index1 = 18,
                index2 = 36,
                index3 = 54,
                handicap = -3.0,
                strokes = 4,
                expectedPoints = 1.0F
            )
        )

        runCases(cases)
    }

    private fun runCases(cases: List<StablefordCase>) {
        cases.forEach { case ->
            val hole = HoleScoreForCalcs(
                par = case.par,
                index1 = case.index1,
                index2 = case.index2,
                index3 = case.index3
            )

            val actual = calcStablefordUseCase.invoke(hole, case.handicap, case.strokes)

            assertEquals(case.description, case.expectedPoints, actual, 0.01F)
        }
    }
}

