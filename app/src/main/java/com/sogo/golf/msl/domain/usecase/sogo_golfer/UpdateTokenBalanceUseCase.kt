package com.sogo.golf.msl.domain.usecase.sogo_golfer

import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.mongodb.SogoGolfer
import com.sogo.golf.msl.domain.repository.SogoGolferLocalDbRepository
import com.sogo.golf.msl.domain.repository.remote.SogoMongoRepository
import com.revenuecat.purchases.models.StoreTransaction
import javax.inject.Inject

class UpdateTokenBalanceUseCase @Inject constructor(
    private val sogoGolferRepository: SogoGolferLocalDbRepository,
    private val sogoMongoRepository: SogoMongoRepository
) {
    suspend operator fun invoke(
        currentBalance: Int,
        storeTransaction: StoreTransaction,
        sogoGolfer: SogoGolfer?
    ): Result<SogoGolfer> {
        return try {
            val purchasedTokens = extractTokenAmountFromTransaction(storeTransaction)
            val newBalance = currentBalance + purchasedTokens
            
            val updateResult = sogoMongoRepository.updateGolferTokenBalance(
                sogoGolfer?.golfLinkNo ?: "", 
                newBalance
            )
            
            when (updateResult) {
                is NetworkResult.Success -> {
                    sogoGolferRepository.saveSogoGolfer(updateResult.data)
                    Result.success(updateResult.data)
                }
                is NetworkResult.Error -> {
                    Result.failure(Exception(updateResult.error.toUserMessage()))
                }
                else -> Result.failure(Exception("Unexpected result"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun extractTokenAmountFromTransaction(storeTransaction: StoreTransaction): Int {
        return when {
            storeTransaction.productIds.any { it.contains("5token") } -> 5
            storeTransaction.productIds.any { it.contains("10token") } -> 10
            storeTransaction.productIds.any { it.contains("20token") } -> 20
            else -> 5
        }
    }
}
