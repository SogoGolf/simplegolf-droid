package com.sogo.golf.msl.data.network.dto

data class MslGolferDto(
    val country: String? = "australia",
    val dateOfBirth: String,
    val email: String?,
    val firstName: String,
    val gender: String?,
    val golfLinkNo: String,
    val mobileNo: String?,
    val postCode: String?,
    val primary: Double,
    val state: String?,
    val surname: String
)