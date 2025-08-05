package com.sogo.golf.msl.data.local.database.entities.mongodb

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sogo.golf.msl.domain.model.mongodb.AppSettings
import com.sogo.golf.msl.domain.model.mongodb.NotificationFlag
import com.sogo.golf.msl.domain.model.mongodb.SogoState

class SogoGolferConverters {

    @TypeConverter
    fun fromAppSettings(appSettings: AppSettings?): String? {
        return if (appSettings == null) null else Gson().toJson(appSettings)
    }

    @TypeConverter
    fun toAppSettings(appSettingsJson: String?): AppSettings? {
        return if (appSettingsJson.isNullOrBlank()) {
            null
        } else {
            try {
                Gson().fromJson(appSettingsJson, AppSettings::class.java)
            } catch (e: Exception) {
                android.util.Log.w("SogoGolferConverters", "Failed to parse AppSettings: $appSettingsJson", e)
                null
            }
        }
    }

    @TypeConverter
    fun fromSogoState(sogoState: SogoState?): String? {
        return if (sogoState == null) null else Gson().toJson(sogoState)
    }

    @TypeConverter
    fun toSogoState(sogoStateJson: String?): SogoState? {
        return if (sogoStateJson.isNullOrBlank()) {
            null
        } else {
            try {
                Gson().fromJson(sogoStateJson, SogoState::class.java)
            } catch (e: Exception) {
                android.util.Log.w("SogoGolferConverters", "Failed to parse SogoState: $sogoStateJson", e)
                null
            }
        }
    }
}
