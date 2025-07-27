package com.sogo.golf.msl.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sogo.golf.msl.data.local.database.entities.MslGolferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MslGolferDao {

    @Query("SELECT * FROM golfer LIMIT 1")
    fun getCurrentGolfer(): Flow<MslGolferEntity?>

    @Query("SELECT * FROM golfer WHERE golfLinkNo = :golfLinkNo")
    suspend fun getGolferByGolfLinkNo(golfLinkNo: String): MslGolferEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGolfer(golfer: MslGolferEntity)

    @Update
    suspend fun updateGolfer(golfer: MslGolferEntity)

    @Query("DELETE FROM golfer")
    suspend fun clearGolfer()

    @Query("SELECT COUNT(*) FROM golfer")
    suspend fun getGolferCount(): Int
}