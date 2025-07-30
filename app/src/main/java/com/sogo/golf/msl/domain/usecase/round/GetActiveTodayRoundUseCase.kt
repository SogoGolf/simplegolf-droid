package com.sogo.golf.msl.domain.usecase.round

import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.repository.RoundLocalDbRepository
import com.sogo.golf.msl.shared.utils.DateUtils
import javax.inject.Inject

class GetActiveTodayRoundUseCase @Inject constructor(
    private val roundRepository: RoundLocalDbRepository
) {
    suspend operator fun invoke(): Round? {
        val todayDateString = DateUtils.getTodayDateString()
        val activeRound = roundRepository.getActiveTodayRound(todayDateString)
        
        android.util.Log.d("GetActiveTodayRound", "Checking for active round on: $todayDateString")
        android.util.Log.d("GetActiveTodayRound", "Found active round: ${activeRound != null}")
        android.util.Log.d("GetActiveTodayRound", "Round ID: ${activeRound?.id}")
        
        return activeRound
    }
}
