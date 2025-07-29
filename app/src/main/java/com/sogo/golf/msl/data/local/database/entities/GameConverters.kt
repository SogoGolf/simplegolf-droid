// app/src/main/java/com/sogo/golf/msl/data/local/database/entities/GameConverters.kt
package com.sogo.golf.msl.data.local.database.entities

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sogo.golf.msl.domain.model.msl.MslGameCompetition
import com.sogo.golf.msl.domain.model.msl.MslPlayingPartner
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

class GameConverters {

    @TypeConverter
    fun fromPlayingPartnerList(partners: List<MslPlayingPartner>): String {
        return Gson().toJson(partners)
    }

    @TypeConverter
    fun toPlayingPartnerList(partnersJson: String): List<MslPlayingPartner> {
        val listType = object : TypeToken<List<MslPlayingPartner>>() {}.type
        return Gson().fromJson(partnersJson, listType)
    }

    @TypeConverter
    fun fromGameCompetitionList(competitions: List<MslGameCompetition>): String {
        return Gson().toJson(competitions)
    }

    @TypeConverter
    fun toGameCompetitionList(competitionsJson: String): List<MslGameCompetition> {
        val listType = object : TypeToken<List<MslGameCompetition>>() {}.type
        return Gson().fromJson(competitionsJson, listType)
    }

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return try {
            if (dateTimeString.isNullOrBlank()) {
                null
            } else {
                LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }
        } catch (e: Exception) {
            android.util.Log.w("GameConverters", "Failed to parse LocalDateTime: $dateTimeString", e)
            null
        }
    }
}
