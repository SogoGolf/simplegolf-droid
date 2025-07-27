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
    val holes: List<MslHole>
)

data class MslHole(
    val par: Int,
    val strokes: Int,
    val strokeIndexes: List<Int>,
    val distance: Int,
    val holeNumber: Int,
    val holeName: String?,
    val holeAlias: String?
)