// FILE: app/src/main/java/com/madaba/supervisorapp/data/source/local/AttendanceEntity.kt
package com.madaba.supervisorapp.data.source.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val workerId: String,
    val date: Long, // Store as timestamp
    val status: String,
    val hoursWorked: Int,
    val overtimeHours: Int,
    val notes: String?,
    val photoUrl: String?,
    var synced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),
    val editedBy: String
)