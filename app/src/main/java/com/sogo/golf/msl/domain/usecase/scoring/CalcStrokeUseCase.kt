package com.sogo.golf.msl.domain.usecase.scoring

import com.sogo.golf.msl.domain.model.HoleScoreForCalcs
import javax.inject.Inject

class CalcStrokeUseCase @Inject constructor(
    private val calcHoleNetParUseCase: CalcHoleNetParUseCase
) {
    operator fun invoke(strokes: Int, roundHole: HoleScoreForCalcs, dailyHandicap: Double, extraStrokes: Int? = null): Float {
        val netPar = calcHoleNetParUseCase.invoke(roundHole, dailyHandicap, extraStrokes)
        val netParInt = netPar.toInt()
        return (strokes - netParInt).toFloat()
    }
}
