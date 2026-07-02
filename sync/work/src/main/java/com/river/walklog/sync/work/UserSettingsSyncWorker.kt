package com.river.walklog.sync.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.data.sync.Syncable
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class UserSettingsSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val userSettingsRepository: UserSettingsRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val syncable = userSettingsRepository as? Syncable ?: return Result.failure()
        return if (syncable.sync()) Result.success() else Result.retry()
    }

    companion object {
        const val WORK_NAME = "UserSettingsSyncWork"
    }
}
