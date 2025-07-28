package com.sogo.golf.msl.domain.usecase.fees

import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.mongodb.Fee
import com.sogo.golf.msl.domain.repository.FeeLocalDbRepository
import javax.inject.Inject

class FetchAndSaveFeesUseCase @Inject constructor(
    private val feeRepository: FeeLocalDbRepository
) {
    suspend operator fun invoke(): NetworkResult<List<Fee>> {
        return feeRepository.fetchAndSaveFees()
    }
}