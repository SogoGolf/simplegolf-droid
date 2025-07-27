package com.sogo.golf.msl.data.network.dto

import com.google.gson.annotations.SerializedName

// Step 1: Club list response
data class MslClubDto(
    val clubId: Int,
    val isChappGuestRegistrationEnabled: Boolean,
    val isGuestRegistrationEnabled: Boolean,
    val latitude: Int,
    val logoUrl: String,
    val longitude: Int,
    val name: String,
    val posLocationId: String,
    val posTerminalId: String,
    val resourceId: String,
    val tenantId: String
)