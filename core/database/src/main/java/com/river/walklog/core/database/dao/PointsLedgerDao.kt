package com.river.walklog.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.river.walklog.core.database.entity.PointsLedgerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PointsLedgerDao {

    @Insert
    suspend fun insert(entity: PointsLedgerEntity): Long

    @Query("SELECT * FROM points_ledger ORDER BY createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<PointsLedgerEntity>>

    @Query("SELECT * FROM points_ledger")
    suspend fun getAllOnce(): List<PointsLedgerEntity>

    @Query("SELECT * FROM points_ledger WHERE remoteId IS NULL")
    suspend fun getUnsyncedOnce(): List<PointsLedgerEntity>

    @Query("UPDATE points_ledger SET remoteId = :remoteId WHERE id = :id")
    suspend fun setRemoteId(id: Long, remoteId: String)
}
