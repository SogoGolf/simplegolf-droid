package com.sogo.golf.msl.data.network.dto.mongodb

import com.google.gson.annotations.SerializedName
import com.sogo.golf.msl.domain.model.mongodb.ClubType

data class ClubTypeDto(
    @SerializedName("code") val code: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("category") val category: String = "",
    @SerializedName("sortOrder") val sortOrder: Int = 0
)

fun ClubTypeDto.toDomain(): ClubType = ClubType(
    code = code,
    name = name,
    category = category,
    sortOrder = sortOrder
)
