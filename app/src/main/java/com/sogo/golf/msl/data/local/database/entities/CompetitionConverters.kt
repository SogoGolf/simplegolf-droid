package com.sogo.golf.msl.data.local.database.entities

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sogo.golf.msl.domain.model.msl.MslPlayer

class CompetitionConverters {

    @TypeConverter
    fun fromPlayerList(players: List<MslPlayer>): String {
        return Gson().toJson(players)
    }

    @TypeConverter
    fun toPlayerList(playersJson: String): List<MslPlayer> {
        val listType = object : TypeToken<List<MslPlayer>>() {}.type
        return Gson().fromJson(playersJson, listType)
    }
}
