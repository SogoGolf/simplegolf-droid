package com.sogo.golf.msl.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sogo.golf.msl.data.local.database.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    
    @Query("SELECT * FROM transactions WHERE golferId = :golferId AND transactionDate >= :startOfDay AND transactionDate <= :endOfDay AND mainCompetitionId = :mainCompetitionId AND debitCreditType = 'DEBIT'")
    suspend fun getDebitTransactionsByGolferDateCompetition(
        golferId: String,
        startOfDay: Long,
        endOfDay: Long,
        mainCompetitionId: Int
    ): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE golferId = :golferId ORDER BY transactionDate DESC")
    fun getTransactionsByGolfer(golferId: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE golferId = :golferId")
    suspend fun deleteTransactionsByGolfer(golferId: String)

    @Query("SELECT COUNT(*) FROM transactions WHERE golferId = :golferId AND transactionDate >= :startOfDay AND transactionDate <= :endOfDay AND mainCompetitionId = :mainCompetitionId AND debitCreditType = 'DEBIT'")
    suspend fun countDebitTransactionsByGolferDateCompetition(
        golferId: String,
        startOfDay: Long,
        endOfDay: Long,
        mainCompetitionId: Int
    ): Int
}
