// FILE: app/src/main/java/com/madaba/supervisorapp/di/AppModule.kt
package com.madaba.supervisorapp.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.madaba.supervisorapp.data.source.local.AppDatabase
import com.madaba.supervisorapp.data.source.local.AttendanceDao
import com.madaba.supervisorapp.data.source.local.WorkerDao
import com.madaba.supervisorapp.data.source.repository.AttendanceRepository
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
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "supervisor_app_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideWorkerDao(database: AppDatabase): WorkerDao = database.workerDao()

    @Provides
    @Singleton
    fun provideAttendanceDao(database: AppDatabase): AttendanceDao = database.attendanceDao()

    @Provides
    @Singleton
    fun provideAttendanceRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        workerDao: WorkerDao,
        attendanceDao: AttendanceDao
    ): AttendanceRepository {
        return AttendanceRepository(firestore, auth, workerDao, attendanceDao)
    }
}