package com.sogo.golf.msl.data.local.database.entities.mongodb

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.sogo.golf.msl.domain.model.mongodb.SogoGolfer
import com.sogo.golf.msl.domain.model.mongodb.AppSettings
import com.sogo.golf.msl.domain.model.mongodb.SogoState

@Entity(tableName = "sogo_golfers")
@TypeConverters(SogoGolferConverters::class)
data class SogoGolferEntity(
    @PrimaryKey
    val id: String,
    val entityId: String?,
    val golfLinkNo: String,
    val firstName: String,
    val lastName: String,
    val email: String?,
    val phone: String?,
    val mobileNo: String?,
    val dateOfBirth: String?,
    val handicap: Double?,
    val club: String?,
    val membershipType: String?,
    val isActive: Boolean,
    val createdAt: String?,
    val updatedAt: String?,
    val tokenBalance: Int, // ✅ Token balance field
    val appSettings: AppSettings?, // ✅ Store as JSON instead of flat column
    val postCode: String?,
    val state: SogoState?,
    val gender: String?,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): SogoGolfer {
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
            appSettings = appSettings, // ✅ Use the appSettings field directly
            postCode = postCode,
            state = state,
            gender = gender
        )
    }

    companion object {
        fun fromDomainModel(sogoGolfer: SogoGolfer): SogoGolferEntity {
            return SogoGolferEntity(
                id = sogoGolfer.id,
                entityId = sogoGolfer.entityId,
                golfLinkNo = sogoGolfer.golfLinkNo,
                firstName = sogoGolfer.firstName,
                lastName = sogoGolfer.lastName,
                email = sogoGolfer.email,
                phone = sogoGolfer.phone,
                mobileNo = sogoGolfer.mobileNo,
                dateOfBirth = sogoGolfer.dateOfBirth,
                handicap = sogoGolfer.handicap,
                club = sogoGolfer.club,
                membershipType = sogoGolfer.membershipType,
                isActive = sogoGolfer.isActive,
                createdAt = sogoGolfer.createdAt,
                updatedAt = sogoGolfer.updatedAt,
                tokenBalance = sogoGolfer.tokenBalance,
                appSettings = sogoGolfer.appSettings, // ✅ Use appSettings directly
                postCode = sogoGolfer.postCode,
                state = sogoGolfer.state,
                gender = sogoGolfer.gender
            )
        }
    }
}
