// FILE: app/src/main/java/com/madaba/supervisorapp/data/source/local/WorkerDao.kt
package com.madaba.supervisorapp.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkerDao {
    @Query("SELECT * FROM workers WHERE supervisorId = :supervisorId")
    fun getWorkersForSupervisor(supervisorId: String): Flow<List<WorkerEntity>>

    @Query("SELECT * FROM workers WHERE supervisorId = :supervisorId AND areaId IN (:areaIds)")
    fun getWorkersForSupervisorInAreas(supervisorId: String, areaIds: List<String>): Flow<List<WorkerEntity>>

    @Query("SELECT * FROM workers WHERE areaId = :areaId")
    fun getWorkersByArea(areaId: String): Flow<List<WorkerEntity>>

    @Query("SELECT * FROM workers")
    fun getAllWorkers(): Flow<List<WorkerEntity>>

    @Query("SELECT * FROM workers WHERE id = :id")
    suspend fun getWorkerById(id: String): WorkerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: WorkerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(workers: List<WorkerEntity>)

    @Query("DELETE FROM workers WHERE id = :id")
    suspend fun deleteWorker(id: String)

    @Query("DELETE FROM workers")
    suspend fun clearAll()
}