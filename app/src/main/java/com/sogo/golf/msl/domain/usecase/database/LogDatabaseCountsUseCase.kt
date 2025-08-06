package com.sogo.golf.msl.domain.usecase.database

import android.util.Log
import com.sogo.golf.msl.domain.repository.FeeLocalDbRepository
import com.sogo.golf.msl.domain.repository.MslCompetitionLocalDbRepository
import com.sogo.golf.msl.domain.repository.MslGameLocalDbRepository
import com.sogo.golf.msl.domain.repository.MslGolferLocalDbRepository
import com.sogo.golf.msl.domain.repository.RoundLocalDbRepository
import com.sogo.golf.msl.domain.repository.SogoGolferLocalDbRepository
import com.sogo.golf.msl.domain.repository.TransactionLocalDbRepository
import javax.inject.Inject

class LogDatabaseCountsUseCase @Inject constructor(
    private val mslGolferRepository: MslGolferLocalDbRepository,
    private val mslCompetitionRepository: MslCompetitionLocalDbRepository,
    private val mslGameRepository: MslGameLocalDbRepository,
    private val roundRepository: RoundLocalDbRepository,
    private val sogoGolferRepository: SogoGolferLocalDbRepository,
    private val feeRepository: FeeLocalDbRepository,
    private val transactionRepository: TransactionLocalDbRepository
) {
    
    companion object {
        private const val TAG = "DatabaseCounts"
    }
    
    suspend operator fun invoke(prefix: String = "") {
        try {
            val logPrefix = if (prefix.isNotEmpty()) "$prefix - " else ""
            
            Log.d(TAG, "${logPrefix}=== DATABASE TABLE COUNTS ===")
            
            // Get counts from all tables
            val golferCount = mslGolferRepository.getGolferCount()
            val competitionCount = mslCompetitionRepository.getCompetitionCount()
            val gameCount = mslGameRepository.getGameCount()
            val roundCount = roundRepository.getRoundCount()
            val sogoGolferCount = sogoGolferRepository.getSogoGolferCount()
            val feeCount = feeRepository.getFeeCount()
            val transactionCount = transactionRepository.getTransactionCount()
            
            // Log each table count
            Log.d(TAG, "${logPrefix}golfer table: $golferCount records")
            Log.d(TAG, "${logPrefix}competitions table: $competitionCount records")
            Log.d(TAG, "${logPrefix}games table: $gameCount records")
            Log.d(TAG, "${logPrefix}rounds table: $roundCount records")
            Log.d(TAG, "${logPrefix}sogo_golfers table: $sogoGolferCount records")
            Log.d(TAG, "${logPrefix}fees table: $feeCount records")
            Log.d(TAG, "${logPrefix}transactions table: $transactionCount records")
            
            // Calculate total records
            val totalRecords = golferCount + competitionCount + gameCount + 
                             roundCount + sogoGolferCount + feeCount + transactionCount
            
            Log.d(TAG, "${logPrefix}TOTAL RECORDS: $totalRecords")
            Log.d(TAG, "${logPrefix}================================")
            
        } catch (e: Exception) {
            Log.e(TAG, "${prefix}Error logging database counts", e)
        }
    }
}
