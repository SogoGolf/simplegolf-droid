package com.sogo.golf.msl.domain.usecase.scoring

import com.sogo.golf.msl.domain.model.HoleScoreForCalcs
import javax.inject.Inject

class CalcHoleNetParUseCase @Inject constructor() {

    operator fun invoke(roundHole: HoleScoreForCalcs, dailyHandicap: Double, extraStrokes: Int? = null): Double {
        return calculateHoleNetPar(roundHole, dailyHandicap, extraStrokes)
    }

    private fun calculateHoleNetPar(roundHole: HoleScoreForCalcs, dailyHandicap: Double, extraStrokes: Int? = null): Double {
        // Always require extraStrokes from the API - never fallback to calculation
        if (extraStrokes == null) {
            throw IllegalStateException("extraStrokes is required but was null. Competition data may be incomplete.")
        }

        val netPar = roundHole.par.toDouble() + extraStrokes.toDouble()
        android.util.Log.d("CalcHoleNetPar", "par=${roundHole.par}, extraStrokes=${extraStrokes}, netPar=${netPar}, dailyHandicap=${dailyHandicap}")
        return netPar
    }
}
