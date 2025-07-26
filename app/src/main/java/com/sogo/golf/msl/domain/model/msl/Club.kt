package com.sogo.golf.msl.domain.model.msl


data class Club(
    val clubId: Int,
    val name: String,
    val tenantId: String?,
    val logoUrl: String?
)
