package com.river.walklog.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.river.walklog.core.database.entity.DailyStepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStepDao {

    @Upsert
    suspend fun upsert(entity: DailyStepEntity)

    @Query("SELECT * FROM daily_steps WHERE dateEpochDay = :dateEpochDay")
    fun observeForDay(dateEpochDay: Long): Flow<DailyStepEntity?>

    @Query("SELECT * FROM daily_steps WHERE dateEpochDay = :dateEpochDay")
    suspend fun getForDay(dateEpochDay: Long): DailyStepEntity?

    /**
     * 일간 집계 주간 범위 조회 (월~일 7일치)
     */
    @Query(
        """
        SELECT * FROM daily_steps
        WHERE dateEpochDay BETWEEN :fromEpochDay AND :toEpochDay
        ORDER BY dateEpochDay ASC
        """,
    )
    fun observeForRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<DailyStepEntity>>
}
