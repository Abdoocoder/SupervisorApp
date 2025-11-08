// FILE: app/src/main/java/com/madaba/supervisorapp/SupervisorApp.kt
package com.madaba.supervisorapp

import android.app.Application
import com.madaba.supervisorapp.data.sync.SyncManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * The main [Application] class for the SupervisorApp.
 *
 * This class is the entry point for the application and is responsible for
 * initializing application-wide components. It is annotated with [HiltAndroidApp]
 * to enable Hilt for dependency injection throughout the app.
 */
@HiltAndroidApp
class SupervisorApp : Application() {

    @Inject
    lateinit var syncManager: SyncManager

    override fun onCreate() {
        super.onCreate()
        // Schedule periodic sync when app starts
        // Note: This will be injected after Hilt initialization
        // We'll schedule it from MainActivity after login instead
    }
}