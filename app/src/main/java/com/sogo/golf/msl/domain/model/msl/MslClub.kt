package com.sogo.golf.msl.domain.model.msl

data class MslClub(
    val clubId: Int,
    val name: String,
    val logoUrl: String?,
    val tenantId: String,
    val latitude: Int,
    val longitude: Int,
    val isGuestRegistrationEnabled: Boolean,
    val isChappGuestRegistrationEnabled: Boolean,
    val posLocationId: String,
    val posTerminalId: String,
    val resourceId: String
)