// FILE: app/src/main/java/com/madaba/supervisorapp/data/source/repository/AttendanceRepository.kt
package com.madaba.supervisorapp.data.source.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.madaba.supervisorapp.data.models.Area
import com.madaba.supervisorapp.data.models.Supervisor
import com.madaba.supervisorapp.data.models.UserRole
import com.madaba.supervisorapp.data.models.Worker
import com.madaba.supervisorapp.data.source.local.AreaDao
import com.madaba.supervisorapp.data.source.local.AttendanceDao
import com.madaba.supervisorapp.data.source.local.AttendanceEntity
import com.madaba.supervisorapp.data.source.local.SupervisorDao
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
    private val attendanceDao: AttendanceDao,
    private val supervisorDao: SupervisorDao,
    private val areaDao: AreaDao
) {

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    // Get current user role
    suspend fun getCurrentUserRole(): UserRole? {
        val userId = currentUserId ?: return null
        val supervisor = supervisorDao.getSupervisorById(userId)
        return supervisor?.role
    }

    // Get current supervisor with areas
    suspend fun getCurrentSupervisor(): Supervisor? {
        val userId = currentUserId ?: return null
        val entity = supervisorDao.getSupervisorById(userId)
        return entity?.toModel()
    }

    // Workers flow - will be created dynamically based on user role
    suspend fun getWorkersFlow(): Flow<List<Worker>> {
        val userId = currentUserId ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        val supervisor = supervisorDao.getSupervisorById(userId)
        
        return if (supervisor?.role == UserRole.Admin) {
            // Admin sees all workers
            workerDao.getAllWorkers().map { entities -> entities.map { it.toModel() } }
        } else {
            // Supervisor sees only workers in their areas
            val areaIds = supervisor?.areaIds ?: emptyList()
            if (areaIds.isNotEmpty()) {
                workerDao.getWorkersForSupervisorInAreas(userId, areaIds)
                    .map { entities -> entities.map { it.toModel() } }
            } else {
                kotlinx.coroutines.flow.flowOf(emptyList())
            }
        }
    }
    
    // Legacy workers flow for backward compatibility
    val workers: Flow<List<Worker>> = kotlinx.coroutines.flow.flowOf(emptyList())

    val supervisors: Flow<List<Supervisor>> = supervisorDao.getAllSupervisors()
        .map { entities -> entities.map { it.toModel() } }

    val areas: Flow<List<Area>> = areaDao.getAllAreas()
        .map { entities -> entities.map { it.toModel() } }

    val unsyncedAttendances: Flow<List<AttendanceEntity>> = attendanceDao.getUnsyncedAttendancesFlow()

    suspend fun refreshWorkers(): RepositoryResult<Unit> {
        val userId = currentUserId ?: return RepositoryResult.Error("User not authenticated")
        val userRole = getCurrentUserRole()
        
        return try {
            val snapshot = if (userRole == UserRole.Admin) {
                // Admin gets all workers
                firestore.collection("workers").get().await()
            } else {
                // Supervisor gets workers in their areas
                val supervisor = supervisorDao.getSupervisorById(userId)
                val areaIds = supervisor?.areaIds ?: emptyList()
                if (areaIds.isEmpty()) {
                    return RepositoryResult.Success(Unit)
                }
                // Get workers by areaIds
                firestore.collection("workers")
                    .whereIn("areaId", areaIds)
                    .get()
                    .await()
            }
            val workers = snapshot.toObjects(Worker::class.java)
            workerDao.insertAll(workers.map { it.toEntity() })
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to refresh workers: ${e.message}", e)
        }
    }

    suspend fun saveAttendance(attendance: AttendanceEntity): RepositoryResult<Long> {
        val supervisorId = currentUserId ?: return RepositoryResult.Error("User not authenticated")
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
        val supervisorId = currentUserId ?: return RepositoryResult.Error("User not authenticated")
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
            // Calculate effective overtime days based on type
            val effectiveOvertimeDays = calculateEffectiveOvertimeDays(
                attendance.overtimeDays,
                attendance.overtimeType
            )
            
            val attendanceMap = mapOf(
                "workerId" to attendance.workerId,
                "date" to attendance.date,
                "status" to attendance.status,
                "regularDays" to attendance.regularDays,
                "overtimeDays" to attendance.overtimeDays,
                "overtimeType" to attendance.overtimeType,
                "effectiveOvertimeDays" to effectiveOvertimeDays,
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

    /**
     * Calculate effective overtime days based on type:
     * - normal: each day = 0.5 day
     * - holiday: each day = 1.0 day
     */
    fun calculateEffectiveOvertimeDays(days: Int, type: String): Double {
        return when (type) {
            "holiday" -> days.toDouble() // Full day
            "normal" -> days * 0.5 // Half day
            else -> days * 0.5
        }
    }

    // ========== CRUD Operations for Workers ==========
    suspend fun createWorker(worker: Worker): RepositoryResult<String> {
        val userId = currentUserId ?: return RepositoryResult.Error("User not authenticated")
        val userRole = getCurrentUserRole() ?: return RepositoryResult.Error("User role not found")
        
        if (userRole != UserRole.Admin) {
            return RepositoryResult.Error("Only admins can create workers")
        }
        
        return try {
            val workerId = worker.id.ifBlank { firestore.collection("workers").document().id }
            val workerWithId = worker.copy(id = workerId)
            
            // Save to Firestore
            firestore.collection("workers")
                .document(workerId)
                .set(workerWithId)
                .await()
            
            // Save locally
            workerDao.insertWorker(workerWithId.toEntity())
            
            RepositoryResult.Success(workerId)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to create worker: ${e.message}", e)
        }
    }

    suspend fun updateWorker(worker: Worker): RepositoryResult<Unit> {
        val userId = currentUserId ?: return RepositoryResult.Error("User not authenticated")
        val userRole = getCurrentUserRole() ?: return RepositoryResult.Error("User role not found")
        
        if (userRole != UserRole.Admin) {
            return RepositoryResult.Error("Only admins can update workers")
        }
        
        return try {
            // Update Firestore
            firestore.collection("workers")
                .document(worker.id)
                .set(worker)
                .await()
            
            // Update locally
            workerDao.insertWorker(worker.toEntity())
            
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to update worker: ${e.message}", e)
        }
    }

    suspend fun deleteWorker(workerId: String): RepositoryResult<Unit> {
        val userId = currentUserId ?: return RepositoryResult.Error("User not authenticated")
        val userRole = getCurrentUserRole() ?: return RepositoryResult.Error("User role not found")
        
        if (userRole != UserRole.Admin) {
            return RepositoryResult.Error("Only admins can delete workers")
        }
        
        return try {
            // Delete from Firestore
            firestore.collection("workers")
                .document(workerId)
                .delete()
                .await()
            
            // Delete locally
            workerDao.deleteWorker(workerId)
            
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to delete worker: ${e.message}", e)
        }
    }

    // ========== CRUD Operations for Supervisors ==========
    suspend fun createSupervisor(supervisor: Supervisor): RepositoryResult<String> {
        val userId = currentUserId ?: return RepositoryResult.Error("User not authenticated")
        val userRole = getCurrentUserRole() ?: return RepositoryResult.Error("User role not found")
        
        if (userRole != UserRole.Admin) {
            return RepositoryResult.Error("Only admins can create supervisors")
        }
        
        return try {
            val supervisorId = supervisor.id.ifBlank { firestore.collection("supervisors").document().id }
            val supervisorWithId = supervisor.copy(id = supervisorId)
            
            // Save to Firestore
            firestore.collection("supervisors")
                .document(supervisorId)
                .set(supervisorWithId)
                .await()
            
            // Save locally
            supervisorDao.insertSupervisor(supervisorWithId.toEntity())
            
            RepositoryResult.Success(supervisorId)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to create supervisor: ${e.message}", e)
        }
    }

    suspend fun updateSupervisor(supervisor: Supervisor): RepositoryResult<Unit> {
        val userId = currentUserId ?: return RepositoryResult.Error("User not authenticated")
        val userRole = getCurrentUserRole() ?: return RepositoryResult.Error("User role not found")
        
        if (userRole != UserRole.Admin) {
            return RepositoryResult.Error("Only admins can update supervisors")
        }
        
        return try {
            // Update Firestore
            firestore.collection("supervisors")
                .document(supervisor.id)
                .set(supervisor)
                .await()
            
            // Update locally
            supervisorDao.insertSupervisor(supervisor.toEntity())
            
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to update supervisor: ${e.message}", e)
        }
    }

    suspend fun deleteSupervisor(supervisorId: String): RepositoryResult<Unit> {
        val userId = currentUserId ?: return RepositoryResult.Error("User not authenticated")
        val userRole = getCurrentUserRole() ?: return RepositoryResult.Error("User role not found")
        
        if (userRole != UserRole.Admin) {
            return RepositoryResult.Error("Only admins can delete supervisors")
        }
        
        return try {
            // Delete from Firestore
            firestore.collection("supervisors")
                .document(supervisorId)
                .delete()
                .await()
            
            // Delete locally
            supervisorDao.deleteSupervisor(supervisorId)
            
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to delete supervisor: ${e.message}", e)
        }
    }

    // ========== CRUD Operations for Areas ==========
    suspend fun createArea(area: Area): RepositoryResult<String> {
        val userId = currentUserId ?: return RepositoryResult.Error("User not authenticated")
        val userRole = getCurrentUserRole() ?: return RepositoryResult.Error("User role not found")
        
        if (userRole != UserRole.Admin) {
            return RepositoryResult.Error("Only admins can create areas")
        }
        
        return try {
            val areaId = area.id.ifBlank { firestore.collection("areas").document().id }
            val areaWithId = area.copy(id = areaId)
            
            // Save to Firestore
            firestore.collection("areas")
                .document(areaId)
                .set(areaWithId)
                .await()
            
            // Save locally
            areaDao.insertArea(areaWithId.toEntity())
            
            RepositoryResult.Success(areaId)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to create area: ${e.message}", e)
        }
    }

    suspend fun updateArea(area: Area): RepositoryResult<Unit> {
        val userId = currentUserId ?: return RepositoryResult.Error("User not authenticated")
        val userRole = getCurrentUserRole() ?: return RepositoryResult.Error("User role not found")
        
        if (userRole != UserRole.Admin) {
            return RepositoryResult.Error("Only admins can update areas")
        }
        
        return try {
            // Update Firestore
            firestore.collection("areas")
                .document(area.id)
                .set(area)
                .await()
            
            // Update locally
            areaDao.insertArea(area.toEntity())
            
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to update area: ${e.message}", e)
        }
    }

    suspend fun deleteArea(areaId: String): RepositoryResult<Unit> {
        val userId = currentUserId ?: return RepositoryResult.Error("User not authenticated")
        val userRole = getCurrentUserRole() ?: return RepositoryResult.Error("User role not found")
        
        if (userRole != UserRole.Admin) {
            return RepositoryResult.Error("Only admins can delete areas")
        }
        
        return try {
            // Delete from Firestore
            firestore.collection("areas")
                .document(areaId)
                .delete()
                .await()
            
            // Delete locally
            areaDao.deleteArea(areaId)
            
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to delete area: ${e.message}", e)
        }
    }

    // Refresh data from Firestore
    suspend fun refreshSupervisors(): RepositoryResult<Unit> {
        return try {
            val snapshot = firestore.collection("supervisors").get().await()
            val supervisors = snapshot.toObjects(Supervisor::class.java)
            supervisorDao.insertAll(supervisors.map { it.toEntity() })
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to refresh supervisors: ${e.message}", e)
        }
    }

    suspend fun refreshAreas(): RepositoryResult<Unit> {
        return try {
            val snapshot = firestore.collection("areas").get().await()
            val areas = snapshot.toObjects(Area::class.java)
            areaDao.insertAll(areas.map { it.toEntity() })
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to refresh areas: ${e.message}", e)
        }
    }
}