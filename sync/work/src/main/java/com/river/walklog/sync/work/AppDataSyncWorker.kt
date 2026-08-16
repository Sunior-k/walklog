package com.river.walklog.sync.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.river.walklog.core.data.repository.PointsLedgerRepository
import com.river.walklog.core.data.repository.RewardRedemptionRepository
import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.data.sync.Syncable
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

@HiltWorker
class AppDataSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val userSettingsRepository: UserSettingsRepository,
    private val rewardRedemptionRepository: RewardRedemptionRepository,
    private val pointsLedgerRepository: PointsLedgerRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        // 서로 독립적인 동기화라 순차 실행할 이유가 없다 — 병렬로 실행해 백그라운드 작업 시간을 줄인다.
        val results = listOf(userSettingsRepository, rewardRedemptionRepository, pointsLedgerRepository)
            .mapNotNull { it as? Syncable }
            .map { async { it.sync() } }
            .map { it.await() }

        if (results.all { it }) Result.success() else Result.retry()
    }

    companion object {
        const val WORK_NAME = "AppDataSyncWork"
    }
}
