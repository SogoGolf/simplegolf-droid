package com.sogo.golf.msl.domain.model.mongodb

data class Competition(
    val id: String? = null,
    val name: String? = null,
    val shortDescription: String? = null,
    val sortIndex: Int = 0,
    val startDate: String? = null,
    val endDate: String? = null,
    val isActive: Boolean = false
)