// FILE: app/src/main/java/com/madaba/supervisorapp/data/source/local/AttendanceDao.kt
package com.madaba.supervisorapp.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity)

    @Query("SELECT * FROM attendance WHERE synced = 0")
    fun getUnsyncedAttendancesFlow(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE synced = 0")
    suspend fun getUnsyncedAttendancesList(): List<AttendanceEntity>

    @Query("UPDATE attendance SET synced = 1 WHERE localId = :localId")
    suspend fun markSynced(localId: Long)

    @Query("SELECT * FROM attendance")
    fun getAllAttendanceRecords(): Flow<List<AttendanceEntity>>
}