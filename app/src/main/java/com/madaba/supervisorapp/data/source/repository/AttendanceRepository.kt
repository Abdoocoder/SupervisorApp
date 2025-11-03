// FILE: app/src/main/java/com/madaba/supervisorapp/data/source/repository/AttendanceRepository.kt
package com.madaba.supervisorapp.data.source.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.madaba.supervisorapp.data.models.Worker
import com.madaba.supervisorapp.data.source.local.AttendanceDao
import com.madaba.supervisorapp.data.source.local.WorkerDao
import com.madaba.supervisorapp.data.source.local.toEntity
import com.madaba.supervisorapp.data.source.local.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

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

    suspend fun refreshWorkers() {
        val supervisorId = currentSupervisorId ?: return
        try {
            val snapshot = firestore.collection("workers")
                .whereEqualTo("supervisorId", supervisorId)
                .get()
                .await()
            val workers = snapshot.toObjects(Worker::class.java)
            workerDao.insertAll(workers.map { it.toEntity() })
        } catch (e: Exception) {
            // Handle error, maybe log it
            e.printStackTrace()
        }
    }
}