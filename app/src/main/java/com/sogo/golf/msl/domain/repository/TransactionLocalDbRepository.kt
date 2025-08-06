package com.sogo.golf.msl.domain.repository

import com.sogo.golf.msl.data.local.database.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface TransactionLocalDbRepository {
    suspend fun getDebitTransactionsByGolferDateCompetition(
        golferId: String,
        startOfDay: Long,
        endOfDay: Long,
        mainCompetitionId: Int
    ): List<TransactionEntity>
    
    fun getTransactionsByGolfer(golferId: String): Flow<List<TransactionEntity>>
    
    suspend fun insertTransaction(transaction: TransactionEntity)
    
    suspend fun insertTransactions(transactions: List<TransactionEntity>)
    
    suspend fun deleteTransactionsByGolfer(golferId: String)
    
    suspend fun countDebitTransactionsByGolferDateCompetition(
        golferId: String,
        startOfDay: Long,
        endOfDay: Long,
        mainCompetitionId: Int
    ): Int
    
    suspend fun clearAllTransactions()
    
    suspend fun getTransactionCount(): Int
}
