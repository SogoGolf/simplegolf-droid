package com.sogo.golf.msl.data.network.dto

import com.sogo.golf.msl.domain.model.msl.MslHole

data class MslHoleDto(
    val par: Int,
    val strokes: Int,
    val strokeIndexes: List<Int>,
    val distance: Int,
    val holeNumber: Int,
    val holeName: String?,
    val holeAlias: String?
)

fun MslHoleDto.toDomainModel(): MslHole {
    return MslHole(
        par = par,
        strokes = strokes,
        strokeIndexes = strokeIndexes,
        distance = distance,
        holeNumber = holeNumber,
        holeName = holeName,
        holeAlias = holeAlias
    )
}