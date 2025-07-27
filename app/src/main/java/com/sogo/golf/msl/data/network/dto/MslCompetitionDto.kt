package com.sogo.golf.msl.data.network.dto

import com.sogo.golf.msl.domain.model.msl.MslCompetition

data class MslCompetitionDto(
    val players: List<MslPlayerDto>
)

fun MslCompetitionDto.toDomainModel(): MslCompetition {
    return MslCompetition(
        players = players.map { it.toDomainModel() }
    )
}