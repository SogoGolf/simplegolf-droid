package com.sogo.golf.msl.data.network.dto.mongodb

import com.google.gson.annotations.SerializedName
import com.sogo.golf.msl.domain.model.mongodb.Competition

data class CompetitionDto(
    @SerializedName("id")
    val id: String? = null,
    
    @SerializedName("name")
    val name: String? = null,
    
    @SerializedName("shortDescription")
    val shortDescription: String? = null,
    
    @SerializedName("sortIndex")
    val sortIndex: Int? = null,
    
    @SerializedName("startDate")
    val startDate: String? = null,
    
    @SerializedName("endDate")
    val endDate: String? = null,
    
    @SerializedName("isActive")
    val isActive: Boolean? = null
)

fun CompetitionDto.toDomain(): Competition {
    return Competition(
        id = id,
        name = name,
        shortDescription = shortDescription,
        sortIndex = sortIndex ?: 0,
        startDate = startDate,
        endDate = endDate,
        isActive = isActive ?: false
    )
}

fun List<CompetitionDto>.toDomain(): List<Competition> {
    return this.map { it.toDomain() }
}