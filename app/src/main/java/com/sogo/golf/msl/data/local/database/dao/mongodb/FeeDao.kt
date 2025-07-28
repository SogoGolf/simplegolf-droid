package com.sogo.golf.msl.data.local.database.dao.mongodb


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sogo.golf.msl.data.local.database.entities.mongodb.FeeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeeDao {

    @Query("SELECT * FROM fees ORDER BY numberHoles ASC, cost ASC")
    fun getAllFees(): Flow<List<FeeEntity>>

    @Query("SELECT * FROM fees WHERE entityId = :entityId ORDER BY numberHoles ASC, cost ASC")
    fun getFeesByEntityId(entityId: String): Flow<List<FeeEntity>>

    @Query("SELECT * FROM fees WHERE numberHoles = :numberHoles ORDER BY cost ASC")
    fun getFeesByNumberHoles(numberHoles: Int): Flow<List<FeeEntity>>

    @Query("SELECT * FROM fees WHERE id = :feeId")
    suspend fun getFeeById(feeId: String): FeeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFee(fee: FeeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFees(fees: List<FeeEntity>)

    @Query("DELETE FROM fees")
    suspend fun clearAllFees()

    @Transaction
    suspend fun replaceFees(fees: List<FeeEntity>) {
        clearAllFees()
        insertFees(fees)
    }

    @Query("SELECT COUNT(*) FROM fees")
    suspend fun getFeeCount(): Int

    // Get fees with specific criteria
    @Query("SELECT * FROM fees WHERE isWaived = 0 ORDER BY numberHoles ASC, cost ASC")
    fun getActiveFees(): Flow<List<FeeEntity>>

    @Query("SELECT * FROM fees WHERE isWaived = 1 ORDER BY numberHoles ASC")
    fun getWaivedFees(): Flow<List<FeeEntity>>
}
