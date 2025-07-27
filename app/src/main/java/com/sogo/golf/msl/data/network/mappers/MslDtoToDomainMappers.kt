package com.sogo.golf.msl.data.network.mappers

import com.sogo.golf.msl.data.network.dto.*
import com.sogo.golf.msl.domain.model.msl.*

// Extension functions to map DTOs to domain models
fun MslClubDto.toDomainModel(): MslClub {
    return MslClub(
        clubId = clubId,
        name = name,
        logoUrl = logoUrl.takeIf { it.isNotBlank() },
        tenantId = tenantId,
        latitude = latitude,
        longitude = longitude,
        isGuestRegistrationEnabled = isGuestRegistrationEnabled,
        isChappGuestRegistrationEnabled = isChappGuestRegistrationEnabled,
        posLocationId = posLocationId,
        posTerminalId = posTerminalId,
        resourceId = resourceId
    )
}

fun MslGolferDto.toDomainModel(): MslGolfer {
    return MslGolfer(
        firstName = firstName,
        surname = surname,
        email = email?.takeIf { it.isNotBlank() },
        golfLinkNo = golfLinkNo,
        dateOfBirth = dateOfBirth,
        mobileNo = mobileNo?.takeIf { it.isNotBlank() },
        gender = gender?.takeIf { it.isNotBlank() },
        country = country ?: "australia",
        state = state?.takeIf { it.isNotBlank() },
        postCode = postCode?.takeIf { it.isNotBlank() },
        primary = primary
    )
}

fun PostAuthTokenResponseDto.toDomainModel(): MslTokens {
    return MslTokens(
        accessToken = accessToken,
        refreshToken = refreshToken,
        tokenType = tokenType,
        expiresIn = expiresIn.toLongOrNull() ?: 3600L, // Default to 1 hour if parsing fails
        issuedAt = System.currentTimeMillis()
    )
}

fun PostPrelimTokenResponseDto.toDomainModel(): MslPreliminaryToken {
    return MslPreliminaryToken(
        token = token,
        statusCode = statusCode
    )
}

// List mappers
fun List<MslClubDto>.toDomainModel(): List<MslClub> {
    return map { it.toDomainModel() }
}