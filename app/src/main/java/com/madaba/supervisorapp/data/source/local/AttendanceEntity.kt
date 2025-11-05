package com.madaba.supervisorapp.data.source.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single attendance record for a worker in the local database.
 * This class is used as a Room entity to store attendance data on the device.
 *
 * @property localId The unique, auto-generated primary key for the attendance record in the local database.
 * @property workerId The identifier for the worker this attendance record belongs to.
 * @property date The date of the attendance, stored as a Unix timestamp (milliseconds).
 * @property status The attendance status for the worker on the given date (e.g., "Present", "Absent").
 * @property hoursWorked The number of regular hours worked by the employee.
 * @property overtimeHours The number of overtime hours worked.
 * @property notes Optional text containing any additional notes or comments about the attendance.
 * @property photoUrl An optional URL or local path to a photo associated with this record.
 * @property synced A boolean flag indicating whether this record has been successfully synchronized with the remote server. Defaults to `false`.
 * @property createdAt The timestamp (in milliseconds) when this record was created locally.
 * @property updatedAt The timestamp (in milliseconds) when this record was last modified.
 * @property editedBy The identifier of the user (e.g., supervisor) who created or last edited this record.
 */
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