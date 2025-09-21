package com.sogo.golf.msl.domain.usecase.scoring

import com.sogo.golf.msl.domain.model.HoleScoreForCalcs
import javax.inject.Inject

class CalcHoleNetParUseCase @Inject constructor() {

    operator fun invoke(roundHole: HoleScoreForCalcs, dailyHandicap: Double): Double {
        return calculateHoleNetPar(roundHole, dailyHandicap)
    }

    private fun calculateHoleNetPar(roundHole: HoleScoreForCalcs, dailyHandicap: Double): Double {
        val index1 = roundHole.index1
        val index2 = if (roundHole.index2 > 0) {
            roundHole.index2
        } else {
            index1 + 18
        }
        val index3 = if (roundHole.index3 > 0) {
            roundHole.index3
        } else {
            index2 + 18
        }

        return when {
            dailyHandicap in 1.0..18.0 -> {
                if (dailyHandicap >= index1.toDouble()) {
                    roundHole.par + 1.0
                } else {
                    roundHole.par.toDouble()
                }
            }
            dailyHandicap in 19.0..36.0 -> {
                val baseStroke = 1.0
                val extraStroke = if (dailyHandicap >= index2.toDouble()) 1.0 else 0.0
                roundHole.par.toDouble() + baseStroke + extraStroke
            }
            dailyHandicap > 36.0 -> {
                if (dailyHandicap >= index3.toDouble()) {
                    roundHole.par + 3.0
                } else {
                    roundHole.par + 2.0
                }
            }
            dailyHandicap <= 0.0 -> {
                if (dailyHandicap + 19 <= roundHole.index1.toDouble()) {
                    roundHole.par - 1.0
                } else {
                    roundHole.par.toDouble()
                }
            }
            else -> roundHole.par.toDouble()
        }
    }
}
