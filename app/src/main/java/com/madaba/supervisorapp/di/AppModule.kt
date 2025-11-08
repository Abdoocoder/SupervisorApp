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
                // Add areaId column to workers table
                database.execSQL("ALTER TABLE workers ADD COLUMN areaId TEXT NOT NULL DEFAULT ''")
                // Note: AttendanceEntity changes (hoursWorked -> regularDays, etc.) 
                // will be handled by Room's fallback migration (drop and recreate)
            }
        }
        
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "supervisor_app_db"
        )
        .addMigrations(migration1to2)
        .fallbackToDestructiveMigration() // For development - remove in production
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