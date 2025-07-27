package com.sogo.golf.msl.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sogo.golf.msl.domain.model.msl.MslGolfer

@Entity(tableName = "golfer")
data class MslGolferEntity(
    @PrimaryKey
    val golfLinkNo: String,
    val firstName: String,
    val surname: String,
    val email: String?,
    val dateOfBirth: String,
    val mobileNo: String?,
    val gender: String?,
    val country: String,
    val state: String?,
    val postCode: String?,
    val primary: Double, // Handicap
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): MslGolfer {
        return MslGolfer(
            firstName = firstName,
            surname = surname,
            email = email,
            golfLinkNo = golfLinkNo,
            dateOfBirth = dateOfBirth,
            mobileNo = mobileNo,
            gender = gender,
            country = country,
            state = state,
            postCode = postCode,
            primary = primary
        )
    }

    companion object {
        fun fromDomainModel(golfer: MslGolfer): MslGolferEntity {
            return MslGolferEntity(
                golfLinkNo = golfer.golfLinkNo,
                firstName = golfer.firstName,
                surname = golfer.surname,
                email = golfer.email,
                dateOfBirth = golfer.dateOfBirth,
                mobileNo = golfer.mobileNo,
                gender = golfer.gender,
                country = golfer.country,
                state = golfer.state,
                postCode = golfer.postCode,
                primary = golfer.primary
            )
        }
    }
}