package com.sogo.golf.msl.domain.repository

import com.sogo.golf.msl.data.network.api.CreateGolferRequestDto
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.mongodb.SogoGolfer
import kotlinx.coroutines.flow.Flow

interface SogoGolferLocalDbRepository {
    fun getSogoGolferByGolfLinkNo(golfLinkNo: String): Flow<SogoGolfer?>
    suspend fun getSogoGolferByGolfLinkNoOnce(golfLinkNo: String): SogoGolfer?
    suspend fun getSogoGolferById(id: String): SogoGolfer?
    fun getAllSogoGolfers(): Flow<List<SogoGolfer>>
    fun getActiveSogoGolfers(): Flow<List<SogoGolfer>>
    suspend fun fetchAndSaveSogoGolfer(golfLinkNo: String): NetworkResult<SogoGolfer>
    suspend fun saveSogoGolfer(sogoGolfer: SogoGolfer)
    suspend fun deleteSogoGolferByGolfLinkNo(golfLinkNo: String)
    suspend fun clearAllSogoGolfers()
    suspend fun hasSogoGolferByGolfLinkNo(golfLinkNo: String): Boolean
    suspend fun createAndSaveGolfer(request: CreateGolferRequestDto): NetworkResult<SogoGolfer>
}
