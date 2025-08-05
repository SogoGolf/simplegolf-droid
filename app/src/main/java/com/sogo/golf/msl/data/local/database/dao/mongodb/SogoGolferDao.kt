package com.sogo.golf.msl.data.local.database.dao.mongodb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sogo.golf.msl.data.local.database.entities.mongodb.SogoGolferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SogoGolferDao {

    @Query("SELECT * FROM sogo_golfers WHERE golfLinkNo = :golfLinkNo LIMIT 1")
    suspend fun getSogoGolferByGolfLinkNo(golfLinkNo: String): SogoGolferEntity?

    @Query("SELECT * FROM sogo_golfers WHERE golfLinkNo = :golfLinkNo LIMIT 1")
    fun getSogoGolferByGolfLinkNoFlow(golfLinkNo: String): Flow<SogoGolferEntity?>

    @Query("SELECT * FROM sogo_golfers WHERE id = :id")
    suspend fun getSogoGolferById(id: String): SogoGolferEntity?

    @Query("SELECT * FROM sogo_golfers ORDER BY lastName ASC, firstName ASC")
    fun getAllSogoGolfers(): Flow<List<SogoGolferEntity>>

    @Query("SELECT * FROM sogo_golfers WHERE isActive = 1 ORDER BY lastName ASC, firstName ASC")
    fun getActiveSogoGolfers(): Flow<List<SogoGolferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSogoGolfer(sogoGolfer: SogoGolferEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSogoGolfers(sogoGolfers: List<SogoGolferEntity>)

    @Query("DELETE FROM sogo_golfers WHERE golfLinkNo = :golfLinkNo")
    suspend fun deleteSogoGolferByGolfLinkNo(golfLinkNo: String)

    @Query("DELETE FROM sogo_golfers")
    suspend fun clearAllSogoGolfers()

    @Transaction
    suspend fun replaceSogoGolfer(sogoGolfer: SogoGolferEntity) {
        // Delete any existing golfer with the same golfLinkNo to ensure only one record per golfer
        deleteSogoGolferByGolfLinkNo(sogoGolfer.golfLinkNo)
        // Insert the new golfer data
        insertSogoGolfer(sogoGolfer)
    }

    @Query("SELECT COUNT(*) FROM sogo_golfers")
    suspend fun getSogoGolferCount(): Int

    @Query("SELECT COUNT(*) FROM sogo_golfers WHERE golfLinkNo = :golfLinkNo")
    suspend fun hasSogoGolferByGolfLinkNo(golfLinkNo: String): Int
}
