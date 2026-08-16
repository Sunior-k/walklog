package com.river.walklog.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.river.walklog.core.model.PointsLedgerEntry

@Entity(tableName = "points_ledger")
data class PointsLedgerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deltaPoints: Int,
    val reason: String,
    val createdAtEpochMillis: Long,
    /** Firestore 문서 ID. 클라우드 업로드 성공 후 채워짐 — null이면 아직 백업되지 않은 로컬 전용 기록. */
    val remoteId: String? = null,
)

fun PointsLedgerEntity.asExternalModel(): PointsLedgerEntry = PointsLedgerEntry(
    id = id,
    deltaPoints = deltaPoints,
    reason = reason,
    createdAtEpochMillis = createdAtEpochMillis,
)
