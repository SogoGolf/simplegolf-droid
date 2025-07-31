package com.sogo.golf.msl.domain.usecase.scoring

import com.sogo.golf.msl.domain.model.HoleScoreForCalcs
import javax.inject.Inject

class CalcStablefordUseCase @Inject constructor(
    private val calcHoleNetParUseCase: CalcHoleNetParUseCase
) {
    operator fun invoke(roundHole: HoleScoreForCalcs, dailyHandicap: Double, strokes: Int): Float {
        val netPar = calcHoleNetParUseCase.invoke(roundHole, dailyHandicap)
        val netParInt = netPar.toInt()

        return when (strokes) {
            in Int.MIN_VALUE..(netParInt - 7) -> 8.0F
            netParInt - 6 -> 8.0F
            netParInt - 5 -> 7.0F
            netParInt - 4 -> 6.0F
            netParInt - 3 -> 5.0F
            netParInt - 2 -> 4.0F
            netParInt - 1 -> 3.0F
            netParInt -> 2.0F
            netParInt + 1 -> 1.0F
            else -> 0.0F
        }
    }
}
