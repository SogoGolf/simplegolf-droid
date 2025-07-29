package com.sogo.golf.msl.domain.model.mongodb

data class SogoGolfer(
    val id: String,
    val entityId: String?,
    val golfLinkNo: String,
    val firstName: String,
    val lastName: String,
    val email: String?,
    val phone: String?,
    val dateOfBirth: String?, // ISO date string
    val handicap: Double?,
    val club: String?,
    val membershipType: String?,
    val isActive: Boolean,
    val createdAt: String?, // ISO date string
    val updatedAt: String?, // ISO date string
    val tokenBalance: Int = 0
)
