package com.sogo.golf.msl.domain.model.msl

data class MslGolfer(
    val firstName: String,
    val surname: String,
    val email: String?,
    val golfLinkNo: String,
    val dateOfBirth: String,
    val mobileNo: String?,
    val gender: String?,
    val country: String,
    val state: String?,
    val postCode: String?,
    val primary: Double // Handicap
)