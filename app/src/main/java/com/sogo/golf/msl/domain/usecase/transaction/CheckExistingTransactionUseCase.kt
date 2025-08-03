package com.sogo.golf.msl.domain.usecase.transaction

import com.sogo.golf.msl.data.local.database.dao.TransactionDao
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.repository.remote.SogoMongoRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class CheckExistingTransactionUseCase @Inject constructor(
    private val transactionDao: TransactionDao,
    private val sogoMongoRepository: SogoMongoRepository
) {
    suspend operator fun invoke(
        golferId: String,
        mainCompetitionId: Int
    ): Result<Boolean> {
        return try {
            val today = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            val startOfDay = today.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfDay = today.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val localTransactionCount = transactionDao.countDebitTransactionsByGolferDateCompetition(
                golferId = golferId,
                startOfDay = startOfDay,
                endOfDay = endOfDay,
                mainCompetitionId = mainCompetitionId
            )

            if (localTransactionCount > 0) {
                Result.success(true)
            } else {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val todayString = dateFormat.format(Date())

                when (val result = sogoMongoRepository.getTransactionsByGolferDateCompetition(
                    golferId = golferId,
                    date = todayString,
                    mainCompetitionId = mainCompetitionId
                )) {
                    is NetworkResult.Success -> {
                        val hasExistingTransaction = result.data.isNotEmpty()
                        Result.success(hasExistingTransaction)
                    }
                    is NetworkResult.Error -> {
                        Result.failure(Exception("Failed to check existing transactions: ${result.error}"))
                    }
                    is NetworkResult.Loading -> {
                        Result.failure(Exception("Unexpected loading state"))
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
