package com.river.walklog.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.river.walklog.core.model.RewardRedemption

@Entity(tableName = "reward_redemptions")
data class RewardRedemptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rewardId: String,
    val pointsCost: Int,
    val redeemedAtEpochMillis: Long,
    val couponCode: String?,
    /** Firestore 문서 ID. 클라우드 업로드 성공 후 채워짐 — null이면 아직 백업되지 않은 로컬 전용 기록. */
    val remoteId: String? = null,
)

fun RewardRedemptionEntity.asExternalModel(): RewardRedemption = RewardRedemption(
    id = id,
    rewardId = rewardId,
    pointsCost = pointsCost,
    redeemedAtEpochMillis = redeemedAtEpochMillis,
    couponCode = couponCode,
)
