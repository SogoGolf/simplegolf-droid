package com.sogo.golf.msl.data.network.dto.mongodb

import com.google.gson.annotations.SerializedName

data class TransactionDto(
    @SerializedName("_id")
    val id: String = "",
    @SerializedName("entityId")
    val entityId: String? = null,
    @SerializedName("transactionId")
    val transactionId: String = "",
    @SerializedName("golferId")
    val golferId: String = "",
    @SerializedName("golferEmail")
    val golferEmail: String? = null,
    @SerializedName("transactionDate")
    val transactionDate: String? = null,
    @SerializedName("amount")
    val amount: Int = 0,
    @SerializedName("transactionType")
    val transactionType: String = "",
    @SerializedName("debitCreditType")
    val debitCreditType: String = "",
    @SerializedName("comment")
    val comment: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("mainCompetitionId")
    val mainCompetitionId: Int? = null
)
