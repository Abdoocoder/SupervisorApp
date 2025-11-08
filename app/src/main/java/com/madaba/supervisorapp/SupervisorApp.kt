// FILE: app/src/main/java/com/madaba/supervisorapp/SupervisorApp.kt
package com.madaba.supervisorapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
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
class SupervisorApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}