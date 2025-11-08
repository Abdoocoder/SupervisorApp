// FILE: app/src/main/java/com/madaba/supervisorapp/data/source/local/AppDatabase.kt
package com.madaba.supervisorapp.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        WorkerEntity::class,
        AttendanceEntity::class,
        SupervisorEntity::class,
        AreaEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workerDao(): WorkerDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun supervisorDao(): SupervisorDao
    abstract fun areaDao(): AreaDao
}