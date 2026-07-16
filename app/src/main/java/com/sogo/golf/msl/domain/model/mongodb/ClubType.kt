package com.sogo.golf.msl.domain.model.mongodb

// A selectable golf club (equipment) from GET /clubTypes, for the Hole Stats club picker.
data class ClubType(
    val code: String,
    val name: String,
    val category: String,
    val sortOrder: Int
)
