package com.sogo.golf.msl.domain.repository.remote

import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.mongodb.Fee

interface SogoMongoRepository {
    suspend fun getFees(): NetworkResult<List<Fee>>
}