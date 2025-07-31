package com.sogo.golf.msl.domain.usecase.scoring

import com.sogo.golf.msl.domain.model.HoleScoreForCalcs
import javax.inject.Inject

class CalcStrokeUseCase @Inject constructor(
    private val calcHoleNetParUseCase: CalcHoleNetParUseCase
) {
    operator fun invoke(strokes: Int, roundHole: HoleScoreForCalcs, dailyHandicap: Double): Float {
        val netPar = calcHoleNetParUseCase.invoke(roundHole, dailyHandicap)
        val netParInt = netPar.toInt()
        return (strokes - netParInt).toFloat()
    }
}
