package com.madaba.supervisorapp.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AreaDao {
    @Query("SELECT * FROM areas")
    fun getAllAreas(): Flow<List<AreaEntity>>

    @Query("SELECT * FROM areas WHERE id = :id")
    suspend fun getAreaById(id: String): AreaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArea(area: AreaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(areas: List<AreaEntity>)

    @Query("DELETE FROM areas WHERE id = :id")
    suspend fun deleteArea(id: String)

    @Query("DELETE FROM areas")
    suspend fun clearAll()
}

