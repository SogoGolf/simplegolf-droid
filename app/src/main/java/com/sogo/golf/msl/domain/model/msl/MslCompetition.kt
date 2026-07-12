package com.sogo.golf.msl.domain.model.msl

//MSL data model
data class MslCompetition(
    val players: List<MslPlayer>
)

data class MslPlayer(
    val firstName: String?,
    val lastName: String?,
    val dailyHandicap: Int,
    val golfLinkNumber: String?,
    val competitionName: String?,
    val competitionType: String?,
    val teeName: String?,
    val teeColour: String?,
    val teeColourName: String?,
    val scoreType: String?,
    val slopeRating: Int,
    val scratchRating: Double,
    val gender: String?,
    val holes: List<MslHole>
)

data class MslHole(
    val par: Int,
    val strokeIndexes: List<StrokeIndex>,
    val distance: Int,
    val holeNumber: Int,
    val holeName: String?,
    val holeAlias: String?,
    val extraStrokes: Int,
    /** Expected minutes to complete this hole (pace of play), from V3 golferinfo.
     *  0 when the source doesn't provide it (e.g. legacy payloads). */
    val playTimeMinutes: Int = 0
)

data class StrokeIndex(
    val courseHandicapFrom: Int,
    val courseHandicapTo: Int,
    val stroke: Int
)
