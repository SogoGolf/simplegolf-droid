// app/src/main/java/com/sogo/golf/msl/data/local/database/entities/MslGameEntity.kt
package com.sogo.golf.msl.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.sogo.golf.msl.domain.model.msl.MslGame
import com.sogo.golf.msl.domain.model.msl.MslGameCompetition
import com.sogo.golf.msl.domain.model.msl.MslPlayingPartner

@Entity(tableName = "games")
@TypeConverters(GameConverters::class)
data class MslGameEntity(
    @PrimaryKey
    val id: String, // We'll use clubId as the primary key since games are club-specific
    val errorMessage: String?,
    val scorecardMessageOfTheDay: String?,
    val startingHoleNumber: Int,
    val mainCompetitionId: Int,
    val golflinkNumber: String?,
    val teeName: String?,
    val teeColourName: String?,
    val teeColour: String?,
    val dailyHandicap: Int?,
    val gaHandicap: Double?,
    val numberOfHoles: Int?,
    val playingPartners: List<MslPlayingPartner>, // Will be converted to JSON
    val competitions: List<MslGameCompetition>, // Will be converted to JSON
    val lastUpdated: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
) {
    fun toDomainModel(): MslGame {
        return MslGame(
            errorMessage = errorMessage,
            scorecardMessageOfTheDay = scorecardMessageOfTheDay,
            startingHoleNumber = startingHoleNumber,
            mainCompetitionId = mainCompetitionId,
            golflinkNumber = golflinkNumber,
            teeName = teeName,
            teeColourName = teeColourName,
            teeColour = teeColour,
            dailyHandicap = dailyHandicap,
            gaHandicap = gaHandicap,
            numberOfHoles = numberOfHoles,
            playingPartners = playingPartners,
            competitions = competitions
        )
    }

    companion object {
        fun fromDomainModel(game: MslGame, gameId: String): MslGameEntity {
            return MslGameEntity(
                id = gameId,
                errorMessage = game.errorMessage,
                scorecardMessageOfTheDay = game.scorecardMessageOfTheDay,
                startingHoleNumber = game.startingHoleNumber,
                mainCompetitionId = game.mainCompetitionId,
                golflinkNumber = game.golflinkNumber,
                teeName = game.teeName,
                teeColourName = game.teeColourName,
                teeColour = game.teeColour,
                dailyHandicap = game.dailyHandicap,
                gaHandicap = game.gaHandicap,
                numberOfHoles = game.numberOfHoles,
                playingPartners = game.playingPartners,
                competitions = game.competitions
            )
        }
    }
}