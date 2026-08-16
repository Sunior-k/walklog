package com.river.walklog.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.river.walklog.core.database.entity.RewardRedemptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardRedemptionDao {

    @Insert
    suspend fun insert(entity: RewardRedemptionEntity): Long

    @Query("SELECT * FROM reward_redemptions ORDER BY redeemedAtEpochMillis DESC")
    fun observeAll(): Flow<List<RewardRedemptionEntity>>

    @Query("SELECT * FROM reward_redemptions WHERE rewardId = :rewardId ORDER BY redeemedAtEpochMillis DESC")
    fun observeByRewardId(rewardId: String): Flow<List<RewardRedemptionEntity>>

    @Query("SELECT * FROM reward_redemptions")
    suspend fun getAllOnce(): List<RewardRedemptionEntity>

    @Query("SELECT * FROM reward_redemptions WHERE remoteId IS NULL")
    suspend fun getUnsyncedOnce(): List<RewardRedemptionEntity>

    @Query("UPDATE reward_redemptions SET remoteId = :remoteId WHERE id = :id")
    suspend fun setRemoteId(id: Long, remoteId: String)
}
