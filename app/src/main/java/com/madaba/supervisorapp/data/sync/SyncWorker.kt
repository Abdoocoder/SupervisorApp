package com.madaba.supervisorapp.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.madaba.supervisorapp.data.source.repository.AttendanceRepository
import com.madaba.supervisorapp.data.source.repository.RepositoryResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: AttendanceRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Sync workers
            val workersResult = repository.refreshWorkers()
            if (workersResult is RepositoryResult.Error) {
                return Result.retry()
            }

            // Sync unsynced attendances
            val attendanceResult = repository.syncUnsyncedAttendances()
            when (attendanceResult) {
                is RepositoryResult.Success -> {
                    Result.success()
                }
                is RepositoryResult.Error -> {
                    // Retry if there was an error
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

