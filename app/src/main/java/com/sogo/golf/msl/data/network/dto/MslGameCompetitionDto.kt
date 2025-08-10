package com.sogo.golf.msl.data.network.dto

import com.sogo.golf.msl.domain.model.msl.MslGameCompetition

data class MslGameCompetitionDto(
    val id: Int,
    val name: String,
    val scoreType: String,
    val type: String?
)

fun MslGameCompetitionDto.toDomainModel(): MslGameCompetition {
    return MslGameCompetition(
        id = id,
        name = name,
        scoreType = scoreType,
        type = type ?: ""
    )
}
