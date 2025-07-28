package com.sogo.golf.msl.domain.usecase.date

import com.sogo.golf.msl.data.local.preferencesdata.GameDataTimestampPreferences
import com.sogo.golf.msl.shared.utils.DateUtils
import javax.inject.Inject

class SaveGameDataDateUseCase @Inject constructor(
    private val gameDataTimestampPreferences: GameDataTimestampPreferences
) {
    suspend operator fun invoke() {
        val todayDate = DateUtils.getTodayDateString()
        gameDataTimestampPreferences.saveGameDataDate(todayDate)

        android.util.Log.d("SaveGameDataDate", "✅ Saved today's date as game data date: $todayDate")
    }
}