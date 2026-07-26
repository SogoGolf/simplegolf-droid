package com.sogo.golf.msl.data.network.dto.mongodb

import com.google.gson.annotations.SerializedName
import com.sogo.golf.msl.domain.model.mongodb.HoleInsights
import com.sogo.golf.msl.domain.model.mongodb.HoleInsightsDistribution
import com.sogo.golf.msl.domain.model.mongodb.HoleInsightsFieldGross
import com.sogo.golf.msl.domain.model.mongodb.HoleInsightsFieldNet
import com.sogo.golf.msl.domain.model.mongodb.HoleInsightsItem

data class HoleInsightsResponseDto(
    @SerializedName("holeNumber") val holeNumber: Int = 0,
    @SerializedName("par") val par: Int = 0,
    @SerializedName("yourRoundsCount") val yourRoundsCount: Int = 0,
    @SerializedName("yourAvg") val yourAvg: Double? = null,
    @SerializedName("yourBest") val yourBest: Int? = null,
    @SerializedName("history") val history: List<HoleInsightsItemDto> = emptyList(),
    @SerializedName("distribution") val distribution: HoleInsightsDistributionDto? = null,
    @SerializedName("fieldGross") val fieldGross: HoleInsightsFieldGrossDto? = null,
    @SerializedName("fieldNet") val fieldNet: HoleInsightsFieldNetDto? = null
)

data class HoleInsightsItemDto(
    @SerializedName("date") val date: String? = null,
    @SerializedName("strokes") val strokes: Int = 0,
    @SerializedName("par") val par: Int = 0
)

data class HoleInsightsDistributionDto(
    @SerializedName("eagle") val eagle: Int = 0,
    @SerializedName("birdie") val birdie: Int = 0,
    @SerializedName("par") val par: Int = 0,
    @SerializedName("bogey") val bogey: Int = 0,
    @SerializedName("doublePlus") val doublePlus: Int = 0
)

data class HoleInsightsFieldGrossDto(
    @SerializedName("avg") val avg: Double = 0.0,
    @SerializedName("count") val count: Int = 0,
    @SerializedName("hcpLow") val hcpLow: Double = 0.0,
    @SerializedName("hcpHigh") val hcpHigh: Double = 0.0,
    @SerializedName("widened") val widened: Boolean = false
)

data class HoleInsightsFieldNetDto(
    @SerializedName("avg") val avg: Double = 0.0,
    @SerializedName("count") val count: Int = 0
)

fun HoleInsightsResponseDto.toDomain(): HoleInsights = HoleInsights(
    holeNumber = holeNumber,
    par = par,
    yourRoundsCount = yourRoundsCount,
    yourAvg = yourAvg,
    yourBest = yourBest,
    history = history.map { HoleInsightsItem(date = it.date, strokes = it.strokes, par = it.par) },
    distribution = distribution?.let {
        HoleInsightsDistribution(it.eagle, it.birdie, it.par, it.bogey, it.doublePlus)
    },
    fieldGross = fieldGross?.let {
        HoleInsightsFieldGross(it.avg, it.count, it.hcpLow, it.hcpHigh, it.widened)
    },
    fieldNet = fieldNet?.let { HoleInsightsFieldNet(it.avg, it.count) }
)
