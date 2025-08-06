package com.sogo.golf.msl.domain.repository

import com.sogo.golf.msl.domain.model.msl.MslGolfer
import kotlinx.coroutines.flow.Flow

interface MslGolferLocalDbRepository {
    fun getCurrentGolfer(): Flow<MslGolfer?>
    suspend fun getGolferByGolfLinkNo(golfLinkNo: String): MslGolfer?
    suspend fun saveGolfer(golfer: MslGolfer)
    suspend fun clearGolfer()
    suspend fun hasGolfer(): Boolean
    suspend fun getGolferCount(): Int
}