package com.sogo.golf.msl.domain.model.msl

//MSL data model
data class Competition(
    val players: List<Player>
)

data class Player(
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
    val holes: List<Hole>
)

data class Hole(
    val par: Int,
    val strokes: Int,
    val strokeIndexes: List<Int>,
    val distance: Int,
    val holeNumber: Int,
    val holeName: String?,
    val holeAlias: String?
)