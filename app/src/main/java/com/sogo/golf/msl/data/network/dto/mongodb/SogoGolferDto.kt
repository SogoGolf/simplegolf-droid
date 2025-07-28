package com.sogo.golf.msl.data.network.dto.mongodb

import com.google.gson.annotations.SerializedName
import com.sogo.golf.msl.domain.model.mongodb.SogoGolfer

data class SogoGolferDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("golflinkNo")
    val golfLinkNo: String,

    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("dateOfBirth")
    val dateOfBirth: String? = null,

    @SerializedName("handicap")
    val handicap: Double? = null,

    @SerializedName("club")
    val club: String? = null,

    @SerializedName("membershipType")
    val membershipType: String? = null,

    @SerializedName("isActive")
    val isActive: Boolean = true,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

fun SogoGolferDto.toDomainModel(): SogoGolfer {
    return SogoGolfer(
        id = id,
        golfLinkNo = golfLinkNo,
        firstName = firstName,
        lastName = lastName,
        email = email,
        phone = phone,
        dateOfBirth = dateOfBirth,
        handicap = handicap,
        club = club,
        membershipType = membershipType,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}