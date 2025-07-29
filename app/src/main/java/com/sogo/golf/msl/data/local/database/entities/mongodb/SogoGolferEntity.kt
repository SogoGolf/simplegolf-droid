package com.sogo.golf.msl.data.local.database.entities.mongodb

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sogo.golf.msl.domain.model.mongodb.SogoGolfer

@Entity(tableName = "sogo_golfers")
data class SogoGolferEntity(
    @PrimaryKey
    val id: String,
    val entityId: String?,
    val golfLinkNo: String,
    val firstName: String,
    val lastName: String,
    val email: String?,
    val phone: String?,
    val dateOfBirth: String?,
    val handicap: Double?,
    val club: String?,
    val membershipType: String?,
    val isActive: Boolean,
    val createdAt: String?,
    val updatedAt: String?,
    val tokenBalance: Int, // ✅ NEW: Token balance field
    val lastUpdated: Long = System.currentTimeMillis()
){
    fun toDomainModel(): SogoGolfer {
        return SogoGolfer(
            id = id,
            entityId = entityId,
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
            updatedAt = updatedAt,
            tokenBalance = tokenBalance // ✅ NEW: Map token balance
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
                dateOfBirth = sogoGolfer.dateOfBirth,
                handicap = sogoGolfer.handicap,
                club = sogoGolfer.club,
                membershipType = sogoGolfer.membershipType,
                isActive = sogoGolfer.isActive,
                createdAt = sogoGolfer.createdAt,
                updatedAt = sogoGolfer.updatedAt,
                tokenBalance = sogoGolfer.tokenBalance
            )
        }
    }
}
