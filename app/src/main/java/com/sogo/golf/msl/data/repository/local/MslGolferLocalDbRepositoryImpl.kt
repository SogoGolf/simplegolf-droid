// app/src/main/java/com/sogo/golf/msl/data/repository/local/MslGolferLocalDbRepositoryImpl.kt
package com.sogo.golf.msl.data.repository.local

import android.util.Log
import com.sogo.golf.msl.data.local.database.dao.MslGolferDao
import com.sogo.golf.msl.data.local.database.entities.MslGolferEntity
import com.sogo.golf.msl.domain.model.msl.MslGolfer
import com.sogo.golf.msl.domain.repository.MslGolferLocalDbRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MslGolferLocalDbRepositoryImpl @Inject constructor(
    private val golferDao: MslGolferDao
) : MslGolferLocalDbRepository {

    companion object {
        private const val TAG = "MslGolferLocalDbRepo"
    }

    override fun getCurrentGolfer(): Flow<MslGolfer?> {
        Log.d(TAG, "getCurrentGolfer called")
        return golferDao.getCurrentGolfer()
            .map { entity ->
                Log.d(TAG, "getCurrentGolfer mapped: entity = $entity")
                entity?.toDomainModel()
            }
    }

    override suspend fun getGolferByGolfLinkNo(golfLinkNo: String): MslGolfer? {
        Log.d(TAG, "getGolferByGolfLinkNo called with: $golfLinkNo")
        val entity = golferDao.getGolferByGolfLinkNo(golfLinkNo)
        Log.d(TAG, "Found entity: $entity")
        return entity?.toDomainModel()
    }

    override suspend fun saveGolfer(golfer: MslGolfer) {
        Log.d(TAG, "saveGolfer called for: ${golfer.firstName} ${golfer.surname} (${golfer.golfLinkNo})")

        // Check current count before replace
        val countBefore = golferDao.getGolferCount()
        Log.d(TAG, "Golfers in database before replace: $countBefore")

        // SINGLE RECORD PATTERN: Clear existing golfer and insert new one
        golferDao.clearGolfer()
        Log.d(TAG, "Cleared all existing golfers")

        val entity = MslGolferEntity.fromDomainModel(golfer)
        Log.d(TAG, "Created entity: $entity")

        golferDao.insertGolfer(entity)
        Log.d(TAG, "Golfer inserted to database")

        // Verify the single-record pattern worked
        val countAfter = golferDao.getGolferCount()
        Log.d(TAG, "Golfers in database after replace: $countAfter (should be 1)")

        val savedEntity = golferDao.getGolferByGolfLinkNo(golfer.golfLinkNo)
        Log.d(TAG, "Verification - saved entity: $savedEntity")
    }

    override suspend fun clearGolfer() {
        Log.d(TAG, "clearGolfer called")
        golferDao.clearGolfer()
        Log.d(TAG, "Golfer cleared from database")
    }

    override suspend fun hasGolfer(): Boolean {
        val count = golferDao.getGolferCount()
        Log.d(TAG, "hasGolfer: $count golfers in database")
        return count > 0
    }
}