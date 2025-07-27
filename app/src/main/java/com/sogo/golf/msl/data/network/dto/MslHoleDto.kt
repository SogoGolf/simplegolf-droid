package com.sogo.golf.msl.data.network.dto

data class MslHoleDto(
    val par: Int,
    val strokes: Int,
    val strokeIndexes: List<Int>,
    val distance: Int,
    val holeNumber: Int,
    val holeName: String?,
    val holeAlias: String?
)