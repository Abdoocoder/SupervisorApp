// FILE: app/src/main/java/com/madaba/supervisorapp/di/AppModule.kt
package com.madaba.supervisorapp.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.madaba.supervisorapp.data.source.local.AreaDao
import com.madaba.supervisorapp.data.source.local.AppDatabase
import com.madaba.supervisorapp.data.source.local.AttendanceDao
import com.madaba.supervisorapp.data.source.local.SupervisorDao
import com.madaba.supervisorapp.data.source.local.WorkerDao
import com.madaba.supervisorapp.data.source.repository.AttendanceRepository
import com.madaba.supervisorapp.data.sync.SyncManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val migration1to2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Drop existing attendance table
                database.execSQL("DROP TABLE IF EXISTS attendance")
                
                // Create new attendance table with updated schema
                database.execSQL("""
                    CREATE TABLE attendance (
                        localId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        workerId TEXT NOT NULL,
                        date INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        regularDays INTEGER NOT NULL DEFAULT 0,
                        overtimeDays INTEGER NOT NULL DEFAULT 0,
                        overtimeType TEXT NOT NULL DEFAULT 'normal',
                        notes TEXT,
                        photoUrl TEXT,
                        synced INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0,
                        editedBy TEXT NOT NULL DEFAULT ''
                    )
                """)
                
                // Add areaId to workers table
                database.execSQL("ALTER TABLE workers ADD COLUMN areaId TEXT NOT NULL DEFAULT ''")
            }
        }
        
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "supervisor_app_db"
        )
        .addMigrations(migration1to2)
        .build()
    }

    @Provides
    @Singleton
    fun provideWorkerDao(database: AppDatabase): WorkerDao = database.workerDao()

    @Provides
    @Singleton
    fun provideAttendanceDao(database: AppDatabase): AttendanceDao = database.attendanceDao()

    @Provides
    @Singleton
    fun provideSupervisorDao(database: AppDatabase): SupervisorDao = database.supervisorDao()

    @Provides
    @Singleton
    fun provideAreaDao(database: AppDatabase): AreaDao = database.areaDao()

    @Provides
    @Singleton
    fun provideAttendanceRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        workerDao: WorkerDao,
        attendanceDao: AttendanceDao,
        supervisorDao: SupervisorDao,
        areaDao: AreaDao
    ): AttendanceRepository {
        return AttendanceRepository(firestore, auth, workerDao, attendanceDao, supervisorDao, areaDao)
    }

}