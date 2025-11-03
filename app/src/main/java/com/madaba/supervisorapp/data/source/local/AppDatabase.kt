// FILE: app/src/main/java/com/madaba/supervisorapp/data/source/local/AppDatabase.kt
package com.madaba.supervisorapp.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WorkerEntity::class, AttendanceEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workerDao(): WorkerDao
    abstract fun attendanceDao(): AttendanceDao
}