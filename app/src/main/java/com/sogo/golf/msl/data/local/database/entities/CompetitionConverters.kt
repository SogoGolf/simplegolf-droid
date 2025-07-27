package com.sogo.golf.msl.data.local.database.entities

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sogo.golf.msl.domain.model.msl.Player

class CompetitionConverters {

    @TypeConverter
    fun fromPlayerList(players: List<Player>): String {
        return Gson().toJson(players)
    }

    @TypeConverter
    fun toPlayerList(playersJson: String): List<Player> {
        val listType = object : TypeToken<List<Player>>() {}.type
        return Gson().fromJson(playersJson, listType)
    }
}
