package com.sogo.golf.msl.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val entityId: String?,
    val transactionId: String,
    val golferId: String,
    val golferEmail: String?,
    val transactionDate: Long,
    val amount: Int,
    val transactionType: String,
    val debitCreditType: String,
    val comment: String?,
    val status: String?,
    val mainCompetitionId: Int?,
    val lastUpdated: Long,
    val isSynced: Boolean
)
