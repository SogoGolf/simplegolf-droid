// app/src/main/java/com/sogo/golf/msl/data/local/database/entities/GameConverters.kt
package com.sogo.golf.msl.data.local.database.entities

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sogo.golf.msl.domain.model.msl.MslGameCompetition
import com.sogo.golf.msl.domain.model.msl.MslPlayingPartner

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
}