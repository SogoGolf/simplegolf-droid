package com.sogo.golf.msl.data.local.database.entities

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sogo.golf.msl.domain.model.StateInfo
import com.sogo.golf.msl.domain.model.HoleScore
import com.sogo.golf.msl.domain.model.PlayingPartnerRound
import com.sogo.golf.msl.domain.model.MslMetaData
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

class RoundConverters {
    
    private val gson = Gson()
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    @TypeConverter
    fun fromBoolean(value: Boolean?): Int? {
        return value?.let { if (it) 1 else 0 }
    }

    @TypeConverter
    fun toBoolean(value: Int?): Boolean? {
        return value?.let { it == 1 }
    }
    
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.format(formatter)
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { 
            try {
                LocalDateTime.parse(it, formatter)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    @TypeConverter
    fun fromStateInfo(value: StateInfo?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStateInfo(value: String?): StateInfo? {
        return value?.let { gson.fromJson(it, StateInfo::class.java) }
    }
    
    @TypeConverter
    fun fromHoleScoreList(value: List<HoleScore>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toHoleScoreList(value: String?): List<HoleScore>? {
        return value?.let { 
            val type = object : TypeToken<List<HoleScore>>() {}.type
            gson.fromJson<List<HoleScore>>(it, type)
        }
    }
    
    @TypeConverter
    fun fromPlayingPartnerRound(value: PlayingPartnerRound?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toPlayingPartnerRound(value: String?): PlayingPartnerRound? {
        return value?.let { gson.fromJson(it, PlayingPartnerRound::class.java) }
    }
    
    @TypeConverter
    fun fromMslMetaData(value: MslMetaData?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toMslMetaData(value: String?): MslMetaData? {
        return value?.let { gson.fromJson(it, MslMetaData::class.java) }
    }
}
