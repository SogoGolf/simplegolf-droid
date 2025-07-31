package com.sogo.golf.msl.domain.usecase.scoring

import com.sogo.golf.msl.domain.model.HoleScoreForCalcs
import javax.inject.Inject

class CalcHoleNetParUseCase @Inject constructor() {

    operator fun invoke(roundHole: HoleScoreForCalcs, dailyHandicap: Double): Double {
        return calculateHoleNetPar(roundHole, dailyHandicap)
    }

    private fun calculateHoleNetPar(roundHole: HoleScoreForCalcs, dailyHandicap: Double): Double {
        return when {
            dailyHandicap in 1.0..18.0 -> {
                if (dailyHandicap >= roundHole.index1.toDouble()) {
                    roundHole.par + 1.0
                } else {
                    roundHole.par.toDouble()
                }
            }
            dailyHandicap in 19.0..36.0 -> {
                if (dailyHandicap >= roundHole.index2.toDouble()) {
                    roundHole.par + 2.0
                } else {
                    roundHole.par + 1.0
                }
            }
            dailyHandicap > 36.0 -> {
                if (dailyHandicap >= roundHole.index3.toDouble()) {
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
