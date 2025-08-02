package com.sogo.golf.msl.domain.usecase.transaction

import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.mongodb.SogoGolfer
import com.sogo.golf.msl.domain.repository.remote.SogoMongoRepository
import javax.inject.Inject

class CreateTransactionUseCase @Inject constructor(
    private val sogoMongoRepository: SogoMongoRepository
) {
    suspend operator fun invoke(
        tokens: Int,
        entityIdVal: String?,
        transId: String,
        sogoGolfer: SogoGolfer,
        transactionTypeVal: String,
        debitCreditTypeVal: String,
        commentVal: String,
        statusVal: String
    ): Result<Unit> {
        return try {
            val result = sogoMongoRepository.createTransaction(
                entityId = entityIdVal,
                transactionId = transId,
                golferId = sogoGolfer.id,
                golferEmail = sogoGolfer.email,
                amount = tokens,
                transactionType = transactionTypeVal.lowercase(),
                debitCreditType = debitCreditTypeVal.lowercase(),
                comment = commentVal,
                status = statusVal
            )
            
            when (result) {
                is NetworkResult.Success -> Result.success(Unit)
                is NetworkResult.Error -> Result.failure(Exception(result.error.toUserMessage()))
                else -> Result.failure(Exception("Unexpected result"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
