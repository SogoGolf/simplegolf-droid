package com.sogo.golf.msl.domain.usecase.round

import com.sogo.golf.msl.domain.usecase.round.GetActiveTodayRoundUseCase
import javax.inject.Inject

class CheckActiveTodayRoundUseCase @Inject constructor(
    private val getActiveTodayRoundUseCase: GetActiveTodayRoundUseCase
) {
    suspend operator fun invoke(): Boolean {
        val activeRound = getActiveTodayRoundUseCase()
        
        android.util.Log.d("CheckActiveTodayRound", "Checking for active round")
        android.util.Log.d("CheckActiveTodayRound", "Found active round: ${activeRound != null}")
        
        return activeRound != null
    }
}
