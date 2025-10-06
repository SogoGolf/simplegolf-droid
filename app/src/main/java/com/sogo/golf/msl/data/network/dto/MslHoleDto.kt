package com.sogo.golf.msl.data.network.dto

import com.sogo.golf.msl.domain.model.msl.MslHole
import com.sogo.golf.msl.domain.model.msl.StrokeIndex

data class MslHoleDto(
    val par: Int,
    val strokeIndexes: List<StrokeIndexDto>,
    val distance: Int,
    val holeNumber: Int,
    val holeName: String?,
    val holeAlias: String?,
    val extraStrokes: Int
)

fun MslHoleDto.toDomainModel(): MslHole {
    return MslHole(
        par = par,
        strokeIndexes = strokeIndexes.map {
            StrokeIndex(
                courseHandicapFrom = it.courseHandicapFrom,
                courseHandicapTo = it.courseHandicapTo,
                stroke = it.stroke
            )
        },
        distance = distance,
        holeNumber = holeNumber,
        holeName = holeName,
        holeAlias = holeAlias,
        extraStrokes = extraStrokes
    )
}