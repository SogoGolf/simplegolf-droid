package com.sogo.golf.msl.domain.usecase.scoring

import com.sogo.golf.msl.domain.model.HoleScoreForCalcs
import org.junit.Test
import org.junit.Before

class DebugScoringTest {

    private lateinit var calcHoleNetParUseCase: CalcHoleNetParUseCase
    private lateinit var calcStablefordUseCase: CalcStablefordUseCase
    private lateinit var calcParUseCase: CalcParUseCase
    private lateinit var calcStrokeUseCase: CalcStrokeUseCase

    @Before
    fun setUp() {
        calcHoleNetParUseCase = CalcHoleNetParUseCase()
        calcStablefordUseCase = CalcStablefordUseCase(calcHoleNetParUseCase)
        calcParUseCase = CalcParUseCase(calcHoleNetParUseCase)
        calcStrokeUseCase = CalcStrokeUseCase(calcHoleNetParUseCase)
    }

    @Test
    fun debugStablefordScoring() {
        val par3Hole = HoleScoreForCalcs(par = 3, index1 = 17, index2 = 35, index3 = 53)
        val handicap = 0.0
        val strokes = 1
        
        val netPar = calcHoleNetParUseCase.invoke(par3Hole, handicap)
        val stablefordPoints = calcStablefordUseCase.invoke(par3Hole, handicap, strokes)
        
        println("Par 3, Index 17, Handicap 0, Strokes 1:")
        println("Net Par: $netPar")
        println("Stableford Points: $stablefordPoints")
        println("Expected: 3 points (netPar-1 = 3-1 = 2 strokes under net par)")
    }

    @Test
    fun debugHoleInOneStablefordTests() {
        println("=== Debugging Hole-in-One Stableford Tests ===")
        
        // testStableford_HoleInOne_Par4_Returns6Points (failing)
        val par4Hole = HoleScoreForCalcs(par = 4, index1 = 10, index2 = 28, index3 = 46)
        val handicap = 0.0
        val strokes = 1
        
        val netPar4 = calcHoleNetParUseCase.invoke(par4Hole, handicap)
        val stablefordPar4 = calcStablefordUseCase.invoke(par4Hole, handicap, strokes)
        
        println("Hole-in-One on Par 4:")
        println("  Net Par: $netPar4")
        println("  Stableford Points: $stablefordPar4 (test expects: 6.0)")
        println("  Actual result: $stablefordPar4")
        
        // testStableford_HoleInOne_Par5_Returns8Points (failing)
        val par5Hole = HoleScoreForCalcs(par = 5, index1 = 1, index2 = 19, index3 = 37)
        val stablefordPar5 = calcStablefordUseCase.invoke(par5Hole, handicap, strokes)
        val netPar5 = calcHoleNetParUseCase.invoke(par5Hole, handicap)
        
        println("\nHole-in-One on Par 5:")
        println("  Net Par: $netPar5")
        println("  Stableford Points: $stablefordPar5 (test expects: 8.0)")
        println("  Actual result: $stablefordPar5")
        
        println("\nCorrect expected values:")
        println("  Par 4 hole-in-one should expect: ${stablefordPar4}F")
        println("  Par 5 hole-in-one should expect: ${stablefordPar5}F")
    }
    
    private fun debugScenario(description: String, hole: HoleScoreForCalcs, handicap: Double, strokes: Int): String {
        val netPar = calcHoleNetParUseCase.invoke(hole, handicap)
        val parScore = calcParUseCase.invoke(strokes, hole, handicap)
        val strokeScore = calcStrokeUseCase.invoke(strokes, hole, handicap)
        val stablefordScore = calcStablefordUseCase.invoke(hole, handicap, strokes)
        
        println("$description:")
        println("  Net Par: $netPar")
        println("  Par Score: $parScore")
        println("  Stroke Score: $strokeScore")
        println("  Stableford Score: $stablefordScore")
        println()
        
        return "NetPar:$netPar,Par:$parScore,Stroke:$strokeScore,Stableford:$stablefordScore"
    }

    @Test
    fun debugHandicapCalculation() {
        val par4Hole = HoleScoreForCalcs(par = 4, index1 = 10, index2 = 28, index3 = 46)
        
        println("Par 4, Index 10:")
        
        val handicaps = listOf(0.0, 5.0, 10.0, 15.0, 18.0, 25.0, 40.0)
        handicaps.forEach { handicap ->
            val netPar = calcHoleNetParUseCase.invoke(par4Hole, handicap)
            println("Handicap $handicap -> Net Par: $netPar")
        }
    }
}
