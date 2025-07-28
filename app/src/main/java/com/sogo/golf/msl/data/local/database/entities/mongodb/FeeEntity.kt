package com.sogo.golf.msl.data.local.database.entities.mongodb

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sogo.golf.msl.domain.model.mongodb.Fee

@Entity(tableName = "fees")
data class FeeEntity(
    @PrimaryKey
    val id: String,
    val uuid: String,
    val entityId: String,
    val entityName: String,
    val numberHoles: Int,
    val cost: Double,
    val description: String,
    val isWaived: Boolean,
    val item: String,
    val appIsFreeText: String,
    val updateDate: String?,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): Fee {
        return Fee(
            id = id,
            uuid = uuid,
            entityId = entityId,
            entityName = entityName,
            numberHoles = numberHoles,
            cost = cost,
            description = description,
            isWaived = isWaived,
            item = item,
            appIsFreeText = appIsFreeText,
            updateDate = updateDate
        )
    }

    companion object {
        fun fromDomainModel(fee: Fee): FeeEntity {
            return FeeEntity(
                id = fee.id,
                uuid = fee.uuid,
                entityId = fee.entityId,
                entityName = fee.entityName,
                numberHoles = fee.numberHoles,
                cost = fee.cost,
                description = fee.description,
                isWaived = fee.isWaived,
                item = fee.item,
                appIsFreeText = fee.appIsFreeText,
                updateDate = fee.updateDate
            )
        }
    }
}

