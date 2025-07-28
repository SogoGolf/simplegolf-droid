package com.sogo.golf.msl.domain.repository

import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.mongodb.Fee
import kotlinx.coroutines.flow.Flow

interface FeeLocalDbRepository {
    fun getAllFees(): Flow<List<Fee>>
    fun getFeesByEntityName(entityName: String): Flow<List<Fee>>
    fun getFeesByNumberHoles(numberHoles: Int): Flow<List<Fee>>
    fun getActiveFees(): Flow<List<Fee>>
    fun getWaivedFees(): Flow<List<Fee>>
    suspend fun getFeeById(feeId: String): Fee?
    suspend fun fetchAndSaveFees(): NetworkResult<List<Fee>>
    suspend fun clearAllFees()
    suspend fun hasFees(): Boolean
}