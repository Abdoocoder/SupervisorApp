// FILE: app/src/main/java/com/madaba/supervisorapp/data/source/repository/AttendanceRepository.kt
package com.madaba.supervisorapp.data.source.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.madaba.supervisorapp.data.models.Worker
import com.madaba.supervisorapp.data.source.local.AttendanceDao
import com.madaba.supervisorapp.data.source.local.AttendanceEntity
import com.madaba.supervisorapp.data.source.local.WorkerDao
import com.madaba.supervisorapp.data.source.local.toEntity
import com.madaba.supervisorapp.data.source.local.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

sealed class RepositoryResult<out T> {
    data class Success<T>(val data: T) : RepositoryResult<T>()
    data class Error(val message: String, val exception: Throwable? = null) : RepositoryResult<Nothing>()
}

@Singleton
class AttendanceRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val workerDao: WorkerDao,
    private val attendanceDao: AttendanceDao
) {

    private val currentSupervisorId: String?
        get() = auth.currentUser?.uid

    val workers: Flow<List<Worker>> = workerDao.getWorkersForSupervisor(currentSupervisorId ?: "")
        .map { entities -> entities.map { it.toModel() } }

    val unsyncedAttendances: Flow<List<AttendanceEntity>> = attendanceDao.getUnsyncedAttendancesFlow()

    suspend fun refreshWorkers(): RepositoryResult<Unit> {
        val supervisorId = currentSupervisorId ?: return RepositoryResult.Error("User not authenticated")
        return try {
            val snapshot = firestore.collection("workers")
                .whereEqualTo("supervisorId", supervisorId)
                .get()
                .await()
            val workers = snapshot.toObjects(Worker::class.java)
            workerDao.insertAll(workers.map { it.toEntity() })
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to refresh workers: ${e.message}", e)
        }
    }

    suspend fun saveAttendance(attendance: AttendanceEntity): RepositoryResult<Long> {
        val supervisorId = currentSupervisorId ?: return RepositoryResult.Error("User not authenticated")
        return try {
            // Save locally first and get the generated localId
            val attendanceWithSupervisor = attendance.copy(editedBy = supervisorId)
            val localId = attendanceDao.insertAttendance(attendanceWithSupervisor)
            val savedAttendance = attendanceWithSupervisor.copy(localId = localId)
            
            // Try to sync immediately if online
            val syncResult = syncAttendanceToFirestore(savedAttendance)
            if (syncResult is RepositoryResult.Success) {
                attendanceDao.markSynced(localId)
            }
            
            RepositoryResult.Success(localId)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to save attendance: ${e.message}", e)
        }
    }

    suspend fun getAttendanceForWorker(workerId: String): Flow<List<AttendanceEntity>> {
        return attendanceDao.getAttendanceForWorker(workerId)
    }

    suspend fun syncUnsyncedAttendances(): RepositoryResult<Int> {
        val supervisorId = currentSupervisorId ?: return RepositoryResult.Error("User not authenticated")
        return try {
            val unsynced = attendanceDao.getUnsyncedAttendancesList()
            var syncedCount = 0
            
            unsynced.forEach { attendance ->
                val result = syncAttendanceToFirestore(attendance)
                if (result is RepositoryResult.Success) {
                    attendanceDao.markSynced(attendance.localId)
                    syncedCount++
                }
            }
            
            RepositoryResult.Success(syncedCount)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to sync attendances: ${e.message}", e)
        }
    }

    private suspend fun syncAttendanceToFirestore(attendance: AttendanceEntity): RepositoryResult<String> {
        return try {
            val attendanceMap = mapOf(
                "workerId" to attendance.workerId,
                "date" to attendance.date,
                "status" to attendance.status,
                "hoursWorked" to attendance.hoursWorked,
                "overtimeHours" to attendance.overtimeHours,
                "notes" to (attendance.notes ?: ""),
                "photoUrl" to (attendance.photoUrl ?: ""),
                "createdAt" to attendance.createdAt,
                "updatedAt" to attendance.updatedAt,
                "editedBy" to attendance.editedBy
            )
            
            val docRef = firestore.collection("attendance")
                .add(attendanceMap)
                .await()
            
            RepositoryResult.Success(docRef.id)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to sync to Firestore: ${e.message}", e)
        }
    }
}