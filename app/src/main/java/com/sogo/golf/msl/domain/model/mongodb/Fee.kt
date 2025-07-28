package com.sogo.golf.msl.domain.model.mongodb

data class Fee(
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
    val updateDate: String? // ISO date string
)

