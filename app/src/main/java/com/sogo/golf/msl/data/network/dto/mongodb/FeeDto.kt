package com.sogo.golf.msl.data.network.dto.mongodb

import com.google.gson.annotations.SerializedName
import com.sogo.golf.msl.domain.model.mongodb.Fee


data class FeeDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("uuid")
    val uuid: String,

    @SerializedName("entityId")
    val entityId: String,

    @SerializedName("entityName")
    val entityName: String,

    @SerializedName("numberHoles")
    val numberHoles: Int,

    @SerializedName("cost")
    val cost: Double,

    @SerializedName("description")
    val description: String,

    @SerializedName("isWaived")
    val isWaived: Boolean,

    @SerializedName("item")
    val item: String,

    @SerializedName("appIsFreeText")
    val appIsFreeText: String,

    @SerializedName("updateDate")
    val updateDate: String? = null
)

fun FeeDto.toDomainModel(): Fee {
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

// List mapper
fun List<FeeDto>.toDomainModel(): List<Fee> {
    return mapNotNull { feeDto ->
        try {
            feeDto.toDomainModel()
        } catch (e: Exception) {
            // Log the problematic fee and skip it
            android.util.Log.e("FeeMappers", "Failed to map fee: $feeDto", e)
            null
        }
    }
}