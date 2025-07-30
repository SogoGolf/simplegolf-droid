package com.sogo.golf.msl.domain.usecase.round

import com.sogo.golf.msl.domain.repository.RoundLocalDbRepository
import com.sogo.golf.msl.shared.utils.DateUtils
import javax.inject.Inject

class CheckActiveTodayRoundUseCase @Inject constructor(
    private val roundRepository: RoundLocalDbRepository
) {
    suspend operator fun invoke(): Boolean {
        val todayDateString = DateUtils.getTodayDateString()
        val activeRound = roundRepository.getActiveTodayRound(todayDateString)
        
        android.util.Log.d("CheckActiveTodayRound", "Checking for active round on: $todayDateString")
        android.util.Log.d("CheckActiveTodayRound", "Found active round: ${activeRound != null}")
        
        return activeRound != null
    }
}
