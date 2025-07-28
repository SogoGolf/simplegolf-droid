package com.sogo.golf.msl.domain.usecase.fees

import com.sogo.golf.msl.domain.model.mongodb.Fee
import com.sogo.golf.msl.domain.repository.FeeLocalDbRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFeesUseCase @Inject constructor(
    private val feeRepository: FeeLocalDbRepository
) {
    operator fun invoke(): Flow<List<Fee>> {
        return feeRepository.getAllFees()
    }

    fun getActiveFees(): Flow<List<Fee>> {
        return feeRepository.getActiveFees()
    }

    fun getFeesByNumberHoles(numberHoles: Int): Flow<List<Fee>> {
        return feeRepository.getFeesByNumberHoles(numberHoles)
    }

    fun getFeesByEntityId(entityId: String): Flow<List<Fee>> {
        return feeRepository.getFeesByEntityId(entityId)
    }
}