package com.sogo.golf.msl.data.repository.local

import android.util.Log
import com.sogo.golf.msl.data.local.database.dao.TransactionDao
import com.sogo.golf.msl.data.local.database.entities.TransactionEntity
import com.sogo.golf.msl.domain.repository.TransactionLocalDbRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionLocalDbRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionLocalDbRepository {

    companion object {
        private const val TAG = "TransactionLocalDbRepo"
    }

    override suspend fun getDebitTransactionsByGolferDateCompetition(
        golferId: String,
        startOfDay: Long,
        endOfDay: Long,
        mainCompetitionId: Int
    ): List<TransactionEntity> {
        Log.d(TAG, "getDebitTransactionsByGolferDateCompetition called for golfer: $golferId, competition: $mainCompetitionId")
        val transactions = transactionDao.getDebitTransactionsByGolferDateCompetition(golferId, startOfDay, endOfDay, mainCompetitionId)
        Log.d(TAG, "Found ${transactions.size} debit transactions")
        return transactions
    }

    override fun getTransactionsByGolfer(golferId: String): Flow<List<TransactionEntity>> {
        Log.d(TAG, "getTransactionsByGolfer called for golfer: $golferId")
        return transactionDao.getTransactionsByGolfer(golferId)
    }

    override suspend fun insertTransaction(transaction: TransactionEntity) {
        Log.d(TAG, "insertTransaction called for transaction: ${transaction.transactionId}")
        transactionDao.insertTransaction(transaction)
        Log.d(TAG, "Transaction inserted to database")
    }

    override suspend fun insertTransactions(transactions: List<TransactionEntity>) {
        Log.d(TAG, "insertTransactions called for ${transactions.size} transactions")
        transactionDao.insertTransactions(transactions)
        Log.d(TAG, "Transactions inserted to database")
    }

    override suspend fun deleteTransactionsByGolfer(golferId: String) {
        Log.d(TAG, "deleteTransactionsByGolfer called for golfer: $golferId")
        transactionDao.deleteTransactionsByGolfer(golferId)
        Log.d(TAG, "Transactions deleted for golfer")
    }

    override suspend fun countDebitTransactionsByGolferDateCompetition(
        golferId: String,
        startOfDay: Long,
        endOfDay: Long,
        mainCompetitionId: Int
    ): Int {
        Log.d(TAG, "countDebitTransactionsByGolferDateCompetition called for golfer: $golferId, competition: $mainCompetitionId")
        val count = transactionDao.countDebitTransactionsByGolferDateCompetition(golferId, startOfDay, endOfDay, mainCompetitionId)
        Log.d(TAG, "Found $count debit transactions")
        return count
    }

    override suspend fun clearAllTransactions() {
        Log.d(TAG, "clearAllTransactions called")
        transactionDao.clearAllTransactions()
        Log.d(TAG, "All transactions cleared from database")
    }

    override suspend fun getTransactionCount(): Int {
        Log.d(TAG, "getTransactionCount called")
        val count = transactionDao.getTransactionCount()
        Log.d(TAG, "Found $count transactions")
        return count
    }
}
