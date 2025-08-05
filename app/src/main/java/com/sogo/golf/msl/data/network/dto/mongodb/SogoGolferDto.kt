package com.sogo.golf.msl.data.network.dto.mongodb

import com.google.gson.annotations.SerializedName
import com.sogo.golf.msl.domain.model.mongodb.SogoGolfer
import com.sogo.golf.msl.domain.model.mongodb.AppSettings
import com.sogo.golf.msl.domain.model.mongodb.SogoState

data class SogoGolferDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("entityId")
    val entityId: String? = null,

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

    @SerializedName("mobileNo")
    val mobileNo: String? = null,

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
    val updatedAt: String? = null,

    @SerializedName("tokenBalance")
    val tokenBalance: Int = 0,

    @SerializedName("appSettings")
    val appSettings: AppSettingsDto? = null,

    @SerializedName("postCode")
    val postCode: String? = null,

    @SerializedName("state")
    val state: SogoStateDto? = null,

    @SerializedName("gender")
    val gender: String? = null
)

data class AppSettingsDto(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("notificationFlags")
    val notificationFlags: List<NotificationFlagDto>? = null,

    @SerializedName("isEnabledAutoTokenPayments")
    val isEnabledAutoTokenPayments: Boolean? = null,

    @SerializedName("isAcceptedSogoTermsAndConditions")
    val isAcceptedSogoTermsAndConditions: Boolean? = null
)

data class NotificationFlagDto(
    @SerializedName("type")
    val type: String? = null,

    @SerializedName("enabled")
    val enabled: Boolean = false
)

data class SogoStateDto(
    @SerializedName("alpha2")
    val alpha2: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("shortName")
    val shortName: String? = null
)

fun SogoGolferDto.toDomainModel(): SogoGolfer {
    return SogoGolfer(
        id = id,
        entityId = entityId,
        golfLinkNo = golfLinkNo,
        firstName = firstName,
        lastName = lastName,
        email = email,
        phone = phone,
        mobileNo = mobileNo,
        dateOfBirth = dateOfBirth,
        handicap = handicap,
        club = club,
        membershipType = membershipType,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt,
        tokenBalance = tokenBalance,
        appSettings = appSettings?.let { dto ->
            AppSettings(
                id = dto.id,
                notificationFlags = dto.notificationFlags?.map { flagDto ->
                    com.sogo.golf.msl.domain.model.mongodb.NotificationFlag(
                        type = flagDto.type,
                        enabled = flagDto.enabled
                    )
                },
                isEnabledAutoTokenPayments = dto.isEnabledAutoTokenPayments,
                isAcceptedSogoTermsAndConditions = dto.isAcceptedSogoTermsAndConditions
            )
        },
        postCode = postCode,
        state = state?.let { stateDto ->
            SogoState(
                alpha2 = stateDto.alpha2,
                name = stateDto.name,
                shortName = stateDto.shortName
            )
        },
        gender = gender
    )
}
