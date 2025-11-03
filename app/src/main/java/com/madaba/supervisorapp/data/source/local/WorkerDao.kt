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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(workers: List<WorkerEntity>)

    @Query("DELETE FROM workers")
    suspend fun clearAll()
}