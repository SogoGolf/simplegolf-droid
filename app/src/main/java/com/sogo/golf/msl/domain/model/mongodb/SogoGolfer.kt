package com.sogo.golf.msl.domain.model.mongodb

data class SogoGolfer(
    val id: String,
    val entityId: String?,
    val golfLinkNo: String,
    val firstName: String,
    val lastName: String,
    val email: String?,
    val phone: String?,
    val mobileNo: String?, // Mobile number field from MongoDB
    val dateOfBirth: String?, // ISO date string
    val handicap: Double?,
    val club: String?,
    val membershipType: String?,
    val isActive: Boolean,
    val createdAt: String?, // ISO date string
    val updatedAt: String?, // ISO date string
    val tokenBalance: Int = 0,
    val appSettings: AppSettings? = null,
    val postCode: String?, // Postcode field from MongoDB
    val state: SogoState?, // State object from MongoDB
    val gender: String?, // Gender field from MongoDB
    val authSystemUid: String? = null,
    val deviceManufacturer: String? = null,
    val deviceModel: String? = null,
    val deviceOS: String? = null,
    val deviceOSVersion: String? = null,
    val deviceToken: String? = null,
    val sogoAppVersion: String? = null,
    val country: String? = null,
    val userType: String? = null,
    val isInactive: Boolean? = null,
    val memberSince: String? = null
)

data class AppSettings(
    val id: String? = null,
    val notificationFlags: List<NotificationFlag>? = null,
    val isEnabledAutoTokenPayments: Boolean? = null,
    val isAcceptedSogoTermsAndConditions: Boolean? = null
)

data class NotificationFlag(
    val type: String? = null,
    val enabled: Boolean = false
)

data class SogoState(
    val alpha2: String? = null,
    val name: String? = null,
    val shortName: String? = null
)
