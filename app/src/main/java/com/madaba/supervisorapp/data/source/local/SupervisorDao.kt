package com.madaba.supervisorapp.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SupervisorDao {
    @Query("SELECT * FROM supervisors")
    fun getAllSupervisors(): Flow<List<SupervisorEntity>>

    @Query("SELECT * FROM supervisors WHERE id = :id")
    suspend fun getSupervisorById(id: String): SupervisorEntity?

    @Query("SELECT * FROM supervisors WHERE role = 'Admin'")
    fun getAdmins(): Flow<List<SupervisorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupervisor(supervisor: SupervisorEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(supervisors: List<SupervisorEntity>)

    @Query("DELETE FROM supervisors WHERE id = :id")
    suspend fun deleteSupervisor(id: String)

    @Query("DELETE FROM supervisors")
    suspend fun clearAll()
}

