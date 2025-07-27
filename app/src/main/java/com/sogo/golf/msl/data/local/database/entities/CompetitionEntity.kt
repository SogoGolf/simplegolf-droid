package com.sogo.golf.msl.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sogo.golf.msl.domain.model.msl.MslCompetition
import com.sogo.golf.msl.domain.model.msl.MslPlayer

@Entity(tableName = "competitions")
@TypeConverters(CompetitionConverters::class)
data class CompetitionEntity(
    @PrimaryKey
    val id: String,
    val competitionName: String?,
    val competitionType: String?,
    val players: List<MslPlayer>, // Will be converted to JSON
    val lastUpdated: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
) {
    fun toDomainModel(): MslCompetition {
        return MslCompetition(players = players)
    }

    companion object {
        fun fromDomainModel(competition: MslCompetition, id: String, competitionName: String? = null): CompetitionEntity {
            return CompetitionEntity(
                id = id,
                competitionName = competitionName ?: competition.players.firstOrNull()?.competitionName,
                competitionType = competition.players.firstOrNull()?.competitionType,
                players = competition.players
            )
        }
    }
}