/**
 * Represents a single attendance record for a worker in the local database.
 *
 * @property localId The unique identifier for the attendance record in the local database.
 * @property workerId The ID of the worker this attendance record belongs to.
 * @property date The date of the attendance record, stored as a Unix timestamp.
 * @property status The status of the attendance (e.g., "Present", "Absent", "On Leave").
 * @property hoursWorked The number of hours the worker worked on this date.
 * @property overtimeHours The number of overtime hours the worker worked.
 * @property notes Any additional notes or comments about this attendance record.
 * @property photoUrl The URL of a photo associated with this attendance record, if any.
 * @property synced A flag indicating whether this record has been synced with a remote server.
 * @property createdAt The timestamp when this record was created.
 * @property updatedAt The timestamp when this record was last updated.
 * @property editedBy The identifier of the user who last edited this record.
 */
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