package com.sogo.golf.msl.domain.usecase.date

import com.sogo.golf.msl.data.local.preferencesdata.GameDataTimestampPreferences
import com.sogo.golf.msl.shared.utils.DateUtils
import javax.inject.Inject

class ValidateGameDataFreshnessUseCase @Inject constructor(
    private val gameDataTimestampPreferences: GameDataTimestampPreferences
) {
    suspend operator fun invoke(): Boolean {
        val storedDate = gameDataTimestampPreferences.getGameDataDate()
        val todayDate = DateUtils.getTodayDateString()

        val isDataFresh = DateUtils.isSameDate(storedDate, todayDate)

        android.util.Log.d("ValidateGameDataFreshness", "=== GAME DATA FRESHNESS CHECK ===")
        android.util.Log.d("ValidateGameDataFreshness", "Stored date: $storedDate")
        android.util.Log.d("ValidateGameDataFreshness", "Today's date: $todayDate")
        android.util.Log.d("ValidateGameDataFreshness", "Is data fresh: $isDataFresh")

        return isDataFresh
    }
}